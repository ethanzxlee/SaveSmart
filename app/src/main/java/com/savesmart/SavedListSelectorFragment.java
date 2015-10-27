package com.savesmart;


import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class SavedListSelectorFragment extends Fragment {

    protected RelativeLayout listSelectorMainContainer;
    protected View backgroundMask;
    protected Fragment mFragment;
    protected Button btnCreateList;
    protected ListView lvListSelector;
    protected ListSelectorAdapter listSelectorAdapter;

    public SavedListSelectorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved_list_selector, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            mFragment = this;

            getView().setFocusableInTouchMode(true);
            getView().requestFocus();
            getView().setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (i == KeyEvent.KEYCODE_BACK) {
                        try {
                            onBeforePause();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            listSelectorMainContainer = (RelativeLayout) getActivity().findViewById(R.id.list_selector_main_container);
            backgroundMask = getActivity().findViewById(R.id.list_selector_background_mask);

            backgroundMask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBeforePause();
                }
            });

            View listSelectorCreateList = getActivity().getLayoutInflater().inflate(R.layout.fragment_list_selector_item, null);
            listSelectorCreateList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CreateSavedListDialog createSavedListDialog = new CreateSavedListDialog(mFragment);
                    createSavedListDialog.show(getFragmentManager(), "createList");
                }
            });
            TextView tvCreateList = (TextView) listSelectorCreateList.findViewById(R.id.list_selector_item_name);
            tvCreateList.setText("Create a list");
            tvCreateList.setTextSize(18);
            ImageView ivItemIcon = (ImageView) listSelectorCreateList.findViewById(R.id.list_selector_item_icon);
            ivItemIcon.setVisibility(View.VISIBLE);

            listSelectorAdapter = new ListSelectorAdapter(getActivity(), R.layout.fragment_home_listview_item, R.id.list_selector_fake_text);
            lvListSelector = (ListView) getActivity().findViewById(R.id.list_selector_list_view);
            lvListSelector.addHeaderView(listSelectorCreateList);
            lvListSelector.setAdapter(listSelectorAdapter);
            backgroundMask.animate()
                    .alpha(1)
                    .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            listSelectorMainContainer.animate()
                                    .setInterpolator(new AccelerateDecelerateInterpolator())
                                    .translationY(0)
                                    .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                                    .setListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animator) {
                                            getSavedListName();
                                        }

                                        @Override
                                        public void onAnimationCancel(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationRepeat(Animator animator) {

                                        }
                                    })
                                    .start();
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    })
                    .start();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void onBeforePause() {
        listSelectorMainContainer.animate()
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .translationY(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 325, getResources().getDisplayMetrics()))
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        try {
                            backgroundMask.animate()
                                    .alpha(0)
                                    .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                                    .setListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animator) {
                                            try {
                                                getFragmentManager().popBackStackImmediate();
                                            } catch (Exception e) {
                                                //e.printStackTrace();
                                            }
                                            //mFragment.onPause();
                                        }

                                        @Override
                                        public void onAnimationCancel(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationRepeat(Animator animator) {

                                        }
                                    })
                                    .start();
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                })
                .start();
    }

    public void getSavedListName() {
        ParseQuery<ParseObject> listNameQuery = ParseQuery.getQuery("SavedLists");
        listNameQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        listNameQuery.orderByDescending("createdAt");
        listNameQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    listSelectorAdapter.clear();
                    listSelectorAdapter.addAll(parseObjects);
                    listSelectorAdapter.notifyDataSetChanged();
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    public static class CreateSavedListDialog extends DialogFragment {

        private EditText etListName;
        private Fragment mSavedListsFragment;

        public CreateSavedListDialog() {
            super();
        }

        @SuppressLint("ValidFragment")
        public CreateSavedListDialog(Fragment savedListsFragments) {
            super();
            mSavedListsFragment = savedListsFragments;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final View view = getActivity().getLayoutInflater().inflate(R.layout.popup_save_lists_create, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("Create a list");
            builder.setView(view);
            builder.setPositiveButton(R.string.action_ok, null);
            etListName = (EditText) view.findViewById(R.id.et_saved_list_create_name);
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
                        if (etListName.getText().toString().equals("")) {
                            Toast.makeText(getActivity(), R.string.error_field_empty, Toast.LENGTH_SHORT).show();
                            etListName.setError("");
                        } else {
                            try {
                                ParseACL savedListACL = new ParseACL();
                                savedListACL.setReadAccess(ParseUser.getCurrentUser(), true);
                                savedListACL.setWriteAccess(ParseUser.getCurrentUser(), true);

                                ParseObject savedList = new ParseObject("SavedLists");
                                savedList.put("listName", etListName.getText().toString());
                                savedList.put("listOwner", ParseUser.getCurrentUser());
                                savedList.put("currentSize", 0);
                                savedList.setACL(savedListACL);
                                savedList.saveEventually(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            //mSavedListsFragment.loadSavedListCards();
                                        } else {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                Toast.makeText(getActivity(), R.string.saved_list_created_success, Toast.LENGTH_SHORT).show();
                                //mSavedListsFragment.loadSavedListCards();
                                alertDialog.cancel();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }

    }

    public class ListSelectorViewHolder {
        protected TextView tvListName;
    }

    public class ListSelectorAdapter extends ArrayAdapter<ParseObject> {

        protected ListSelectorViewHolder listSelectorViewHolder;

        public ListSelectorAdapter(Context context, int resource) {
            super(context, resource);
        }

        public ListSelectorAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.fragment_list_selector_item, parent, false);
                listSelectorViewHolder = new ListSelectorViewHolder();
                listSelectorViewHolder.tvListName = (TextView) convertView.findViewById(R.id.list_selector_item_name);
                convertView.setTag(listSelectorViewHolder);
            } else {
                listSelectorViewHolder = (ListSelectorViewHolder) convertView.getTag();
            }

            listSelectorViewHolder.tvListName.setText(getItem(position).getString("listName"));

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ParseObject savedListsItem = new ParseObject("SavedListsItem");
                    savedListsItem.put("savedList", getItem(position));
                    savedListsItem.put("product", ParseObject.createWithoutData("Product", getArguments().getString("productObjectId")));
                    savedListsItem.saveEventually();
                    Toast.makeText(getContext(), getArguments().getString("productObjectId"), Toast.LENGTH_SHORT).show();
                    onBeforePause();
                }
            });

            return super.getView(position, convertView, parent);
        }
    }
}
