package com.savesmart;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class UserProfileEditFragment extends Fragment {

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etBio;
    private RadioGroup rgGender;
    private RadioButton rbMale;
    private RadioButton rbFemale;
    private CircularImageView civProfilePic;
    private RelativeLayout rlProfilePicChooserLayout;
    private Button btnUpdateProfile;

    private String currentFirstName;
    private String currentLastName;
    private String currentBio;
    private String currentGender = "";

    private ParseFile newProfilePicFile;
    private Fragment mFragment;

    public UserProfileEditFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile_edit, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mFragment = this;

        try {
            ParseUser.getCurrentUser().getParseObject("userProfile").fetchIfNeeded();

            currentFirstName = ParseUser.getCurrentUser().getParseObject("userProfile").getString("firstName");
            currentLastName = ParseUser.getCurrentUser().getParseObject("userProfile").getString("lastName");
            currentBio = ParseUser.getCurrentUser().getParseObject("userProfile").getString("bio");
            if (ParseUser.getCurrentUser().getParseObject("userProfile").getString("gender") != null)
                currentGender = ParseUser.getCurrentUser().getParseObject("userProfile").getString("gender");

            etFirstName = (EditText) getActivity().findViewById(R.id.et_user_profile_edit_first_name);
            etLastName = (EditText) getActivity().findViewById(R.id.et_user_profile_edit_last_name);
            etBio = (EditText) getActivity().findViewById(R.id.et_user_profile_edit_bio);
            rbMale = (RadioButton) getActivity().findViewById(R.id.rb_user_profile_edit_male);
            rbFemale = (RadioButton) getActivity().findViewById(R.id.rb_user_profile_edit_female);
            civProfilePic = (CircularImageView) getActivity().findViewById(R.id.user_profile_edit_pic_chooser);
            rlProfilePicChooserLayout = (RelativeLayout) getActivity().findViewById(R.id.user_profile_edit_pic_chooser_layout);
            btnUpdateProfile = (Button) getActivity().findViewById(R.id.btn_user_profile_edit_update);
            rgGender = (RadioGroup) getActivity().findViewById(R.id.rg_user_profile_edit_gender);

            etFirstName.setText(currentFirstName);
            etLastName.setText(currentLastName);
            etBio.setText(currentBio);
            if (ParseUser.getCurrentUser().getParseObject("userProfile").getParseFile("profilePic") != null) {
                ParseUser.getCurrentUser().getParseObject("userProfile").getParseFile("profilePic").getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] bytes, ParseException e) {
                        if (e == null) {
                            Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            civProfilePic.setImageBitmap(bitmapImage);
                        } else {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            rlProfilePicChooserLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    startActivityForResult(Intent.createChooser(intent, "Select photo using"), MainApplication.PROFILE_PIC_CHOOSER_REQUEST);
                }
            });

            if (currentGender.equals("m")) {
                rbMale.setChecked(true);
            } else {
                rbFemale.setChecked(true);
            }

            btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String newFirstName = etFirstName.getText().toString();
                    String newLastName = etLastName.getText().toString();
                    String newBio = etBio.getText().toString();
                    String newGender = "";
                    boolean allow = true;

                    int checkedGenderID = rgGender.indexOfChild(getActivity().findViewById(rgGender.getCheckedRadioButtonId()));

                    if (checkedGenderID == -1) {
                        allow = false;
                    } else if (checkedGenderID == 0) {
                        newGender = "m";
                    } else {
                        newGender = "f";
                    }

                    if (newFirstName.equals("") || !newFirstName.matches("[\\p{L}\\p{Space}]*"))
                        etFirstName.setError("");
                    if (newLastName.equals("") || !newLastName.matches("[\\p{L}\\p{Space}]*"))
                        etLastName.setError("");

                    if (newFirstName.equals("") || newFirstName.equals("") || checkedGenderID == -1) {
                        Toast.makeText(getActivity(), R.string.error_field_empty, Toast.LENGTH_SHORT).show();
                        allow = false;
                    }
                    if (!newFirstName.matches("[\\p{L}\\p{Space}]*") || !newLastName.matches("[\\p{L}\\p{Space}]*")) {
                        Toast.makeText(getActivity(), R.string.error_invalid_character, Toast.LENGTH_SHORT).show();
                        allow = false;
                    }

                    if (allow) {
                        ParseUser.getCurrentUser().getParseObject("userProfile").put("firstName", newFirstName);
                        ParseUser.getCurrentUser().getParseObject("userProfile").put("lastName", newLastName);
                        ParseUser.getCurrentUser().getParseObject("userProfile").put("bio", newBio);
                        ParseUser.getCurrentUser().getParseObject("userProfile").put("gender", newGender);
                        if (newProfilePicFile != null)
                            ParseUser.getCurrentUser().getParseObject("userProfile").put("profilePic", newProfilePicFile);
                        ParseUser.getCurrentUser().getParseObject("userProfile").saveEventually(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    try {
                                        Toast.makeText(getActivity(), R.string.user_profile_edit_update_success, Toast.LENGTH_SHORT).show();
                                        if (mFragment.isVisible())
                                            getFragmentManager().popBackStackImmediate();
                                        //getFragmentManager().beginTransaction().remove(mFragment).commit();
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(getActivity(), R.string.user_profile_edit_update_failed, Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                        Toast.makeText(getActivity(), R.string.user_profile_edit_updating, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MainApplication.PROFILE_PIC_CHOOSER_REQUEST) {
                Uri selectedImage = data.getData();
                if (selectedImage != null) {
                    try {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                        InputStream inputStream = getActivity().getContentResolver().openInputStream(data.getData());
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        if (bitmap.getHeight() > 512) {
                            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 512.0 / bitmap.getHeight()), 512, false);
                        } else if (bitmap.getWidth() > 512) {
                            bitmap = Bitmap.createScaledBitmap(bitmap, 512, (int) (bitmap.getHeight() * 512.0 / bitmap.getWidth()), false);
                        }

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                        inputStream.close();
                        outputStream.close();

                        newProfilePicFile = new ParseFile(ParseUser.getCurrentUser().getObjectId() + ".jpg", outputStream.toByteArray());
                        newProfilePicFile.saveInBackground();

                        if (civProfilePic != null)
                            civProfilePic.setImageBitmap(bitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

/*
                            pf = new ParseFile(userProfile.getObjectId() + ".jpg", outputStream.toByteArray());
                            userProfile.put("profilePic", pf);
                            userProfile.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null)
                                        Toast.makeText(getActivity(), "DONE", Toast.LENGTH_SHORT).show();
                                    else {
                                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        System.out.println(e.getMessage());
                                    }
                                }
                            });*/