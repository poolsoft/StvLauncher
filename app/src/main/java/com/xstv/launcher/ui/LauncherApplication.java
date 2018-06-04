
package com.xstv.launcher.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Process;

import com.xstv.desktop.app.AppPluginActivator;
import com.xstv.desktop.app.widget.AppWorkspace;
import com.xstv.launcher.logic.manager.DataModel;
import com.xstv.library.base.LetvLog;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;



public class LauncherApplication extends Application implements Application.ActivityLifecycleCallbacks {

    protected static final String TAG = "LauncherApplication";

    public static LauncherApplication INSTANCE;

    public String mPreLocale = null;

    private Thread mUIThread;
    private UncaughtExceptionHandler mSystemUncaughtExceptionHandler;

    private ArrayList<Activity> mActivityRunnings = new ArrayList<Activity>(3);

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
        LetvLog.d(TAG, "attachBaseContext");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String pkgName = getPackageName();
        if (pkgName != null && pkgName.equals(getProcessName(android.os.Process.myPid()))) {
            INSTANCE = this;
            this.registerActivityLifecycleCallbacks(this);
            /** 必须首先调用并传递Context,否则会造成crash */
            // We should initialize this first, for it will be used within uncaught exception hander.
            mUIThread = Thread.currentThread();
            mUIThread.setName(TAG);
            mSystemUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(mUncaughtExceptionHandler);
            /** init pf */
            DataModel.getInstance().initPluginFramework();

            mPreLocale = getResources().getConfiguration().locale.getDisplayName();


            AppPluginActivator.initContext(this);
        }
    }

    private String getProcessName(int pid) {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

    // We use UncaughtExceptionHandler to catch unpredictable background thread exception.
    private Thread.UncaughtExceptionHandler mUncaughtExceptionHandler = new UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            LetvLog.e("UncaughtExceptionHandler.AndroidRuntime", "Caught a unhandled exception within background thread.", ex);
        }
    };

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        LetvLog.d(TAG, " onActivityCreated " + activity.getComponentName());
        mActivityRunnings.add(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        LetvLog.d(TAG, " onActivityStarted " + activity.getComponentName());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        LetvLog.d(TAG, " onActivityResumed " + activity.getComponentName());

        ComponentName componentName = activity.getComponentName();
        if (componentName.flattenToString().equals("com.stv.launcher/com.stv.launcher.ui.activity.Launcher")) {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        LetvLog.d(TAG, " onActivityPaused " + activity.getComponentName());

        ComponentName componentName = activity.getComponentName();
        if (componentName.flattenToString().equals("com.stv.launcher/com.stv.launcher.ui.activity.Launcher")) {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        LetvLog.d(TAG, " onActivityStopped " + activity.getComponentName());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        LetvLog.d(TAG, " onActivitySaveInstanceState " + activity.getComponentName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        LetvLog.d(TAG, " onActivityDestroyed " + activity.getComponentName());
        mActivityRunnings.remove(activity);
    }
}
