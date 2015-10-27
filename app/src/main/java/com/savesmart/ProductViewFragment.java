package com.savesmart;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;


public class ProductViewFragment extends Fragment {

    public static String productObjectId;
    protected TextView tvProductName;
    protected ImageView ivProductPhoto;
    protected LinearLayout priceUpdatesListLayout;
    protected ToggleButton btnFavourite;
    protected ImageButton btnUpdatePrice;
    protected ImageButton btnAddToList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            productObjectId = getArguments().getString("productObjectId");
            priceUpdatesListLayout = (LinearLayout) getActivity().findViewById(R.id.product_view_section_price_list);
            btnUpdatePrice = (ImageButton) getActivity().findViewById(R.id.product_view_update_price);
            btnAddToList = (ImageButton) getActivity().findViewById(R.id.product_view_add_to_list);
            btnFavourite = (ToggleButton) getActivity().findViewById(R.id.product_view_favourite);

            if (ParseUser.getCurrentUser() == null) {
                btnAddToList.setVisibility(View.GONE);
                btnUpdatePrice.setVisibility(View.GONE);
                btnFavourite.setVisibility(View.GONE);
            } else {

                btnAddToList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SavedListSelectorFragment savedListSelectorFragment = new SavedListSelectorFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("productObjectId", getArguments().getString("productObjectId"));
                        savedListSelectorFragment.setArguments(bundle);
                        getFragmentManager().beginTransaction().replace(R.id.product_view_select_list_section, savedListSelectorFragment).addToBackStack(null).commit();
                    }
                });

                btnFavourite.setChecked(false);


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            getDataFromParse(((MainActivity) getActivity()).lastKnownLocation);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getDataFromParse(Location currentLocation) {

        final HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("productObjectId", getArguments().getString("productObjectId"));
        hashMap.put("latitude", currentLocation.getLatitude());
        hashMap.put("longitude", currentLocation.getLongitude());
        hashMap.put("radius", 40000);

        ParseCloud.callFunctionInBackground("GetProductDetails", hashMap, new FunctionCallback<HashMap<String, Object>>() {
            @Override
            public void done(final HashMap<String, Object> data, ParseException e) {
                if (e == null) {
                    final ParseObject productObject = (ParseObject) data.get("product");
                    try {
                        tvProductName = (TextView) getActivity().findViewById(R.id.tv_product_view_product_name);
                        tvProductName.setText(productObject.getString("productName"));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                    ParseFile photoFile = ((ParseObject) (((ArrayList) (data.get("photo"))).get(0))).getParseFile("photo");
                    try {
                        ivProductPhoto = (ImageView) getActivity().findViewById(R.id.product_view_product_photo);
                        photoFile.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] bytes, ParseException e) {
                                if (e == null)
                                    try {
                                        ivProductPhoto.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                else
                                    e.printStackTrace();
                            }
                        });
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                    final ArrayList priceWithRetailerInfo = (ArrayList) data.get("priceWithRetailerInfo");
                    try {
                        for (int i = 0; i < priceWithRetailerInfo.size(); i++) {
                            getActivity().getLayoutInflater().inflate(R.layout.product_view_section_price_list, priceUpdatesListLayout);
                        }

                        for (int i = 0; i < priceUpdatesListLayout.getChildCount(); i++) {
                            View view = priceUpdatesListLayout.getChildAt(i);
                            ((TextView) view.findViewById(R.id.product_view_price_section_retailer)).setText(((HashMap) ((HashMap) priceWithRetailerInfo.get(i)).get("retailer")).get("name").toString());

                            DecimalFormat priceFormat = new DecimalFormat("0.00");
                            ((TextView) view.findViewById(R.id.product_view_price_section_price)).setText("RM " + priceFormat.format(((ParseObject) ((HashMap) priceWithRetailerInfo.get(i)).get("price")).getDouble("price")));

                            Location retailerLocation = new Location("");
                            retailerLocation.setLatitude((Double) ((HashMap) ((HashMap) ((HashMap) priceWithRetailerInfo.get(i)).get("retailer")).get("location")).get("lat"));
                            retailerLocation.setLongitude((Double) ((HashMap) ((HashMap) ((HashMap) priceWithRetailerInfo.get(i)).get("retailer")).get("location")).get("lng"));
                            double distanceMeter = ((MainActivity) getActivity()).lastKnownLocation.distanceTo(retailerLocation);
                            double distanceKm = distanceMeter / 1000;

                            DecimalFormat distanceFormat = new DecimalFormat("0.000");
                            ((TextView) view.findViewById(R.id.product_view_price_section_distance)).setText(distanceFormat.format(distanceKm) + " km");

                            SimpleDateFormat formatDateJava = new SimpleDateFormat("dd/MM/yyyy");
                            ((TextView) view.findViewById(R.id.product_view_price_section_last_update)).setText("on " + formatDateJava.format(((ParseObject) ((HashMap) priceWithRetailerInfo.get(i)).get("price")).getCreatedAt()));

                            final int finalI = i;
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    RetailerMapFragment retailerMapFragment = new RetailerMapFragment(data);
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("onClickRetailer", finalI);
                                    retailerMapFragment.setArguments(bundle);
                                    getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.main_container, retailerMapFragment).addToBackStack(null).commit();
                                }
                            });
                        }

                    } catch (Exception e1) {

                    }

                    ParseQuery<ParseObject> favouriteQuery = ParseQuery.getQuery("Favourites");
                    favouriteQuery.whereEqualTo("product", productObject);
                    favouriteQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
                    favouriteQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(final ParseObject favouriteObject, ParseException e) {
                            try {
                                if (e == null) {
                                    btnFavourite.setChecked(true);
                                } else {
                                    btnFavourite.setChecked(false);
                                }

                                btnFavourite.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        final ToggleButton mView = (ToggleButton) view;
                                        if (mView.isChecked()) {

                                            ParseACL favouriteACL = new ParseACL();
                                            favouriteACL.setReadAccess(ParseUser.getCurrentUser(), true);
                                            favouriteACL.setWriteAccess(ParseUser.getCurrentUser(), true);

                                            ParseObject favourite = new ParseObject("Favourites");
                                            favourite.put("product", (ParseObject) productObject);
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
                                            favouriteQuery.whereEqualTo("product", (ParseObject) productObject);
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
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                        }
                    });

                    btnUpdatePrice.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DialogFragment updatePriceDialogFragment = new updatePriceDialogFragment();
                            updatePriceDialogFragment.show(getFragmentManager(), "UpdatePrice");
                        }
                    });


                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public static class updatePriceDialogFragment extends DialogFragment {

        protected ArrayAdapter<CharSequence> adapter;
        protected String retailerFoursquareId;
        EditText price;
        Spinner retailers;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final View view = getActivity().getLayoutInflater().inflate(R.layout.update_price, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.update_price_thanks);
            builder.setView(view);
            builder.setPositiveButton(R.string.action_ok, null);
            price = (EditText) view.findViewById(R.id.et_update_price);
            retailers = (Spinner) view.findViewById(R.id.update_retailer);

            HashMap<String, String> params = new HashMap<String, String>();
            params.put("latitude", Double.toString(((MainActivity) getActivity()).lastKnownLocation.getLatitude()));
            params.put("longitude", Double.toString(((MainActivity) getActivity()).lastKnownLocation.getLongitude()));
            params.put("radius", Double.toString(50000));

            ParseCloud.callFunctionInBackground("GetNearbyRetailers", params, new FunctionCallback<ArrayList>() {
                @Override
                public void done(final ArrayList nearbyRetailers, ParseException e) {
                    adapter.clear();
                    for (int i = 0; i < nearbyRetailers.size(); i++) {
                        adapter.add(((HashMap) (nearbyRetailers).get(i)).get("name") + ",  " + ((HashMap) ((HashMap) ((nearbyRetailers).get(i))).get("location")).get("city"));
                    }
                    adapter.notifyDataSetChanged();
                    retailers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            retailerFoursquareId = ((HashMap) nearbyRetailers.get(i)).get("id").toString();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                }
            });
            adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            retailers.setAdapter(adapter);

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

                        if (price.getText().toString().equals("")) {
                            Toast.makeText(getActivity(), R.string.error_price_empty, Toast.LENGTH_SHORT).show();
                            price.setError("");
                        } else {
                            String productPrice = price.getText().toString().trim();
                            final double newPrice = Double.parseDouble(productPrice);

                            ParseObject priceObject = new ParseObject("Price");
                            ParseObject P_Object = ParseObject.createWithoutData("Product", productObjectId);
                            priceObject.put("price", newPrice);
                            priceObject.put("product", P_Object);
                            priceObject.put("retailerFoursquareId", retailerFoursquareId);
                            priceObject.saveEventually();
                            Toast.makeText(getActivity(), "Price updated. Thanks!", Toast.LENGTH_LONG).show();


                            /*final ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Retailers");
                            query1.whereEqualTo("retailerName",retailerName.toUpperCase());
                            query1.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    if(e!=null){
                                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                        System.out.println(e.getMessage());
                                    }else {
                                        retailerObjectid = parseObject.getObjectId();
                                        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Price");
                                        ParseObject P_Object = ParseObject.createWithoutData("Product",productObjectId);
                                        ParseObject R_Object = ParseObject.createWithoutData("Retailers",retailerFoursquareId);
                                        //query.whereEqualTo("product",ProductObjectId );
                                        //query.whereEqualTo("retailers", retailerObjectid);
                                        query.whereEqualTo("product",P_Object );
                                        query.whereEqualTo("retailers", R_Object);
                                        query.getFirstInBackground(new GetCallback<ParseObject>() {
                                            @Override
                                            public void done(ParseObject parseObject, ParseException e) {
                                                if (e== null){
                                                    parseObject.put("price",newPrice);
                                                    parseObject.saveInBackground();
                                                } else {
                                                    Toast.makeText(getActivity(),e.getMessage().toString(),Toast.LENGTH_LONG).show();
                                                }

                                            }
                                        });
                                    }


                                }
                            });*/

                            alertDialog.cancel();
                        }
                    }
                });
            }
        }
    }

}
