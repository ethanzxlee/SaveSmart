package com.savesmart;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;


public class HomeFragment extends Fragment {

    protected String mTitle;
    protected ListView homeListView;
    protected HomeListAdapter homeListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitle = getResources().getStringArray(R.array.nav_drawer_item_title)[0];
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            ((MainActivity) getActivity()).currentFragmentTitle = mTitle;
            if (((MainActivity) getActivity()).getActionBar().getTitle() != getString(R.string.app_name))
                ((MainActivity) getActivity()).getActionBar().setTitle(mTitle);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*homeListView = (ListView) getActivity().findViewById(R.id.fragment_home_listview);
        try {
            homeListAdapter = new HomeListAdapter(getActivity().getBaseContext(), R.id.fragment_home_listview_item_layout, R.id.fragment_home_listview_item_title);
            homeListView.setAdapter(homeListAdapter);
            homeListAdapter.add(new HashMap());
            homeListAdapter.add(new HashMap());
            homeListAdapter.add(new HashMap());
            homeListAdapter.notifyDataSetChanged();

        }
        catch (Exception e) {
            e.printStackTrace();
        }*/
        ParseCloud.callFunctionInBackground("GetHomeLists", new HashMap<String, Object>(), new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {

            }
        });
    }

    public class HomeListAdapter extends ArrayAdapter<HashMap> {

        public HomeListAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.fragment_home_listview_item, parent, false);/*
                    viewHolder= new ViewHolder();
                    viewHolder.productListItemImage = (ImageView)convertView.findViewById(R.id.product_list_item_image);
                    viewHolder.productListItemName = (TextView) convertView.findViewById(R.id.product_list_item_name);
                    viewHolder.productListItemPrice = (TextView) convertView.findViewById(R.id.product_list_item_price);
                    viewHolder.productListItemFavourite = (ToggleButton) convertView.findViewById(R.id.product_card_favourite);
                    viewHolder.productListItemPanel = (RelativeLayout) convertView.findViewById(R.id.product_list_item_panel);
                    convertView.setTag(viewHolder);*/
            } else {
                ///viewHolder = (ViewHolder) convertView.getTag();
            }
            return super.getView(position, convertView, parent);
        }
    }


}
