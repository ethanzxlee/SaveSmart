package com.savesmart;

import android.app.Application;

import com.parse.Parse;
import com.parse.PushService;

/**
 * Created by ZheXian on 26/5/2014.
 */
public class MainApplication extends Application {

    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_FAILED = 1;
    public static final int STATUS_CANCELLED = 2;
    public static final int STATUS_SKIPPED = 3;
    public static final int WLC_SCREEN_REQUEST = 100;
    public static final int USER_LOGIN_REQUEST = 101;
    public static final int USER_SIGN_UP_REQUEST = 102;
    public static final int PROFILE_PIC_CHOOSER_REQUEST = 103;
    public static final int TAKE_FROM_CAMERA = 104;
    public static final int SELECT_FROM_GALLERY = 105;
    public static final int RECOGNIZER_REQUEST = 106;

    public void onCreate() {
        super.onCreate();
        //Parse.enableLocalDatastore(this);
        Parse.initialize(this, "SLLwfUBJxFqRZQkuNEgeuntGKl2TyeHrx0Jw9qIv", "xNgTALti8kbE4AjQv3fkO94SBicqa6FOqRPtDnzn");
        PushService.setDefaultPushCallback(this, MainActivity.class);
    }
}
