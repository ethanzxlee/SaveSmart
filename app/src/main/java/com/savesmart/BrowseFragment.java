package com.savesmart;


import android.os.Bundle;
import android.app.FragmentManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;


public class BrowseFragment extends Fragment {

    ListView listView ;
    SubBrowseFragment subBrowserFragment;
    static String itemValue;
    ArrayList<String> categoryList = new ArrayList<String>();

    private String mTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mTitle = getResources().getStringArray(R.array.nav_drawer_item_title)[1];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
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
        final RelativeLayout rlLoggingIn = (RelativeLayout) getActivity().findViewById(R.id.rl_logging_in);
        animation();
        ParseQuery<ParseObject> category = ParseQuery.getQuery("Category");
        category.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        category.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                try {
                    categoryList.clear();
                    if (e == null) {
                        for (int i = 0; i < parseObjects.size(); i++) {
                            categoryList.add(parseObjects.get(i).getString("categoryName"));
                        }
                        savedList();
                    } else {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e1){
                //    Toast.makeText(getActivity(), e1.getMessage(), Toast.LENGTH_SHORT).show();

                }
                rlLoggingIn.setVisibility(View.INVISIBLE);
            }
        });


    }

    public void savedList (){

        listView = (ListView) getActivity().findViewById(R.id.categorylistview);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, categoryList);

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                itemValue = (String) listView.getItemAtPosition(position);

                Bundle bundle = new Bundle();
                bundle.putString("mTitle", itemValue);

                subBrowserFragment = new SubBrowseFragment();
                subBrowserFragment.setArguments(bundle);
                ft.replace(R.id.main_container, subBrowserFragment).addToBackStack(null).commit();

            }
        });

    }


    public void animation (){
        final RelativeLayout rlLoggingIn = (RelativeLayout) getActivity().findViewById(R.id.rl_logging_in);
        rlLoggingIn.setVisibility(View.VISIBLE);
        animate(rlLoggingIn).alpha(0f).scaleX(2f).scaleY(2f).setDuration(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animate(rlLoggingIn).alpha(1f).scaleX(1f).scaleY(1f).setDuration(300).start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animate(rlLoggingIn).cancel();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }

}
