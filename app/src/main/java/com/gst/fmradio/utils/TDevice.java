package com.gst.fmradio.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.WindowManager;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TDevice {

    private Context mContext;

    public TDevice() {
    }

    public TDevice(Context ctx) {
        mContext = ctx;
    }

    public static void setFullScreen(Activity activity) {
        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        activity.getWindow().setAttributes(params);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    public static void cancelFullScreen(Activity activity) {
        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
        params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activity.getWindow().setAttributes(params);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    public int getStatusBarHeight() {
        int result = 0;
        if (mContext == null) return result;

        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
