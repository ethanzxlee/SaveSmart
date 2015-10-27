package com.savesmart;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;


public class UserLoginActivity extends Activity {

    protected TextView.OnFocusChangeListener emptyCheckingListener;
    private boolean mFinish = false;
    private SaveSmartEditText etEmail;
    private SaveSmartEditText etPassword;
    private Button btnLogin;
    private Button btnLoginSignUp;
    private Button btnForgetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        getActionBar().hide();

        etEmail = (SaveSmartEditText) findViewById(R.id.et_login_email);
        etPassword = (SaveSmartEditText) findViewById(R.id.et_login_password);
        btnLogin = (Button) findViewById(R.id.btn_login_login);
        btnLoginSignUp = (Button) findViewById(R.id.btn_login_sign_up);
        btnForgetPassword = (Button) findViewById(R.id.btn_login_forget_password);
        final ScrollView svLogin = (ScrollView) findViewById(R.id.sv_login);


        emptyCheckingListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b && view == etPassword) {
                    svLogin.smoothScrollTo(0, svLogin.getBottom());
                }
                if (!b && ((TextView) view).getText().toString().equals(""))
                    ((TextView) view).setError("");
                else
                    ((TextView) view).setError(null);
            }
        };

        etEmail.setOnFocusChangeListener(emptyCheckingListener);
        etPassword.setOnFocusChangeListener(emptyCheckingListener);
        etPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    btnLoginOnClick(findViewById(R.id.btn_login_login));
                    return true;
                }
                return false;
            }
        });

        Typeface customFont = Typeface.createFromAsset(getAssets(), getString(R.string.custom_font_path_1));
        ((TextView) findViewById(R.id.tv_login_logo)).setTypeface(customFont);

    }

    @Override
    public void onBackPressed() {
        setResult(MainApplication.STATUS_CANCELLED);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainApplication.USER_SIGN_UP_REQUEST) {
            if (resultCode == MainApplication.STATUS_SUCCESS) {
                setResult(resultCode);
                mFinish = true;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFinish)
            finish();
    }

    public void btnLoginOnClick(View view) {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (email.equals("") || password.equals("")) {
            Toast.makeText(getBaseContext(), R.string.error_field_empty, Toast.LENGTH_LONG).show();
            if (email.equals(""))
                etEmail.setError("");
            if (password.equals(""))
                etPassword.setError("");
        } else {
            //hide keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etEmail.getWindowToken(), 0);

            final RelativeLayout rlLoggingIn = (RelativeLayout) findViewById(R.id.rl_logging_in);

            rlLoggingIn.setVisibility(View.VISIBLE);
            rlLoggingIn.setAnimation(new Animation() {
            });
            animate(rlLoggingIn).alpha(0f).setDuration(0).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    etEmail.setFocusable(false);
                    etPassword.setFocusable(false);
                    btnLogin.setClickable(false);
                    btnForgetPassword.setClickable(false);
                    btnLoginSignUp.setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animate(rlLoggingIn).alpha(1f).setDuration(320).start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

            }).start();

            ParseUser.logInInBackground(email, password, new LogInCallback() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {

                    if (e == null) {
                        getSharedPreferences("preferences", MODE_PRIVATE).edit().putBoolean("justSignedIn", true).commit();
                        getSharedPreferences("preferences", MODE_PRIVATE).edit().putBoolean("firstTimeUse", false).commit();
                        NavigationDrawerFragment.refreshUserProfile();
                        setResult(MainApplication.STATUS_SUCCESS);
                        finish();
                    } else {
                        Toast.makeText(getBaseContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        animate(rlLoggingIn).alpha(0f).setDuration(320).setListener(null).start();
                        etEmail.setError("");
                        etPassword.setError("");
                        etEmail.setFocusableInTouchMode(true);
                        etPassword.setFocusableInTouchMode(true);
                        btnLogin.setClickable(true);
                        btnForgetPassword.setClickable(true);
                        btnLoginSignUp.setClickable(true);
                    }
                }
            });
        }
    }

    public void btnWlcSignUpOnClick(View view) {
        startActivityForResult(new Intent(this, UserSignUpActivity.class), MainApplication.USER_SIGN_UP_REQUEST);
    }

    public void btnForgetPasswordOnClick(View view) {
        final DialogFragment forgetPasswordDialogFragment = new ForgetPasswordDialogFragment();
        forgetPasswordDialogFragment.show(getFragmentManager(), "ForgetPassword");
    }

    public static class ForgetPasswordDialogFragment extends DialogFragment {

        private EditText emailAddress;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final View view = getActivity().getLayoutInflater().inflate(R.layout.popup_forget_password, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.title_popup_forget_password);
            builder.setView(view);
            builder.setPositiveButton(R.string.action_ok, null);
            emailAddress = (EditText) view.findViewById(R.id.et_reset_password_email);
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
                        if (emailAddress.getText().toString().equals("")) {
                            Toast.makeText(getActivity(), R.string.error_email_empty, Toast.LENGTH_SHORT).show();
                            emailAddress.setError("");
                        } else {
                            try {
                                ParseUser.requestPasswordReset(emailAddress.getText().toString().trim());
                                Toast.makeText(getActivity(), R.string.success_reset_password_sent, Toast.LENGTH_SHORT).show();
                                alertDialog.cancel();
                            } catch (ParseException e) {
                                emailAddress.setError("");
                                if (e.getCode() == ParseException.EMAIL_NOT_FOUND) {
                                    Toast.makeText(getActivity(), R.string.error_email_not_found, Toast.LENGTH_SHORT).show();
                                } else if (e.getCode() == ParseException.INVALID_EMAIL_ADDRESS) {
                                    Toast.makeText(getActivity(), R.string.error_email_invalid, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });
            }
        }
    }


}
