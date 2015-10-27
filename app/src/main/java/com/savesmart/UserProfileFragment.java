package com.savesmart;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


public class UserProfileFragment extends Fragment {

    final private ArrayList<Card> activityFeedCardArrayList = new ArrayList<Card>();
    protected CardListView activityFeedListView = null;
    protected CardArrayAdapter activityFeedCardArrayAdapter;
    protected PullToRefreshLayout userProfilePTRView;
    private ViewGroup container;
    private AnimationAdapter animCardArrayAdapter;
    private ParseQuery<ParseObject> userActionLogQuery;
    private int currentFeedCount = 5;
    private boolean isQuerying = false;
    private String mTitle;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.container = container;
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTitle = "Profile";
        userProfilePTRView = (PullToRefreshLayout) getActivity().findViewById(R.id.user_profile_pull_to_refresh_layout);

        ActionBarPullToRefresh.from(this.getActivity()).allChildrenArePullable().listener(new OnRefreshListener() {
            @Override
            public void onRefreshStarted(View view) {
                currentFeedCount = 5;
                try {
                    refreshProfileHeader();
                    refreshActivityFeed();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).setup(userProfilePTRView);

        activityFeedCardArrayAdapter = new CardArrayAdapter(getActivity(), activityFeedCardArrayList);

        activityFeedListView = (CardListView) getActivity().findViewById(R.id.user_profile_activity_feed_list);

        if (activityFeedListView != null) {
            activityFeedListView.addHeaderView(getActivity().getLayoutInflater().inflate(R.layout.fragment_user_profile_header, null), null, false);
            activityFeedListView.addFooterView(getActivity().getLayoutInflater().inflate(R.layout.listview_footer_loading, null), null, false);
            activityFeedListView.setDivider(null);

            animCardArrayAdapter = new SwingBottomInAnimationAdapter(activityFeedCardArrayAdapter);
            animCardArrayAdapter.setAnimationDurationMillis(500);
            animCardArrayAdapter.setAbsListView(activityFeedListView);

            activityFeedListView.setExternalAdapter(animCardArrayAdapter, activityFeedCardArrayAdapter);
            activityFeedListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                int scrollState;
                @Override
                public void onScrollStateChanged(AbsListView absListView, int i) {
                    if (i != SCROLL_STATE_TOUCH_SCROLL && !isQuerying)
                        try {
                            refreshActivityFeed();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    ;
                }

                @Override
                public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    /*if (scrollState == SCROLL_STATE_TOUCH_SCROLL ) {
                        if (totalItemCount - (firstVisibleItem + visibleItemCount) < 3 && !isQuerying) {
                        }
                    }*/

                }
            });
        }


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.user_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_user_profile_edit) {
            UserProfileEditFragment userProfileEditFragment = new UserProfileEditFragment();
            getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack(null).replace(R.id.main_container, userProfileEditFragment).commit();

            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            refreshActivityFeed();
            refreshProfileHeader();
            ((MainActivity) getActivity()).currentFragmentTitle = mTitle;
            if (((MainActivity) getActivity()).getActionBar().getTitle() != getString(R.string.app_name))
                ((MainActivity) getActivity()).getActionBar().setTitle(mTitle);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void refreshProfileHeader() {
        ParseUser.getCurrentUser().getParseObject("userProfile").fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                try {
                    if (e == null) {
                        final TextView userFirstName = (TextView) getActivity().findViewById(R.id.tv_user_profile_first_name);
                        if (userFirstName != null) {
                            userFirstName.setText(parseObject.getString("firstName") + " " + parseObject.getString("lastName"));
                        }

                        final CircularImageView circularProfilePicture = (CircularImageView) getActivity().findViewById(R.id.user_profile_circular_profile_pic);
                        if (circularProfilePicture != null) {
                            //circularProfilePicture.borderWidth = 12;
                            ParseFile profilePic = parseObject.getParseFile("profilePic");
                            if (profilePic != null) {
                                profilePic.getDataInBackground(new GetDataCallback() {
                                    @Override
                                    public void done(byte[] bytes, ParseException e) {
                                        if (e == null) {
                                            Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            circularProfilePicture.setImageBitmap(bitmapImage);
                                        } else {
                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            }
                        }
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        final ProgressBar levelProgressBar = (ProgressBar) getActivity().findViewById(R.id.level_progress);
        final TextView levelCategory = (TextView) getActivity().findViewById(R.id.tv_user_profile_level_category);
        if (levelProgressBar != null && levelCategory != null) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                RelativeLayout topLayout = (RelativeLayout) getActivity().findViewById(R.id.user_profile_top_layout);
                topLayout.setPadding(topLayout.getPaddingLeft(), topLayout.getPaddingTop(), topLayout.getPaddingRight(), getResources().getDimensionPixelSize(R.dimen.default_gap));
            }

            ParseCloud.callFunctionInBackground("UserCurrentPoints", new HashMap<String, Object>(), new FunctionCallback<Integer>() {
                @Override
                public void done(final Integer points, ParseException e) {
                    ParseQuery<ParseObject> userLevelQuery = ParseQuery.getQuery("UserLevel");
                    userLevelQuery.whereLessThanOrEqualTo("minPoint", points);
                    userLevelQuery.whereGreaterThanOrEqualTo("maxPoint", points);
                    userLevelQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if (e == null) {
                                int level = parseObject.getInt("level");
                                levelCategory.setText("LEVEL " + level);
                                levelProgressBar.setProgress((int) ((points - parseObject.getInt("minPoint"))));
                            }
                        }
                    });
                }
            });
        }
    }

    protected void refreshActivityFeed() {
        isQuerying = true;

        HashMap params = new HashMap<String, Object>();
        params.put("currentFeedCount", currentFeedCount);
        params.put("locale", "en-US");

        ParseCloud.callFunctionInBackground("UserActivityFeed", params, new FunctionCallback<ArrayList<HashMap<String, Object>>>() {
            @Override
            public void done(ArrayList<HashMap<String, Object>> activityFeeds, ParseException e) {
                if (e == null) {
                    isQuerying = false;
                    try {
                        activityFeedCardArrayList.clear();
                        for (int i = 0; i < activityFeeds.size(); i++) {
                            String actionDesc = (String) activityFeeds.get(i).get("actionDesc");
                            String actionTitle = (String) activityFeeds.get(i).get("actionTitle");
                            String authorName = (String) activityFeeds.get(i).get("authorName");
                            ParseFile authorPic = (ParseFile) activityFeeds.get(i).get("authorPic");
                            Date date = (Date) activityFeeds.get(i).get("date");
                            String feedDate = date.toString();

                            ActivityFeedCard activityFeedCard = new ActivityFeedCard(getActivity().getBaseContext(), feedDate, authorName, actionTitle, actionDesc, authorPic);
                            if (activityFeedCardArrayAdapter != null)
                                activityFeedCardArrayList.add(activityFeedCard);
                        }

                        if (animCardArrayAdapter != null)
                            animCardArrayAdapter.notifyDataSetChanged();

                        if (userProfilePTRView != null && userProfilePTRView.isRefreshing())
                            userProfilePTRView.setRefreshComplete();

                        ProgressBar loadingProgressBar = (ProgressBar) getActivity().findViewById(R.id.listview_footer_loading_progressbar);
                        TextView loadingTextView = (TextView) getActivity().findViewById(R.id.listview_footer_loading_textview);

                        if (loadingProgressBar != null && loadingTextView != null) {
                            if (activityFeeds.size() == 0) {
                                loadingProgressBar.setVisibility(View.GONE);
                                loadingTextView.setText("Nothing's here.");
                                loadingTextView.setVisibility(View.VISIBLE);
                            } else if (activityFeeds.size() < currentFeedCount - 1) {
                                loadingProgressBar.setVisibility(View.GONE);
                                loadingTextView.setVisibility(View.GONE);
                            } else {
                                loadingProgressBar.setVisibility(View.VISIBLE);
                                loadingTextView.setVisibility(View.GONE);
                            }
                        }
                        currentFeedCount += 5;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onPause() {
        try {
            if (userActionLogQuery != null)
                userActionLogQuery.cancel();
        } catch (Exception e) {
        }
        super.onPause();
    }

    public class ActivityFeedCard extends Card {

        ParseFile authorPic;
        String date;
        String authorName;
        String contentTitle;
        String contentDesc;

        public ActivityFeedCard(Context context, String date, String authorName, String contentTitle, String contentDesc, ParseFile authorPic) {
            this(context, R.layout.card_layout_avtivity_feed);
            this.date = date;
            this.authorName = authorName;
            this.contentTitle = contentTitle;
            this.contentDesc = contentDesc;
            this.authorPic = authorPic;
        }

        public ActivityFeedCard(Context context) {
            this(context, R.layout.card_layout_avtivity_feed);
        }

        public ActivityFeedCard(Context context, int innerLayout) {
            super(context, innerLayout);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, final View view) {
            try {
                TextView feedDate = (TextView) view.findViewById(R.id.activity_feed_date);
                feedDate.setText(date);

                TextView authorText = (TextView) view.findViewById(R.id.activity_feed_author_name);
                authorText.setText(authorName);

                TextView contentTitle = (TextView) view.findViewById(R.id.activity_feed_action_title);
                TextView contentDesc = (TextView) view.findViewById(R.id.activity_feed_action_desc);

                contentTitle.setText(this.contentTitle);
                contentDesc.setText(this.contentDesc);

                if (contentTitle.getText().toString().equals(""))
                    contentTitle.setVisibility(View.GONE);
                else
                    contentTitle.setVisibility(View.VISIBLE);

                authorPic.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] bytes, ParseException e) {
                        if (e == null) {
                            try {
                                CircularImageView authorPicView = (CircularImageView) view.findViewById(R.id.activity_feed_author_pic);
                                Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                authorPicView.setImageBitmap(bitmapImage);
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}

