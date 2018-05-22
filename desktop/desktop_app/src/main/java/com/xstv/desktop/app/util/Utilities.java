
package com.xstv.desktop.app.util;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.xstv.base.LetvLog;
import com.xstv.desktop.app.AppPluginActivator;
import com.xstv.desktop.app.db.ItemInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Various utilities shared amongst the Launcher's classes.
 */
public final class Utilities {

    private static final String TAG = "Utilities";

    public static final String QQ_PACKAGE_NAME = "com.tencent.qqmusictv.qing";

    /**
     * sdk中使用音效,EcoImageView,线程池
     */
    public static final String support_sdk_version_100 = "1.0.0";

    /**
     * 增加声明周期
     */
    public static final String support_sdk_version_102 = "1.0.2";

    private static String releaseVersion;

    static {
        // http://wiki.letv.cn/pages/viewpage.action?pageId=27894917
    }

    public static long getPackageInstallTime(PackageManager packageManager, String packageName) {
        try {
            PackageInfo info = packageManager.getPackageInfo(packageName, 0);
            return info.firstInstallTime;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public static byte[] flattenBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            Log.w(TAG, "Could not write bitmap");
            return null;
        }
    }

    /**
     * 打开已经安装的软件
     *
     * @param context
     * @param packageName
     */
    public static boolean openApp(Context context, String packageName, String activityName) {
        Intent intent = new Intent();
        ComponentName cn = new ComponentName(packageName, activityName);
        intent.setComponent(cn);
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            LetvLog.d(TAG, " openApp error ", e);
        }
        return false;
    }

    public static int getVersionCode(Context context) {
        int versionCode = 0;
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi;
            pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            versionCode = pi.versionCode;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "get app version code error!" + e.getMessage());
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 打开应用管理
     */
    public static boolean openManagApp() {
        boolean isOpen = false;
        try {
            Intent intent =  new Intent();
            intent.setAction("android.intent.action.MAIN");
            intent.setClassName("com.android.settings", "com.android.settings.ManageApplications");
            LauncherState.getInstance().getHostContext().startActivity(intent);
            isOpen = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isOpen;
    }

    public static String getDesktopParam(ItemInfo itemInfo){
        if(itemInfo == null){
            return "";
        }

        String packageName = itemInfo.getPackageName();
        String className = itemInfo.getClassName();
        if("com.stv.feedback".equals(packageName) && "com.stv.feedback.FeedbackActivity".equals(className)){
            try {
                JSONObject object = new JSONObject();
                object.put("feedback_msg_count", itemInfo.superscriptCount);
                return object.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";

    }

    /** Start: added by renguanghui@le.com for app splash ad. */

    public static boolean startAdManagerInPlugin(final Intent intent, Context context, Bundle options) {

        if (null == intent) {
            LetvLog.e(TAG, "startAdManagerInPlugin() intent is null.");
            return false;
        }

        if (!Intent.ACTION_MAIN.equals(intent.getAction())) {
            LetvLog.e(TAG, "startAdManagerInPlugin() action is " + intent.getAction());
            return false;
        }

        if (null == intent.getCategories()) {
            LetvLog.e(TAG, "startAdManagerInPlugin() intent categories is null.");
            return false;
        }

        if (!intent.getCategories().contains(Intent.CATEGORY_LAUNCHER)) {
            LetvLog.e(TAG, "startAdManagerInPlugin() intent contains not " + Intent.CATEGORY_LAUNCHER);
            return false;
        }

        if ("com.stv.bootadmanager".equalsIgnoreCase(intent.getStringExtra("from"))) {
            LetvLog.e(TAG, "startAdManagerInPlugin() start by ad manager.");
            return false;
        }

        ComponentName com = intent.getComponent();
        if (null == com) {
            LetvLog.e(TAG, "startAdManagerInPlugin() get dest pkg name error.");
            return false;
        }

        String pkg = intent.getPackage();
        String desktopSource = intent.getStringExtra("desktopSource");
        String desktopParam = intent.getStringExtra("desktopParam");
        String destClass = com.getClassName();
        String sortClass = com.getShortClassName();
        String destPkgName = com.getPackageName();

        LetvLog.d(TAG, "startAdManagerInPlugin() pkgName=" + pkg + ", destPkgName=" + destPkgName
                + ", destClass=" + destClass + ", sortClass=" + sortClass);

        if (destPkgName == null || "".equals(destPkgName)) {
            LetvLog.e(TAG, "startAdManagerInPlugin() dest pkg name is empty.");
            return false;
        }

        if (TextUtils.isEmpty(destClass)) {
            LetvLog.e(TAG, "startAdManagerInPlugin() destClass is empty.");
            return false;
        }

        LetvLog.v(TAG, "startAdManagerInPlugin() start ad manager service...");

        Bundle bundle = new Bundle();
        bundle.putString("pkg", pkg);
        bundle.putString("destPkgName", destPkgName);
        bundle.putString("destClass", destClass);
        bundle.putString("sortClass", sortClass);

        bundle.putString("desktopSource", desktopSource);
        bundle.putString("desktopParam", desktopParam);

        Intent service = new Intent();
        service.setAction("com.stv.admanager.action.APP_SPLASH_AD_BY_DESKTOP");
        service.putExtra("adBundle", bundle);
        service.putExtra("options", options);
        service.setPackage("com.stv.bootadmanager");

        try {
            ComponentName comName = context.startService(service);
            if (null != comName) {
                LetvLog.v(TAG, "startAdManagerInPlugin() start ad manager service success. {" + comName.getClassName() + "}");
                return true;
            } else {
                LetvLog.v(TAG, "startAdManagerInPlugin() ad manager service not exists.");
            }
        } catch (SecurityException e) {
            LetvLog.e(TAG, "startAdManagerInPlugin() " + e.getMessage());
        } catch (Exception e) {
            LetvLog.e(TAG, "startAdManagerInPlugin() " + e.getMessage());
        }
        LetvLog.v(TAG, "startAdManagerInPlugin() start ad manager service failed." + service);
        return false;

    }
    /** End: added by renguanghui@le.com for app splash ad. */

    /**
     * 打开问题反馈应用
     */
    public static boolean openFeedback() {
        boolean isOpen = false;

        Context pluginContext = AppPluginActivator.getContext();
        Context hostContext = LauncherState.getInstance().getHostContext();
        StringBuilder builder = new StringBuilder();
        // 来源名称
        String name = "appDesktopPlugin";
        // 插件包名
        String sourcePackage = pluginContext.getPackageName();
        builder.append(sourcePackage);
        builder.append(";");
        // 插件版本号
        String sourceVersion = getVersionCode(pluginContext) + "";
        builder.append(sourceVersion);
        builder.append(";");
        // 桌面包名
        String hostPackage = hostContext.getPackageName();
        // sdk版本号
        String sdkVersionCode = "-1";
        builder.append(sdkVersionCode);
        builder.append(";");
        // 桌面版本号
        String hostVersionCode = "-1";
        builder.append(hostVersionCode);

        Intent it = new Intent();
        ComponentName mComponentName = new ComponentName("com.stv.feedback",
                "com.stv.feedback.FeedbackActivity");
        it.setComponent(mComponentName);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        it.putExtra("name", name);
        it.putExtra("package", hostPackage);
        it.putExtra("source_version", sourceVersion);
        it.putExtra("source_package", sourcePackage);
        // 插件包名，插件版本号，sdk版本号，桌面版本号
        it.putExtra("leapp_descripe", builder.toString());

        LetvLog.d(TAG, "openFeedback name:" + name + ",sourcePackage:" + sourcePackage +
                ",sourceVersion" + sourceVersion + ",hostPackage:" + hostPackage + ",description:"
                + builder.toString());
        try {
            LauncherState.getInstance().getHostContext().startActivity(it);
            isOpen = true;
        } catch (Exception e) {
            LetvLog.d(TAG, "openApp error!", e);
        }

        return isOpen;
    }

    /**
     * Use for preloaded app.
     *
     * @return true : If uitype is cibn .
     */
    public static boolean isCIBN() {
        boolean isCIBN = false;
        return isCIBN;
    }

    public static boolean verifySupportSdk(String supportVersion) {
        return true;
    }

    public static boolean isSupportCTR() {
        return true;
    }

    public static boolean isVersion50s() {
        return false;
    }

    /**
     * 918,928,8064,u4,648不支持高斯模糊
     *
     * @return
     */
    public static boolean isNeedBlur() {
        return false;
    }

    public static boolean isPlatform648(){
        return false;
    }

    public static boolean isUserHttpManager(){
        try {
            Context context = LauncherState.getInstance().getHostContext();
            if(context == null){
                context = AppPluginActivator.getContext();
            }
            context.getClassLoader().loadClass("com.stv.launcher.sdk.http.HttpManager");
            return true;
        } catch (ClassNotFoundException e) {
            // Ignore
        }
        return false;
    }

    public static boolean checkApkExist(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName))
            return false;
        try {
            context.getPackageManager()
                    .getApplicationInfo(packageName,
                            PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    /**
     * 应用是否安装
     * @param context
     * @param intent
     * @return
     */
    public static boolean checkApkExist(Context context, Intent intent) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);
        if (list.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * 检测网络是否可用
     *
     * @return 网络是否可用
     */
    public static boolean isNetworkConnected(Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /*
     * uitype: cibn, full, hk, null(default)
     */
    public static boolean isCibnFuntion() {
        return false;
    }

    /**
     * setTranslationZ only on Android version which >= 5.0
     */
    public static void setShadowZ(View view, float f) {
        if (Build.VERSION.SDK_INT >= 21) {// Build.VERSION_CODES.LOLLIPOP
            ViewCompat.setTranslationZ(view, f);
        }
    }

    /**
     * Set change recyclerview draw order with setDrawingCacheEnabled
     *
     * @see android.view.ViewGroup#setChildrenDrawingOrderEnabled(boolean)
     */
    public static boolean isChangeDrawOrder() {
        if (Build.VERSION.SDK_INT >= 21) {// Build.VERSION_CODES.LOLLIPOP
            return false;
        }
        return true;
    }

    /**
     * In X60 should return true
     *
     * @return
     */
    public static boolean isInvalidate() {
        return false;
    }
}
