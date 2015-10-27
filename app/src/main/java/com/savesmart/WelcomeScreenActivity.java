package com.savesmart;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;


public class WelcomeScreenActivity extends Activity {

    private boolean mFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        getActionBar().hide();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        }

        Typeface customFont = Typeface.createFromAsset(getAssets(), getString(R.string.custom_font_path_1));
        ((TextView) findViewById(R.id.tv_welcome_screen_logo)).setTypeface(customFont);
    }

    @Override
    public void onBackPressed() {
        setResult(MainApplication.STATUS_CANCELLED);
        getSharedPreferences("preferences", MODE_PRIVATE).edit().putBoolean("firstTimeUse", true).commit();
        super.onBackPressed();

    }

    public void btnWlcSignUpOnClick(View view) {
        Intent intent = new Intent(this, UserSignUpActivity.class);
        startActivityForResult(intent, MainApplication.USER_SIGN_UP_REQUEST);
    }

    public void btnWlcLoginOnClick(View view) {
        Intent intent = new Intent(this, UserLoginActivity.class);
        startActivityForResult(intent, MainApplication.USER_LOGIN_REQUEST);
    }

    public void btnWlcSkipNowOnClick(View view) {
        getSharedPreferences("preferences", MODE_PRIVATE).edit().putBoolean("firstTimeUse", false).commit();
        setResult(MainApplication.STATUS_SKIPPED);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MainApplication.USER_LOGIN_REQUEST || requestCode == MainApplication.USER_SIGN_UP_REQUEST) {
            if (resultCode == MainApplication.STATUS_SUCCESS) {
                setResult(resultCode);
                mFinish = true;
            } else if (resultCode == MainApplication.STATUS_CANCELLED) {
                setResult(resultCode);
                mFinish = false;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFinish)
            finish();
    }

}
