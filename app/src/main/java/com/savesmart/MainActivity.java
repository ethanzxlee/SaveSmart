package com.savesmart;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseAnalytics;
import com.parse.ParseUser;

import java.util.ArrayList;

public class MainActivity extends Activity implements NavigationDrawerFragment.OnNavDrawerItemSelectedListener {

    public ProductListFragment homeFragment;
    public BrowseFragment browseFragment;
    public SavedListsFragment savedListsFragment;
    public ProductListingFragment favouriteFragment;
    public ProductListingFragment historyFragment;
    public SettingsFragment settingsFragment;
    public UserProfileFragment userProfileFragment;
    public String currentFragmentTitle;
    protected Location lastKnownLocation;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean mFinish = false;
    private boolean mMenuButtonVisible = true;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private String voiceResult;
    private SearchView sv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ParseAnalytics.trackAppOpened(getIntent());


        SharedPreferences mSharedPreferences = getSharedPreferences("preferences", MODE_PRIVATE);
        if (mSharedPreferences.getBoolean("firstTimeUse", true)) {
            Intent intent = new Intent(this, WelcomeScreenActivity.class);
            startActivityForResult(intent, MainApplication.WLC_SCREEN_REQUEST);
        }

        setContentView(R.layout.activity_main);
        determineLocation();
        fm = getFragmentManager();
        ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        if (savedInstanceState == null) {
            homeFragment = new ProductListFragment();

            Bundle bundle = new Bundle();
            bundle.putBoolean("isHome", true);
            homeFragment.setArguments(bundle);

            ft.add(R.id.main_container, homeFragment).commit();
            currentFragmentTitle = getResources().getStringArray(R.array.nav_drawer_item_title)[0];
            getActionBar().setTitle(currentFragmentTitle);
        }


        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    float alpha;
                    if (slideOffset <= 0.5f) {
                        getActionBar().setTitle(currentFragmentTitle);
                        alpha = 1 - (slideOffset * 2);
                    } else {
                        getActionBar().setTitle(R.string.app_name);
                        alpha = (slideOffset - 0.5f) * 2;
                    }
                    try {
                        int titleId;
                        titleId = getResources().getIdentifier("action_bar_title", "id", "android");
                        ((TextView) findViewById(titleId)).setAlpha(alpha);

                    } catch (Exception e) {
                    }
                }
            }

            @Override
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(currentFragmentTitle);
                mMenuButtonVisible = true;
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(R.string.app_name);
                mMenuButtonVisible = false;
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setTintColor(getResources().getColor(R.color.themeColor));

            // Set padding of the navigation drawer
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                ListView navDrawer = (ListView) findViewById(R.id.nav_drawer_list_layout);
                RelativeLayout.LayoutParams navDrawerParams = (RelativeLayout.LayoutParams) navDrawer.getLayoutParams();
                navDrawerParams.setMargins(0, actionBarHeight + getStatusBarHeight(), 0, 0);
                FrameLayout mainContainer = (FrameLayout) findViewById(R.id.main_container);
                mainContainer.setPadding(0, actionBarHeight + getStatusBarHeight(), 0, 0);
            }
        }
*/
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }

        if(RecognizerIntent.ACTION_RECOGNIZE_SPEECH.equals(intent.getAction())){
            startActivityForResult(intent,MainApplication.RECOGNIZER_REQUEST);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(mMenuButtonVisible);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainApplication.WLC_SCREEN_REQUEST) {
            if (resultCode == MainApplication.STATUS_CANCELLED) {
                mFinish = true;
            } else if (resultCode == MainApplication.STATUS_SUCCESS || resultCode == MainApplication.STATUS_SKIPPED) {
                mFinish = false;
            }
        } else if (requestCode == MainApplication.USER_LOGIN_REQUEST) {
            if (resultCode == MainApplication.STATUS_CANCELLED) {
                mFinish = false;
            }
        } else if(requestCode == MainApplication.RECOGNIZER_REQUEST && resultCode == RESULT_OK){
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            voiceResult = result.get(0).toString();
            doMySearch(voiceResult);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        sv = searchView;
        searchView.setQueryHint(getResources().getString(R.string.search_hint));

        int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) searchView.findViewById(searchSrcTextId);
        searchEditText.setTextColor(getResources().getColor(R.color.brightTextColor));
        searchEditText.setHintTextColor(getResources().getColor(R.color.brightTextColor));

        int closeButtonId = getResources().getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeButtonImage = (ImageView) searchView.findViewById(closeButtonId);
        closeButtonImage.setImageResource(R.drawable.ic_action_cancel);

        int voiceButtonId = getResources().getIdentifier("android:id/search_voice_btn", null, null);
        ImageView voiceButtonImage = (ImageView) searchView.findViewById(voiceButtonId);
        voiceButtonImage.setImageResource(R.drawable.ic_action_mic);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));


        return super.onCreateOptionsMenu(menu);
    }

    public void doMySearch(String query){
        ProductListingFragment productResultFragment = new ProductListingFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isSearch",true);
        bundle.putString("searchTerm",query);
        productResultFragment.setArguments(bundle);

        ft = getFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        ft.replace(R.id.main_container, productResultFragment, "productResultFragment").addToBackStack(null).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentFragmentTitle", currentFragmentTitle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentFragmentTitle = savedInstanceState.getString("currentFragmentTitle");
        if (!mDrawerLayout.isDrawerOpen(Gravity.START))
            getActionBar().setTitle(currentFragmentTitle);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mFinish)
            finish();
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(this, AddProductActivity.class);
                startActivityForResult(intent,MainApplication.USER_LOGIN_REQUEST);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void btnNavDrawerLoginOnClick(View view) {
        Intent intent = new Intent(this, UserLoginActivity.class);
        startActivityForResult(intent, MainApplication.USER_LOGIN_REQUEST);
    }

    @Override
    public void onSectionSelected(int position) {
        ft = getFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        if (position == 0) {
            if (homeFragment == null) {
                homeFragment = new ProductListFragment();
            }
            if (!homeFragment.isVisible()) {
                ft.replace(R.id.main_container, homeFragment, "homeFragment").addToBackStack(null).commit();
            }
        } else if (position == 1) {
            if (browseFragment == null) {
                browseFragment = new BrowseFragment();
            }
            if (!browseFragment.isVisible()) {
                ft.replace(R.id.main_container, browseFragment, "browseFragment").addToBackStack(null).commit();
            }
        } else if (position == 2) {
            if (savedListsFragment == null) {
                if (ParseUser.getCurrentUser() == null) {
                    Intent intent = new Intent(this, WelcomeScreenActivity.class);
                    startActivityForResult(intent, MainApplication.USER_LOGIN_REQUEST);
                } else {
                    savedListsFragment = new SavedListsFragment();
                }
            }
            if (ParseUser.getCurrentUser() != null && !savedListsFragment.isVisible()) {
                ft.replace(R.id.main_container, savedListsFragment, "savedListsFragment").addToBackStack(null).commit();
            }
        } else if (position == 3) {
            if (favouriteFragment == null) {
                if (ParseUser.getCurrentUser() == null) {
                    Intent intent = new Intent(this, WelcomeScreenActivity.class);
                    startActivityForResult(intent, MainApplication.USER_LOGIN_REQUEST);
                } else {
                    favouriteFragment = new ProductListingFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isFavourite", true);
                    favouriteFragment.setArguments(bundle);
                }
            }
            if (ParseUser.getCurrentUser() != null && !favouriteFragment.isVisible()) {
                ft.replace(R.id.main_container, favouriteFragment, "favouriteFragment").addToBackStack(null).commit();
            }
        } else if (position == 4) {
            if (historyFragment == null) {
                if (ParseUser.getCurrentUser() == null) {
                    Intent intent = new Intent(this, WelcomeScreenActivity.class);
                    startActivityForResult(intent, MainApplication.USER_LOGIN_REQUEST);
                } else {
                    historyFragment = new ProductListingFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isHistory", true);
                    historyFragment.setArguments(bundle);
                }
            }
            if (ParseUser.getCurrentUser() != null && !historyFragment.isVisible()) {
                ft.replace(R.id.main_container, historyFragment, "historyFragment").addToBackStack(null).commit();
            }
        } else if (position == 5) {
            if (settingsFragment == null) {
                settingsFragment = new SettingsFragment();
            }
            if (!settingsFragment.isVisible()) {
                ft.replace(R.id.main_container, settingsFragment, "settingsFragment").addToBackStack(null).commit();
            }
        }
        mDrawerLayout.closeDrawers();
    }

    public void StartUserProfileActivityOnClick(View view) {
        if (userProfileFragment == null) {
            userProfileFragment = new UserProfileFragment();
        }
        if (!userProfileFragment.isVisible()) {
            ft = getFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.replace(R.id.main_container, userProfileFragment, "userProfileFragment").addToBackStack(null).commit();
        }
        mDrawerLayout.closeDrawers();
    }

    public void determineLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastKnownLocation = location;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        String bestProvider = locationManager.getBestProvider(criteria, true);

        if (bestProvider == null) {
            Toast.makeText(this, "Please turn on location services to continue", Toast.LENGTH_LONG).show();
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            this.startActivity(myIntent);
        } else {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastKnownLocation == null) {
                Toast.makeText(this, "Please turn on location services to continue", Toast.LENGTH_LONG).show();
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                this.startActivity(myIntent);
            }
            //if (lastKnownLocation == null) {
            locationManager.requestLocationUpdates(locationManager.getBestProvider(criteria, true), 300000, 10000, locationListener);
            //}
        }
    }

}
