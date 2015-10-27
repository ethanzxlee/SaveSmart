package com.savesmart;

//import android.app.FragmentManager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
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
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

/**
 * Created by LestonYong on 4/7/2014.
 */
public class SubBrowseFragment extends Fragment {

    static String itemValue, itemValueP, productObjectId;
    static ArrayList<String> subCategoryL = new ArrayList<String>();
    static ArrayList<String> ListOfObjectId = new ArrayList<String>();
    ListView listView;
    ProductListingFragment productListingFragment;
    private String mTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mTitle = bundle.getString("mTitle");
            itemValueP = mTitle;
        } else mTitle = "GetError";


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
        // Loading Animation Start
        animation();
        // Go to get the List
        getSubList();

    }

    public void getSubList() {
        final RelativeLayout rlLoggingIn = (RelativeLayout) getActivity().findViewById(R.id.rl_logging_in);


        ParseQuery innerQuery = new ParseQuery("Category");
        innerQuery.whereEqualTo("categoryName", itemValueP);
        ParseQuery query = new ParseQuery("Subcategory");
        query.whereMatchesQuery("subcategory", innerQuery);
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                try {
                    if (e == null) {
                        subCategoryL.clear();
                        ListOfObjectId.clear();
                        for (int i = 0; i < list.size(); i++) {
                            subCategoryL.add(list.get(i).getString("name"));
                            ListOfObjectId.add(list.get(i).getObjectId());

                        }
                        savedList();
                    } else
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                } catch (Exception e1) {
                    //         Toast.makeText(getActivity(), e1.getMessage(), Toast.LENGTH_SHORT).show();
                }
                rlLoggingIn.setVisibility(View.INVISIBLE);
            }
        });

    }


    public void animation() {
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

    public void savedList() {
        listView = (ListView) getActivity().findViewById(R.id.categorylistview);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, subCategoryL);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                int itemPosition = position;

                itemValue = (String) listView.getItemAtPosition(position);


                productObjectId = ListOfObjectId.get(itemPosition);
                Bundle bundle = new Bundle();
                bundle.putBoolean("isSubCategory", true);
                bundle.putString("subCategoryId", productObjectId);
                bundle.putString("subCategoryName", itemValue);
                productListingFragment = new ProductListingFragment();
                productListingFragment.setArguments(bundle);


                ft.replace(R.id.main_container, productListingFragment).addToBackStack(null).commit();
            }
        });

    }
}