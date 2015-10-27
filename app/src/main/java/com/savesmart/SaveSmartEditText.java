package com.savesmart;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by ZheXian on 30/6/2014.
 */
public class SaveSmartEditText extends EditText {

    public SaveSmartEditText(Context context) {
        super(context);
    }

    public SaveSmartEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SaveSmartEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setError(CharSequence error, Drawable icon) {
        setCompoundDrawables(null, null, icon, null);
    }
}
