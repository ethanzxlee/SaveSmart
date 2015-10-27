package com.savesmart;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;


public class UserSignUpActivity extends Activity {

    protected View.OnFocusChangeListener emptyCheckingListener;
    private Typeface customFont;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etPassword;
    private EditText etEmail;
    private Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_up);
        getActionBar().hide();

        final ScrollView svSignUp = (ScrollView) findViewById(R.id.sv_sign_up);
        btnSignUp = (Button) findViewById(R.id.btn_sign_up_sign_up);

        emptyCheckingListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b)
                    svSignUp.smoothScrollTo(0, svSignUp.getBottom());
                if (!b && ((TextView) view).getText().toString().equals(""))
                    ((TextView) view).setError("");
                else
                    ((TextView) view).setError(null);
            }
        };

        etFirstName = (EditText) findViewById(R.id.et_sign_up_first_name);
        etLastName = (EditText) findViewById(R.id.et_sign_up_last_name);
        etPassword = (EditText) findViewById(R.id.et_sign_up_password);
        etEmail = (EditText) findViewById(R.id.et_sign_up_email);

        etFirstName.setOnFocusChangeListener(emptyCheckingListener);
        etLastName.setOnFocusChangeListener(emptyCheckingListener);
        etPassword.setOnFocusChangeListener(emptyCheckingListener);
        etEmail.setOnFocusChangeListener(emptyCheckingListener);
        etEmail.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    btnSignUpOnClick(findViewById(R.id.btn_sign_up_sign_up));
                    return true;
                }
                return false;
            }
        });

        customFont = Typeface.createFromAsset(getAssets(), getString(R.string.custom_font_path_1));
        ((TextView) findViewById(R.id.tv_sign_up_logo)).setTypeface(customFont);
    }

    @Override
    public void onBackPressed() {
        setResult(MainApplication.STATUS_CANCELLED);
        super.onBackPressed();
    }

    public void btnSignUpOnClick(View view) {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String password = etPassword.getText().toString();
        String email = etEmail.getText().toString().trim();

        final RelativeLayout rlSigningUp = (RelativeLayout) findViewById(R.id.rl_signing_up);

        boolean allow = true;

        if (firstName.equals(""))
            etFirstName.setError("");
        if (lastName.equals(""))
            etLastName.setError("");
        if (password.equals(""))
            etPassword.setError("");
        if (email.equals(""))
            etEmail.setError("");
        if (!firstName.matches("[\\p{L}\\p{Space}]*")) // To make sure only alphabet and spaces between alphabet in name
            etFirstName.setError("");
        if (!lastName.matches("[\\p{L}\\p{Space}]*"))  //   (\\p{L} is a Unicode Character Property that matches any kind of letter from any language)
            etLastName.setError("");

        if (firstName.equals("") || lastName.equals("") || password.equals("") || email.equals("")) {
            Toast.makeText(getBaseContext(), R.string.error_field_empty, Toast.LENGTH_SHORT).show();
            allow = false;
        }
        if (!firstName.matches("[\\p{L}\\p{Space}]*") || !lastName.matches("[\\p{L}\\p{Space}]*")) {
            Toast.makeText(getBaseContext(), R.string.error_invalid_character, Toast.LENGTH_SHORT).show();
            allow = false;
        }

        if (allow) {

            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etEmail.getWindowToken(), 0);

            ParseUser parseUser = new ParseUser();
            parseUser.setUsername(email);
            parseUser.setPassword(password);
            parseUser.setEmail(email);


            final ParseObject userProfile = new ParseObject("UserProfile");
            userProfile.put("firstName", firstName);
            userProfile.put("lastName", lastName);
            try {
                userProfile.save();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            parseUser.put("userProfile", userProfile);


            rlSigningUp.setVisibility(View.VISIBLE);
            animate(rlSigningUp).alpha(0f).setDuration(0).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    etEmail.setFocusable(false);
                    etPassword.setFocusable(false);
                    etLastName.setFocusable(false);
                    etFirstName.setFocusable(false);
                    btnSignUp.setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animate(rlSigningUp).alpha(1f).setDuration(320).start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

            }).start();

            parseUser.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {

                        ParseACL userProfileACL = new ParseACL();
                        userProfileACL.setWriteAccess(ParseUser.getCurrentUser(), true);
                        userProfileACL.setPublicReadAccess(true);
                        ParseUser.getCurrentUser().setACL(userProfileACL);
                        userProfile.setACL(userProfileACL);

                        getSharedPreferences("preferences", MODE_PRIVATE).edit().putBoolean("justSignedIn", true).commit();
                        getSharedPreferences("preferences", MODE_PRIVATE).edit().putBoolean("firstTimeUse", false).commit();
                        NavigationDrawerFragment.refreshUserProfile();
                        setResult(MainApplication.STATUS_SUCCESS);
                        finish();

                    } else {
                        if (e.getCode() == ParseException.EMAIL_TAKEN) {
                            Toast.makeText(getBaseContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            etEmail.setError("");
                        } else if (e.getCode() == ParseException.INVALID_EMAIL_ADDRESS) {
                            Toast.makeText(getBaseContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            etEmail.setError("");
                        } else {
                            Toast.makeText(getBaseContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        animate(rlSigningUp).alpha(0f).setDuration(320).setListener(null).start();
                        etEmail.setFocusableInTouchMode(true);
                        etPassword.setFocusableInTouchMode(true);
                        etLastName.setFocusableInTouchMode(true);
                        etFirstName.setFocusableInTouchMode(true);
                        btnSignUp.setClickable(true);
                    }

                }
            });
        }
    }


}
