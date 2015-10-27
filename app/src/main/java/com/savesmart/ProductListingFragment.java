package com.savesmart;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseACL;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import it.gmariotti.cardslib.library.extra.staggeredgrid.internal.CardGridStaggeredArrayAdapter;
import it.gmariotti.cardslib.library.extra.staggeredgrid.view.CardGridStaggeredView;
import it.gmariotti.cardslib.library.internal.Card;

public class ProductListingFragment extends Fragment {

    protected boolean isSearch = false;
    protected boolean isSavedList = false;
    protected boolean isFavourite = false;
    protected boolean isHistory = false;
    protected boolean isHome = false;
    protected boolean isSubCategory = false;
    ProductView productView;
    private String mTitle;
    private ArrayList<Card> productCardsList = new ArrayList<Card>();
    private CardGridStaggeredArrayAdapter mCardArrayAdapter;
    //RelativeLayout rlSearchingProduct;
    private ParseObject mSavedListObject;
    private Fragment mFragment;
    private TextView tvMessage;

    public ProductListingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product_listing, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isSavedList)
            inflater.inflate(R.menu.product_listing_saved_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_product_listing_delete_list) {
            DeleteSavedListDialog deleteSavedListDialog = new DeleteSavedListDialog(mSavedListObject);
            deleteSavedListDialog.show(getFragmentManager(), "deleteList");
            return true;
        } else if (item.getItemId() == R.id.menu_product_listing_rename_list) {
            RenameSavedListDialog renameSavedListDialog = new RenameSavedListDialog(mSavedListObject);
            renameSavedListDialog.show(getFragmentManager(), "deleteList");
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {

            if (getArguments().getBoolean("isSearch", false)) {
                //final RelativeLayout rlSearchProduct = (RelativeLayout) getActivity().findViewById(R.id.rl_searching_product);
                //rlSearchingProduct = rlSearchProduct;
                //rlSearchingProduct.setVisibility(View.VISIBLE);
                //rlSearchingProduct.setAnimation(new Animation() {
                //});
                /*animate(rlSearchingProduct).alpha(0f).setDuration(0).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animate(rlSearchingProduct).alpha(1f).setDuration(320).start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }

                }).start();*/
                isSearch = true;
                loadSearchResult(getArguments().getString("searchTerm"));
                mTitle = "Search Results";

            } else if (getArguments().getBoolean("isSavedList", false)) {
                isSavedList = true;
                mTitle = getArguments().getString("savedListName");
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("savedList", getArguments().getCharSequence("savedList").toString());
                loadSavedList(params);
            } else if (getArguments().getBoolean("isFavourite", false)) {
                isFavourite = true;
                mTitle = getResources().getStringArray(R.array.nav_drawer_item_title)[3];
                loadFavouriteList();
            } else if (getArguments().getBoolean("isHistory", false)) {
                isHistory = true;
                mTitle = getResources().getStringArray(R.array.nav_drawer_item_title)[4];
                loadHistoryList();
            } else if (getArguments().getBoolean("isHome", false)) {
                isHome = true;
                mTitle = getResources().getStringArray(R.array.nav_drawer_item_title)[0];
                loadHomeList();
            } else if (getArguments().getBoolean("isSubCategory", false)) {
                isSubCategory = true;
                mTitle = getArguments().getString("subCategoryName");
                loadSubCategoryList(getArguments().getString("subCategoryId"));
            }

            ((MainActivity) getActivity()).currentFragmentTitle = mTitle;
            ((MainActivity) getActivity()).getActionBar().setTitle(mTitle);

        } catch (Exception e) {
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {

            mCardArrayAdapter = new CardGridStaggeredArrayAdapter(getActivity(), productCardsList);

            CardGridStaggeredView mGridView = (CardGridStaggeredView) getActivity().findViewById(R.id.staggered_card_product_list);
            if (mGridView != null) {
                mGridView.setAdapter(mCardArrayAdapter);
            }

            tvMessage = (TextView) getActivity().findViewById(R.id.product_listing_message);

        } catch (Exception ex) {

        }

    }


    private void loadSubCategoryList(String subCategoryId) {

        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("subCategoryObjectId", subCategoryId);

        ParseCloud.callFunctionInBackground("GetSubCategoryList", hashMap, new FunctionCallback<ArrayList<ParseObject>>() {
            @Override
            public void done(ArrayList<ParseObject> savedList, ParseException e) {
                try {
                    //rlSearchingProduct.setVisibility(View.INVISIBLE);
                    if (e == null) {
                        if (savedList.size() == 0) {
                            tvMessage.setVisibility(View.VISIBLE);
                            tvMessage.setText("No content over here");
                        } else {
                            tvMessage.setVisibility(View.GONE);
                        }
                        productCardsList.clear();
                        for (int i = 0; i < savedList.size(); i++) {
                            ProductCard productCard = new ProductCard(getActivity().getBaseContext(), savedList.get(i));
                            productCardsList.add(productCard);
                        }
                        mCardArrayAdapter.notifyDataSetChanged();
                    } else {
                        //Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e1) {
                    //e1.printStackTrace();
                }
            }
        });


    }


    private void loadHomeList() {

        ParseCloud.callFunctionInBackground("GetHomeList", new HashMap<String, Object>(), new FunctionCallback<ArrayList<ParseObject>>() {
            @Override
            public void done(ArrayList<ParseObject> savedList, ParseException e) {
                try {
                    //rlSearchingProduct.setVisibility(View.INVISIBLE);
                    if (e == null) {
                        if (savedList.size() == 0) {
                            tvMessage.setVisibility(View.VISIBLE);
                            tvMessage.setText("No content over here");
                        } else {
                            tvMessage.setVisibility(View.GONE);
                        }
                        productCardsList.clear();
                        for (int i = 0; i < savedList.size(); i++) {
                            ProductCard productCard = new ProductCard(getActivity().getBaseContext(), savedList.get(i));
                            productCardsList.add(productCard);
                        }
                        mCardArrayAdapter.notifyDataSetChanged();
                    } else {
                        //Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e1) {
                    //e1.printStackTrace();
                }
            }
        });

    }

    private void loadHistoryList() {

        ParseCloud.callFunctionInBackground("GetHistory", new HashMap<String, Object>(), new FunctionCallback<ArrayList<ParseObject>>() {
            @Override
            public void done(ArrayList<ParseObject> savedList, ParseException e) {
                try {
                    //rlSearchingProduct.setVisibility(View.INVISIBLE);
                    if (e == null) {
                        if (savedList.size() == 0) {
                            tvMessage.setVisibility(View.VISIBLE);
                            tvMessage.setText("No content over here");
                        } else {
                            tvMessage.setVisibility(View.GONE);
                        }
                        productCardsList.clear();
                        for (int i = 0; i < savedList.size(); i++) {
                            ProductCard productCard = new ProductCard(getActivity().getBaseContext(), savedList.get(i));
                            productCardsList.add(productCard);
                        }
                        mCardArrayAdapter.notifyDataSetChanged();
                    } else {
                        //Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e1) {
                    //e1.printStackTrace();
                }
            }
        });

    }

    private void loadSearchResult(String searchTerm) {

        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("searchTerm", searchTerm);

        ParseCloud.callFunctionInBackground("GetSearchProductsResult", hashMap, new FunctionCallback<ArrayList<ParseObject>>() {
            @Override
            public void done(ArrayList<ParseObject> savedList, ParseException e) {
                try {
                    //rlSearchingProduct.setVisibility(View.INVISIBLE);
                    if (e == null) {
                        if (savedList.size() == 0) {
                            tvMessage.setVisibility(View.VISIBLE);
                            tvMessage.setText("No content over here");
                        } else {
                            tvMessage.setVisibility(View.GONE);
                        }
                        productCardsList.clear();
                        for (int i = 0; i < savedList.size(); i++) {
                            ProductCard productCard = new ProductCard(getActivity().getBaseContext(), savedList.get(i));
                            productCardsList.add(productCard);
                        }
                        mCardArrayAdapter.notifyDataSetChanged();
                    } else {
                        //Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e1) {
                    //e1.printStackTrace();
                }
            }
        });

    }

    private void loadFavouriteList() {
        ParseCloud.callFunctionInBackground("GetFavouriteItemProduct", new HashMap<String, Object>(), new FunctionCallback<ArrayList<ParseObject>>() {
            @Override
            public void done(ArrayList<ParseObject> savedList, ParseException e) {
                try {
                    if (e == null) {
                        if (savedList.size() == 0) {
                            tvMessage.setVisibility(View.VISIBLE);
                            tvMessage.setText("No content over here");
                        } else {
                            tvMessage.setVisibility(View.GONE);
                        }
                        productCardsList.clear();
                        for (int i = 0; i < savedList.size(); i++) {
                            ProductCard productCard = new ProductCard(getActivity().getBaseContext(), savedList.get(i));
                            productCardsList.add(productCard);
                        }
                        mCardArrayAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    protected void loadSavedList(HashMap<String, String> params) {
        ParseQuery<ParseObject> savedListObjectQuery = ParseQuery.getQuery("SavedLists");
        savedListObjectQuery.getInBackground(getArguments().getString("savedList"), new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                mSavedListObject = parseObject;
            }
        });

        ParseCloud.callFunctionInBackground("GetSavedListItemProduct", params, new FunctionCallback<ArrayList<ParseObject>>() {
            @Override
            public void done(ArrayList<ParseObject> savedList, ParseException e) {
                try {
                    if (savedList.size() == 0) {
                        tvMessage.setVisibility(View.VISIBLE);
                        tvMessage.setText("No content over here");
                    } else {
                        tvMessage.setVisibility(View.GONE);
                    }
                    if (e == null) {
                        productCardsList.clear();
                        for (int i = 0; i < savedList.size(); i++) {
                            ProductCard productCard = new ProductCard(getActivity().getBaseContext(), savedList.get(i));
                            productCardsList.add(productCard);
                        }
                        mCardArrayAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static class DeleteSavedListDialog extends DialogFragment {

        private EditText etListName;
        private ParseObject mSavedListsObject;

        public DeleteSavedListDialog() {
            super();
        }

        @SuppressLint("ValidFragment")
        public DeleteSavedListDialog(ParseObject savedListObject) {
            super();
            mSavedListsObject = savedListObject;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("Delete list ?");
            builder.setPositiveButton(R.string.action_ok, null);
            builder.setNegativeButton(R.string.action_cancel, null);

            return builder.create();
        }

        @Override
        public void onStart() {
            super.onStart();
            final AlertDialog alertDialog = (AlertDialog) getDialog();

            if (alertDialog != null) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mSavedListsObject.deleteEventually();
                        alertDialog.cancel();
                        try {
                            getFragmentManager().popBackStack();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }
        }

    }

    public static class RenameSavedListDialog extends DialogFragment {

        private EditText etListName;
        private ParseObject mSavedListsObject;

        public RenameSavedListDialog() {
            super();
        }

        @SuppressLint("ValidFragment")
        public RenameSavedListDialog(ParseObject savedListObject) {
            super();
            mSavedListsObject = savedListObject;

        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final View view = getActivity().getLayoutInflater().inflate(R.layout.popup_save_lists_rename, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("Rename list");
            builder.setView(view);
            builder.setPositiveButton(R.string.action_ok, null);
            etListName = (EditText) view.findViewById(R.id.et_saved_list_rename_name);
            builder.setNegativeButton(R.string.action_cancel, null);

            return builder.create();
        }

        @Override
        public void onStart() {
            super.onStart();
            final AlertDialog alertDialog = (AlertDialog) getDialog();

            if (alertDialog != null) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (etListName.getText().toString().equals("")) {
                            Toast.makeText(getActivity(), R.string.error_field_empty, Toast.LENGTH_SHORT).show();
                            etListName.setError("");
                        } else {
                            try {
                                mSavedListsObject.put("listName", etListName.getText().toString());
                                mSavedListsObject.saveEventually(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                    }
                                });
                                Toast.makeText(getActivity(), "The list has been renamed", Toast.LENGTH_SHORT).show();
                                alertDialog.cancel();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }

    }

    public class ProductCard extends Card {

        private ParseObject mSavedListItem = null;

        public ProductCard(Context context) {
            this(context, R.layout.card_layout_product_inner_layout);
        }

        public ProductCard(Context context, ParseObject savedListItem) {
            this(context, R.layout.card_layout_product_inner_layout);
            mSavedListItem = savedListItem;
        }

        public ProductCard(Context context, int innerLayout) {
            super(context, innerLayout);
        }


        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            try {
                RelativeLayout rlProductInfoPanel = (RelativeLayout) view.findViewById(R.id.product_card_info_panel);
                TextView tvProductName = (TextView) view.findViewById(R.id.product_card_product_name);
                TextView tvProductPrice = (TextView) view.findViewById(R.id.product_card_product_price);
                final ToggleButton tgbtnFavourite = (ToggleButton) view.findViewById(R.id.product_card_favourite);
                final ImageView ivProductPhoto = (ImageView) view.findViewById(R.id.product_card_product_photo);

                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ProductViewFragment productViewFragment = new ProductViewFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("productObjectId", mSavedListItem.getParseObject("product").getObjectId());
                        productViewFragment.setArguments(bundle);
                        getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.main_container, productViewFragment).addToBackStack(null).commit();
                    }
                };

                rlProductInfoPanel.setOnClickListener(onClickListener);
                ivProductPhoto.setOnClickListener(onClickListener);

                if (isFavourite || isSavedList || ParseUser.getCurrentUser() == null) {
                    tgbtnFavourite.setVisibility(View.GONE);
                } else {
                    ParseQuery<ParseObject> favouriteQuery = ParseQuery.getQuery("Favourites");
                    favouriteQuery.whereEqualTo("product", mSavedListItem.getParseObject("product"));
                    favouriteQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(final ParseObject favouriteObject, ParseException e) {
                            if (e == null) {
                                tgbtnFavourite.setChecked(true);
                            } else {
                                tgbtnFavourite.setChecked(false);
                            }

                            tgbtnFavourite.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    final ToggleButton mView = (ToggleButton) view;
                                    if (mView.isChecked()) {

                                        ParseACL favouriteACL = new ParseACL();
                                        favouriteACL.setReadAccess(ParseUser.getCurrentUser(), true);
                                        favouriteACL.setWriteAccess(ParseUser.getCurrentUser(), true);

                                        ParseObject favourite = new ParseObject("Favourites");
                                        favourite.put("product", mSavedListItem.getParseObject("product"));
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
                                        favouriteQuery.whereEqualTo("product", mSavedListItem.getParseObject("product"));
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

                tvProductName.setText(mSavedListItem.getParseObject("product").getString("productName"));

                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                tvProductPrice.setText("RM " + decimalFormat.format(mSavedListItem.getDouble("price")));

                ParseQuery<ParseObject> productPhotoQuery = ParseQuery.getQuery("ProductPhoto");
                productPhotoQuery.whereEqualTo("product", mSavedListItem.getParseObject("product"));
                productPhotoQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        ParseFile productPhotoFile = parseObject.getParseFile("photo");
                        productPhotoFile.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] bytes, ParseException e) {
                                if (e == null) {
                                    try {
                                        Bitmap productPhotoBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        ivProductPhoto.setImageBitmap(productPhotoBitmap);
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                } else {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
