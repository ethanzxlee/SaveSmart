package com.savesmart;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.etsy.android.grid.StaggeredGridView;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ZheXian on 19/7/2014.
 */
public class ProductListFragment extends Fragment {

    public String mTitle;
    private List<ParseObject> result = new ArrayList<ParseObject>();
    private ProductListAdapter myTestAdapter;
    private StaggeredGridView staggeredGridView;
    private boolean paused = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitle = getResources().getStringArray(R.array.nav_drawer_item_title)[0];
        myTestAdapter = new ProductListAdapter(getActivity().getBaseContext(), R.layout.view_product_list_item_view, R.id.product_list_item_fake_text_view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_list, container, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            ((MainActivity) getActivity()).currentFragmentTitle = mTitle;
            if (((MainActivity) getActivity()).getActionBar().getTitle() != getString(R.string.app_name))
                ((MainActivity) getActivity()).getActionBar().setTitle(mTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        staggeredGridView = (StaggeredGridView) getActivity().findViewById(R.id.grid_view);
        staggeredGridView.setAdapter(myTestAdapter);

        if (!paused) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("latitude", Double.toString(((MainActivity) getActivity()).lastKnownLocation.getLatitude()));
            params.put("longitude", Double.toString(((MainActivity) getActivity()).lastKnownLocation.getLongitude()));

            ParseCloud.callFunctionInBackground("GetPopularProductList", params, new FunctionCallback<ArrayList<HashMap>>() {
                @Override
                public void done(ArrayList<HashMap> popularProductList, ParseException e) {
                    if (e == null) {
                        myTestAdapter.clear();
                        for (int i = 0; i < popularProductList.size(); i++) {
                            myTestAdapter.add(popularProductList.get(i));
                        }
                        myTestAdapter.notifyDataSetChanged();
                    } else {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    public class ViewHolder {
        ImageView productListItemImage;
        TextView productListItemName;
        TextView productListItemPrice;
        RelativeLayout productListItemPanel;
        ToggleButton productListItemFavourite;
    }

    public class ProductListAdapter extends ArrayAdapter<HashMap> {

        List<ParseObject> mResult;
        ViewHolder viewHolder;
        ImageLoaderTask imageLoaderTask;

        public ProductListAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.view_product_list_item_view, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.productListItemImage = (ImageView) convertView.findViewById(R.id.product_list_item_image);
                viewHolder.productListItemName = (TextView) convertView.findViewById(R.id.product_list_item_name);
                viewHolder.productListItemPrice = (TextView) convertView.findViewById(R.id.product_list_item_price);
                viewHolder.productListItemFavourite = (ToggleButton) convertView.findViewById(R.id.product_list_item_favourite);
                viewHolder.productListItemPanel = (RelativeLayout) convertView.findViewById(R.id.product_list_item_panel);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            View.OnClickListener favouriteListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            };

            View.OnClickListener openProductViewListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        ProductViewFragment productViewFragment = new ProductViewFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("productObjectId", ((ParseObject) getItem(position).get("product")).getObjectId());
                        productViewFragment.setArguments(bundle);
                        getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.main_container, productViewFragment).addToBackStack(null).commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            };

            viewHolder.productListItemPanel.setOnClickListener(openProductViewListener);
            viewHolder.productListItemImage.setOnClickListener(openProductViewListener);

            viewHolder.productListItemName.setText(((ParseObject) getItem(position).get("product")).getString("productName"));
            viewHolder.productListItemPrice.setText("RM " + getItem(position).get("cheapestPrice"));


            int imgHeight, imgWidth;
            viewHolder.productListItemImage.setImageBitmap(null);
            imgWidth = ((StaggeredGridView) parent).getColumnWidth();
            imgHeight = (int) (1.0 * imgWidth / ((ParseObject) getItem(position).get("productPhoto")).getInt("imgWidth") * ((ParseObject) getItem(position).get("productPhoto")).getInt("imgHeight"));
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, imgHeight);
            viewHolder.productListItemImage.setLayoutParams(layoutParams);

            imageLoaderTask = new ImageLoaderTask(getActivity().getBaseContext(), viewHolder.productListItemImage);
            imageLoaderTask.execute(((ParseObject) getItem(position).get("productPhoto")).getParseFile("photo"));

            if (ParseUser.getCurrentUser() == null) {
                viewHolder.productListItemFavourite.setVisibility(View.GONE);
            } else {
                viewHolder.productListItemFavourite.setChecked(false);
                ParseQuery<ParseObject> favouriteQuery = ParseQuery.getQuery("Favourites");
                favouriteQuery.whereEqualTo("product", (ParseObject) getItem(position).get("product"));
                favouriteQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
                favouriteQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(final ParseObject favouriteObject, ParseException e) {
                        if (e == null) {
                            viewHolder.productListItemFavourite.setChecked(true);
                        } else {
                            viewHolder.productListItemFavourite.setChecked(false);
                        }

                        viewHolder.productListItemFavourite.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final ToggleButton mView = (ToggleButton) view;
                                if (mView.isChecked()) {

                                    ParseACL favouriteACL = new ParseACL();
                                    favouriteACL.setReadAccess(ParseUser.getCurrentUser(), true);
                                    favouriteACL.setWriteAccess(ParseUser.getCurrentUser(), true);

                                    ParseObject favourite = new ParseObject("Favourites");
                                    favourite.put("product", (ParseObject) getItem(position).get("product"));
                                    favourite.put("owner", ParseUser.getCurrentUser());
                                    favourite.setACL(favouriteACL);
                                    favourite.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            mView.setClickable(true);
                                        }
                                    });
                                    mView.setClickable(false);
                                } else {
                                    ParseQuery<ParseObject> favouriteQuery = ParseQuery.getQuery("Favourites");
                                    favouriteQuery.whereEqualTo("product", (ParseObject) getItem(position).get("product"));
                                    favouriteQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                                        @Override
                                        public void done(ParseObject favouriteObject, ParseException e) {
                                            if (e == null) {
                                                favouriteObject.deleteEventually();
                                                mView.setClickable(true);
                                            } else {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    mView.setClickable(false);
                                }
                            }
                        });
                    }
                });
            }

            return super.getView(position, convertView, parent);
        }

    }
}
