package com.xstv.launcher.util;

import android.content.Context;
import android.text.TextUtils;

import com.xstv.library.base.LetvLog;

public class AppUtil {
    private static final String TAG = "AppUtil";

    public static boolean isPackageInstalled(Context context, String packageName) {
        boolean result = false;
        if (!TextUtils.isEmpty(packageName)) {
            try {
                result = (null != context.getPackageManager().getApplicationInfo(packageName, 0));
            } catch (Exception e) {
            }
        }
        LetvLog.d(TAG, "isPackageInstalled packageName = " + packageName + " result = " + result);
        return result;
    }
}
