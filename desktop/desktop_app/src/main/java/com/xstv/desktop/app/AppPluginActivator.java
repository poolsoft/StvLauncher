
package com.xstv.desktop.app;

import android.content.Context;


public class AppPluginActivator {
    private static final String TAG = AppPluginActivator.class.getSimpleName();

    private static Context mContext = null;

    public static Context getContext() {
        return mContext;
    }


    public static void initContext(Context context) {
        mContext = context;
    }
}
