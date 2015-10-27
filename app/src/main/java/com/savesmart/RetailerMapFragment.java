package com.savesmart;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class RetailerMapFragment extends Fragment {

    MapFragment mapFragment;
    GoogleMap googleMap;
    HashMap data;
    LatLng onClickRetailerLatLng;
    ViewPager viewPager;
    RetailerViewPagerAdapter retailerViewPagerAdapter;
    ArrayList priceWithRetailerInfo;

    public RetailerMapFragment() {
    }

    @SuppressLint("ValidFragment")
    public RetailerMapFragment(HashMap data) {
        this.data = data;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_retailer_map, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.retailer_map_map)).getMap();
        googleMap.setMyLocationEnabled(true);

        priceWithRetailerInfo = (ArrayList) data.get("priceWithRetailerInfo");
        for (int i = 0; i < priceWithRetailerInfo.size(); i++) {
            double lat = (Double) ((HashMap) ((HashMap) ((HashMap) priceWithRetailerInfo.get(i)).get("retailer")).get("location")).get("lat");
            double lng = (Double) ((HashMap) ((HashMap) ((HashMap) priceWithRetailerInfo.get(i)).get("retailer")).get("location")).get("lng");
            if (getArguments().getInt("onClickRetailer") == i) {
                onClickRetailerLatLng = new LatLng(lat, lng);
            }
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(lat, lng));
            markerOptions.title(((HashMap) ((HashMap) priceWithRetailerInfo.get(i)).get("retailer")).get("name").toString());
            googleMap.addMarker(markerOptions);
        }

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(onClickRetailerLatLng, 15));

        viewPager = (ViewPager) getActivity().findViewById(R.id.retailer_map_info_view_pager);
        retailerViewPagerAdapter = new RetailerViewPagerAdapter();
        viewPager.setAdapter(retailerViewPagerAdapter);
        viewPager.setCurrentItem(getArguments().getInt("onClickRetailer"));
    }

    @Override
    public void onDestroyView() {
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.retailer_map_map);
        if (mapFragment != null) {
            try {
                getFragmentManager().beginTransaction().remove(mapFragment).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onDestroyView();
    }

    public class RetailerViewPagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_retailer_map_info, null, false);

            ((TextView) view.findViewById(R.id.retailer_map_retailer_name)).setText(((HashMap) ((HashMap) priceWithRetailerInfo.get(position)).get("retailer")).get("name").toString());

            ArrayList addressLines = ((ArrayList) ((HashMap) ((HashMap) ((HashMap) priceWithRetailerInfo.get(position)).get("retailer")).get("location")).get("formattedAddress"));
            String combinedAddress = "";
            for (int i = 0; i < addressLines.size(); i++) {
                combinedAddress += addressLines.get(i).toString() + "\n";
            }
            ((TextView) view.findViewById(R.id.retailer_map_retailer_address)).setText(addressLines.get(1).toString());

            DecimalFormat priceFormat = new DecimalFormat("0.00");
            ((TextView) view.findViewById(R.id.retailer_map_product_price)).setText("RM " + priceFormat.format(((ParseObject) ((HashMap) priceWithRetailerInfo.get(position)).get("price")).getDouble("price")));

            Location retailerLocation = new Location("");
            retailerLocation.setLatitude((Double) ((HashMap) ((HashMap) ((HashMap) priceWithRetailerInfo.get(position)).get("retailer")).get("location")).get("lat"));
            retailerLocation.setLongitude((Double) ((HashMap) ((HashMap) ((HashMap) priceWithRetailerInfo.get(position)).get("retailer")).get("location")).get("lng"));
            double distanceMeter = ((MainActivity) getActivity()).lastKnownLocation.distanceTo(retailerLocation);
            double distanceKm = distanceMeter / 1000;

            DecimalFormat distanceFormat = new DecimalFormat("0.000");
            ((TextView) view.findViewById(R.id.retailer_map_retailer_distance)).setText(distanceFormat.format(distanceKm) + " km");

            SimpleDateFormat formatDateJava = new SimpleDateFormat("dd/MM/yyyy");
            ((TextView) view.findViewById(R.id.retailer_map_product_price_last_update)).setText("on " + formatDateJava.format(((ParseObject) ((HashMap) priceWithRetailerInfo.get(position)).get("price")).getCreatedAt()));

            ((TextView) view.findViewById(R.id.retailer_map_product_name)).setText(((ParseObject) data.get("product")).getString("productName"));


            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            super.finishUpdate(container);
            int position = viewPager.getCurrentItem();
            double lat = (Double) ((HashMap) ((HashMap) ((HashMap) priceWithRetailerInfo.get(position)).get("retailer")).get("location")).get("lat");
            double lng = (Double) ((HashMap) ((HashMap) ((HashMap) priceWithRetailerInfo.get(position)).get("retailer")).get("location")).get("lng");
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));

        }

        @Override
        public int getCount() {
            return ((ArrayList) data.get("priceWithRetailerInfo")).size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (View) object;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }
    }
}
