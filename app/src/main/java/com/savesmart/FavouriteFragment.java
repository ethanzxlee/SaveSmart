package com.savesmart;

public class FavouriteFragment extends ProductListFragment {

    private String mTitle;

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
