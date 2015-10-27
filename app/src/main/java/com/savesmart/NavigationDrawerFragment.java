package com.savesmart;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class NavigationDrawerFragment extends Fragment {


    private static Activity mActivity;
    private static List<NavDrawerItem> itemList;
    private static int itemListLength;
    OnNavDrawerItemSelectedListener mCallback;
    private String[] navDrawerItemTitle;
    private TypedArray navDrawerItemIcon;
    private NavDrawerItemAdapter navDrawerItemAdapter;

    public static void refreshUserProfile() {
        if (ParseUser.getCurrentUser() != null) {
            mActivity.findViewById(R.id.nav_drawer_login_sign_up).setVisibility(View.GONE);
            mActivity.findViewById(R.id.btn_nav_drawer_username).setVisibility(View.VISIBLE);
            mActivity.findViewById(R.id.ib_nav_drawer_profile_pic).setVisibility(View.VISIBLE);

            ParseUser currentUser = ParseUser.getCurrentUser();
            ParseObject currentUserProfile = currentUser.getParseObject("userProfile");

            ParseQuery<ParseObject> userProfileQuery = ParseQuery.getQuery("UserProfile");
            userProfileQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
            userProfileQuery.whereEqualTo("objectId", currentUserProfile.getObjectId());
            userProfileQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject userProfile, ParseException e) {
                    if (e == null) {
                        ((Button) (mActivity.findViewById(R.id.btn_nav_drawer_username))).setText(userProfile.getString("firstName") + " " + userProfile.getString("lastName"));
                        ParseFile profilePic = userProfile.getParseFile("profilePic");
                        if (profilePic != null) {
                            profilePic.getDataInBackground(new GetDataCallback() {
                                @Override
                                public void done(byte[] bytes, ParseException e) {
                                    if (e == null) {
                                        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        ((CircularImageView) mActivity.findViewById((R.id.ib_nav_drawer_profile_pic))).setImageBitmap(bitmapImage);
                                    } else {
                                        Toast.makeText(mActivity.getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }
            });

        } else {
            mActivity.findViewById(R.id.nav_drawer_login_sign_up).setVisibility(View.VISIBLE);
            mActivity.findViewById(R.id.btn_nav_drawer_username).setVisibility(View.GONE);
            mActivity.findViewById(R.id.ib_nav_drawer_profile_pic).setVisibility(View.GONE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        itemList = new ArrayList<NavDrawerItem>();

        try {
            mCallback = (OnNavDrawerItemSelectedListener) activity;
        } catch (ClassCastException ex) {
            throw new ClassCastException(activity.toString() + "must implement OnNavDrawerItemSelectedListener");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadNavDrawerList();
        refreshUserProfile();
    }


    private void loadNavDrawerList() {
        navDrawerItemTitle = getResources().getStringArray(R.array.nav_drawer_item_title);
        navDrawerItemIcon = getResources().obtainTypedArray(R.array.nav_drawer_item_icon);

        itemListLength = navDrawerItemTitle.length;

        for (int i = 0; i < itemListLength; i++) {
            itemList.add(new NavDrawerItem(navDrawerItemTitle[i], navDrawerItemIcon.getResourceId(i, -1)));
        }

        ListView navDrawerListLayout = (ListView) mActivity.findViewById(R.id.nav_drawer_list_layout);
        navDrawerItemAdapter = new NavDrawerItemAdapter(mActivity.getBaseContext(), R.layout.view_nav_drawer_item, itemList);
        navDrawerListLayout.setAdapter(navDrawerItemAdapter);
    }

    public interface OnNavDrawerItemSelectedListener {
        public void onSectionSelected(int position);

    }

    public static class LogoutConfirmationDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Are you sure to logout?");
            builder.setNegativeButton("No", null);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ParseUser.logOut();
                    refreshUserProfile();
                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    try {
                        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences("preferences", getActivity().MODE_PRIVATE);
                        mSharedPreferences.edit().putBoolean("firstTimeUse", true);
                        Intent intent = new Intent(getActivity(), WelcomeScreenActivity.class);
                        startActivityForResult(intent, MainApplication.WLC_SCREEN_REQUEST);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return builder.create();
        }
    }

    public class NavDrawerItem {
        private int icon;
        private String title;

        public NavDrawerItem(String title, int icon) {
            this.title = title;
            this.icon = icon;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public class NavDrawerItemAdapter extends ArrayAdapter<NavDrawerItem> {

        private NavDrawerItemAdapter(Context context, int resource, List<NavDrawerItem> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View itemView = convertView;

            if (itemView == null) {
                itemView = mActivity.getLayoutInflater().inflate(R.layout.view_nav_drawer_item, parent, false);
            }

            NavDrawerItem currentItem = itemList.get(position);


            TextView currentNDItemTitle = (TextView) itemView.findViewById(R.id.nav_drawer_item_title);
            currentNDItemTitle.setText(currentItem.getTitle());

            ImageView currentNDItemIcon = (ImageView) itemView.findViewById(R.id.nav_drawer_item_icon);
            currentNDItemIcon.setImageResource(currentItem.getIcon());

            if (position == itemListLength - 1 && ParseUser.getCurrentUser() == null)
                itemView.setVisibility(View.GONE);
            else
                itemView.setVisibility(View.VISIBLE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (position == itemListLength - 1) {
                        DialogFragment logoutConfirmationDialog = new LogoutConfirmationDialog();
                        logoutConfirmationDialog.show(getFragmentManager(), "logoutConfirm");
                    } else {
                        mCallback.onSectionSelected(position);
                    }
                }
            });

            return itemView;
        }
    }
}
