
package com.xstv.desktop.app.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;

import com.xstv.desktop.app.AppPluginActivator;
import com.xstv.desktop.app.R;
import com.xstv.desktop.app.db.ItemInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DialogFactory {
    private static final String TAG = DialogFactory.class.getSimpleName();


    /**
     * 无法安装
     *
     * @param itemInfo
     */
    public static void createNotInstallDialog(ItemInfo itemInfo) {
        if (!isAppDesktop()) {
            return;
        }

        final Context hostContext = LauncherState.getInstance().getHostContext();
        Context context = AppPluginActivator.getContext();
        Resources res = context.getResources();
        String appName = "";
        if (itemInfo != null) {
            appName = itemInfo.getTitle();
        }
        String title = res.getString(R.string.not_install_str);
        if (!TextUtils.isEmpty(appName)) {
            title += "\"" + appName + "\"";
        }
        String closeStr = res.getString(R.string.close_str);
        String notInstallStr = res.getString(R.string.please_retry_str);

        AlertDialog dialog = new AlertDialog.Builder(hostContext).create();
        dialog.setTitle(title);
        dialog.setMessage(notInstallStr);
        dialog.show();
    }

    /**
     * url地址不对
     *
     * @param itemInfo
     */
    public static void createUrlErrorDialog(ItemInfo itemInfo) {
        if (!isAppDesktop()) {
            return;
        }
        final Context hostContext = LauncherState.getInstance().getHostContext();
        Context context = AppPluginActivator.getContext();
        Resources res = context.getResources();
        String appName = "";
        if (itemInfo != null) {
            appName = itemInfo.getTitle();
        }
        String title = res.getString(R.string.not_downlaod_str);
        if (!TextUtils.isEmpty(appName)) {
            title += "\"" + appName + "\"";
        }
        String closeStr = res.getString(R.string.close_str);
        String retryStr = res.getString(R.string.please_retry_str);

        AlertDialog dialog = new AlertDialog.Builder(hostContext).create();
        dialog.setTitle(title);
        dialog.setMessage(retryStr);
        dialog.show();
    }

    /**
     * 断网
     *
     * @param itemInfo
     */
    public static void createCutNetDialog(ItemInfo itemInfo) {
        if (!isAppDesktop()) {
            return;
        }

        final Context hostContext = LauncherState.getInstance().getHostContext();
        Context context = AppPluginActivator.getContext();
        Resources res = context.getResources();
        String appName = "";
        if (itemInfo != null) {
            appName = itemInfo.getTitle();
        }
        String title = res.getString(R.string.not_downlaod_str);
        if (!TextUtils.isEmpty(appName)) {
            title += "\"" + appName + "\"";
        }
        String setNetStr = res.getString(R.string.set_network_str);
        String closeStr = res.getString(R.string.close_str);
        String checkNetwork = res.getString(R.string.check_network_text);

        AlertDialog dialog = new AlertDialog.Builder(hostContext).create();
        dialog.setTitle(title);
        dialog.setMessage(checkNetwork);
        dialog.show();
    }

    public static void createSpaceFullDialog() {
        if (!isAppDesktop()) {
            return;
        }

        final Context hostContext = LauncherState.getInstance().getHostContext();
        Context context = AppPluginActivator.getContext();
        Resources res = context.getResources();
        String title = res.getString(R.string.not_available_space);
        String message = res.getString(R.string.please_goto_tv_manager);
        String closeStr = res.getString(R.string.close_str);
        String clearStr = res.getString(R.string.clear_str);

        AlertDialog dialog = new AlertDialog.Builder(hostContext).create();
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.show();
    }

    private static void gotoNetworkSettings(Context context) {
        /**
         * import letv.setting.SettingUtil; Context context = … … int status = … … SettingUtil settingUtil = SettingUtil.getInstance(); settingUtil.startSettingNetwork(context, status); Definition of
         * status: 0 wire; 1 wireless; 2 wire & wireless & debug network
         */
        boolean hasErr = false;
        try {
            Class c = Class.forName("letv.setting.SettingUtil");
            Method getInstanceMethod = c.getDeclaredMethod("getInstance");
            Object t = getInstanceMethod.invoke(null);

            Method method = c.getMethod("startSettingNetwork", Context.class, int.class);

            // 0 wire; 1 wireless; 2 wire & wireless & debug network
            method.invoke(t, context, 2);

        } catch (ClassNotFoundException e) {
            hasErr = true;
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            hasErr = true;
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            hasErr = true;
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            hasErr = true;
            Throwable cause = e.getCause();
            cause.printStackTrace();
        } finally {
            if (hasErr) {
            }
        }
    }

    private static void gotoTvManager(Context context) {
        Intent mainIntent = context.getPackageManager().getLaunchIntentForPackage("com.stv.helper.main");
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(mainIntent);
    }

    private static boolean isDialogWithFocus() {
        try {
            Class clazz = Class.forName("com.stv.launcher.sdk.widget.LetvOptionsDialog$Builder");
            clazz.getMethod("setPositiveButton", CharSequence.class, DialogInterface.OnClickListener.class, boolean.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isAppDesktop() {
        return true;
    }
}
