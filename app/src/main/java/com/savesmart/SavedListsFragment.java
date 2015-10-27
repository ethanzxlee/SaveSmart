package com.savesmart;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;


public class SavedListsFragment extends Fragment {

    private static SavedListsFragment mFragment;
    private String mTitle;
    private ArrayList<Card> savedListCards;
    private CardArrayAdapter savedListCardsArrayAdapter;
    private CardListView savedListCardsListView;
    private SwingBottomInAnimationAdapter animCardArrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mTitle = getResources().getStringArray(R.array.nav_drawer_item_title)[2];
        mFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved_lists, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            ((MainActivity) getActivity()).currentFragmentTitle = mTitle;
            ((MainActivity) getActivity()).getActionBar().setTitle(mTitle);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.saved_lists, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_saved_lists_new) {
            CreateSavedListDialog createSavedListDialog = new CreateSavedListDialog(this);
            createSavedListDialog.show(getFragmentManager(), "createList");
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        savedListCards = new ArrayList<Card>();
        savedListCardsArrayAdapter = new CardArrayAdapter(getActivity(), savedListCards);
        savedListCardsListView = (CardListView) getActivity().findViewById(R.id.saved_list_card_list_view);

        if (savedListCardsListView != null) {
            savedListCardsListView.setAdapter(savedListCardsArrayAdapter);
            animCardArrayAdapter = new SwingBottomInAnimationAdapter(savedListCardsArrayAdapter);
            animCardArrayAdapter.setAnimationDurationMillis(500);
            animCardArrayAdapter.setAbsListView(savedListCardsListView);

            savedListCardsListView.setExternalAdapter(animCardArrayAdapter, savedListCardsArrayAdapter);
        }

        loadSavedListCards();

    }

    protected void loadSavedListCards() {

        ParseQuery<ParseObject> savedListQuery = ParseQuery.getQuery("SavedLists");
        savedListQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        savedListQuery.orderByDescending("createdAt");
        savedListQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                try {
                    if (e == null) {
                        savedListCards.clear();

                        TextView tvSavedListEmpty = (TextView) getActivity().findViewById(R.id.tv_saved_list_empty);

                        if (list.size() == 0) {
                            tvSavedListEmpty.setVisibility(View.VISIBLE);
                        } else {
                            tvSavedListEmpty.setVisibility(View.GONE);
                        }

                        for (int i = 0; i < list.size(); i++) {
                            Card savedListCard = new SavedListsCard(getActivity().getBaseContext(), list.get(i));
                            savedListCard.setOnClickListener(new Card.OnCardClickListener() {
                                @Override
                                public void onClick(Card card, View view) {
                                    ProductListingFragment productListingFragment = new ProductListingFragment();
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                                    ft.replace(R.id.main_container, productListingFragment).addToBackStack(null).commit();
                                }
                            });
                            savedListCards.add(savedListCard);

                            if (animCardArrayAdapter != null)
                                animCardArrayAdapter.notifyDataSetChanged();

                        }
                    } else {
                        e.printStackTrace();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static class CreateSavedListDialog extends DialogFragment {

        private EditText etListName;
        private SavedListsFragment mSavedListsFragment;

        public CreateSavedListDialog() {
            super();
        }

        @SuppressLint("ValidFragment")
        public CreateSavedListDialog(SavedListsFragment savedListsFragments) {
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
                                            mSavedListsFragment.loadSavedListCards();
                                        } else {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                Toast.makeText(getActivity(), R.string.saved_list_created_success, Toast.LENGTH_SHORT).show();
                                mSavedListsFragment.loadSavedListCards();
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

    public static class DeleteSavedListDialog extends DialogFragment {

        private EditText etListName;
        private ParseObject mSavedListsObject;

        public DeleteSavedListDialog() {
            super();
        }

        @SuppressLint("ValidFragment")
        public DeleteSavedListDialog(ParseObject savedListObject) {
            super();
            mSavedListsObject = savedListObject;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("Delete list ?");
            builder.setPositiveButton(R.string.action_ok, null);
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
                        mSavedListsObject.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                try {
                                    ((SavedListsFragment) mFragment).loadSavedListCards();
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        });
                        alertDialog.cancel();
                    }
                });
            }
        }

    }

    public static class RenameSavedListDialog extends DialogFragment {

        private EditText etListName;
        private SavedListsFragment mSavedListsFragment;
        private ParseObject mSavedListsObject;

        public RenameSavedListDialog() {
            super();
        }

        @SuppressLint("ValidFragment")
        public RenameSavedListDialog(SavedListsFragment savedListsFragments, ParseObject savedListObject) {
            super();
            mSavedListsFragment = savedListsFragments;
            mSavedListsObject = savedListObject;

        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final View view = getActivity().getLayoutInflater().inflate(R.layout.popup_save_lists_rename, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle("Rename list");
            builder.setView(view);
            builder.setPositiveButton(R.string.action_ok, null);
            etListName = (EditText) view.findViewById(R.id.et_saved_list_rename_name);
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
                                mSavedListsObject.put("listName", etListName.getText().toString());
                                mSavedListsObject.saveEventually(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        mSavedListsFragment.loadSavedListCards();
                                    }
                                });
                                mSavedListsFragment.loadSavedListCards();
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

    public class SavedListsCard extends Card {

        private ParseObject mSavedListObject;
        private boolean isFlipped = false;

        public SavedListsCard(Context context) {
            this(context, R.layout.card_layout_saved_list_item);
        }

        public SavedListsCard(Context context, ParseObject saveListObject) {
            this(context, R.layout.card_layout_saved_list_item);
            mSavedListObject = saveListObject;
        }

        public SavedListsCard(Context context, int innerLayout) {
            super(context, innerLayout);
        }

        @Override
        public void setOnClickListener(OnCardClickListener onClickListener) {
            OnCardClickListener onCardClickListener = new Card.OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    if (!isFlipped) {
                        ProductListingFragment productListingFragment = new ProductListingFragment();

                        Bundle bundle = new Bundle();
                        bundle.putBoolean("isSavedList", true);
                        bundle.putCharSequence("savedList", mSavedListObject.getObjectId());
                        bundle.putCharSequence("savedListName", mSavedListObject.getString("listName"));
                        productListingFragment.setArguments(bundle);

                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        ft.replace(R.id.main_container, productListingFragment).addToBackStack(null).commit();
                    }
                }
            };
            super.setOnClickListener(onCardClickListener);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            try {
                TextView tvListName = (TextView) view.findViewById(R.id.saved_list_card_list_name);
                final TextView tvItemCount = (TextView) view.findViewById(R.id.saved_list_card_item_count);

                tvListName.setText(mSavedListObject.getString("listName"));
                tvItemCount.setText(Integer.toString(mSavedListObject.getInt("currentSize")) + " items");

                ImageView ivOverflow = (ImageView) view.findViewById(R.id.saved_list_card_item_overflow);

                final RelativeLayout defaultLayout = (RelativeLayout) view.findViewById(R.id.saved_list_card_default_layout);
                final RelativeLayout flippedLayout = (RelativeLayout) view.findViewById(R.id.saved_list_card_flipped_layout);

                Button btnDeleteList = (Button) view.findViewById(R.id.saved_list_card_item_delete_list);
                Button btnRenameList = (Button) view.findViewById(R.id.saved_list_card_item_rename_list);


                ivOverflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isFlipped = !isFlipped;
                        if (isFlipped) {
                            defaultLayout.setVisibility(View.GONE);
                            flippedLayout.setVisibility(View.VISIBLE);
                        } else {
                            defaultLayout.setVisibility(View.VISIBLE);
                            flippedLayout.setVisibility(View.GONE);
                        }
                    }
                });

                btnDeleteList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DeleteSavedListDialog deleteSavedListDialog = new DeleteSavedListDialog(mSavedListObject);
                        deleteSavedListDialog.show(getFragmentManager(), "deleteList");
                        defaultLayout.setVisibility(View.VISIBLE);
                        flippedLayout.setVisibility(View.GONE);
                    }
                });

                btnRenameList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        RenameSavedListDialog renameSavedListDialog = new RenameSavedListDialog(mFragment, mSavedListObject);
                        renameSavedListDialog.show(getFragmentManager(), "renameList");
                        defaultLayout.setVisibility(View.VISIBLE);
                        flippedLayout.setVisibility(View.GONE);
                    }
                });

                /*ImageView ivPhoto0 = (ImageView) view.findViewById(R.id.saved_list_card_item_photo_0);
                ImageView ivPhoto1 = (ImageView) view.findViewById(R.id.saved_list_card_item_photo_1);
                ImageView ivPhoto2 = (ImageView) view.findViewById(R.id.saved_list_card_item_photo_2);

                final ArrayList<ImageView> ivPhotoList = new ArrayList<ImageView>();
                ivPhotoList.add(ivPhoto0);
                ivPhotoList.add(ivPhoto1);
                ivPhotoList.add(ivPhoto2);*/
/*
                tvListName.setText(mSavedListObject.getString("listName"));

                HashMap<String, String> params = new HashMap<String, String>();
                params.put("savedList", mSavedListObject.getObjectId());

                ParseCloud.callFunctionInBackground("GetSavedListsInfo", params, new FunctionCallback<HashMap>() {
                    @Override
                    public void done(HashMap savedListInfo, ParseException e) {
                        try {
                            tvItemCount.setText(Integer.toString((Integer) savedListInfo.get("itemCount")) + " items");
/*
                            ArrayList<ParseFile> productPhotoFiles = (ArrayList<ParseFile>) savedListInfo.get("photo");

                            int finalI = 0;

                            for (int i = 0; i < 3; i++) {
                                if (i < productPhotoFiles.size()) {
                                    byte[] bytes = productPhotoFiles.get(i).getData();
                                    Bitmap productPhotoBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    ivPhotoList.get(i).setImageBitmap(productPhotoBitmap);
                                }
                                else {
                                    ivPhotoList.get(i).setImageBitmap(null);
                                }
                            }
*/
/*
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });*/

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
