package com.savesmart;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class HistoryFragment extends Fragment {

    private String mTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mTitle = getResources().getStringArray(R.array.nav_drawer_item_title)[4];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    public void onResume() {
        super.onResume();
        try {
            ((MainActivity) getActivity()).currentFragmentTitle = mTitle;
            ((MainActivity) getActivity()).getActionBar().setTitle(mTitle);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

}
