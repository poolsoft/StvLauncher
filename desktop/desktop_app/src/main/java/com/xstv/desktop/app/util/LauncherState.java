
package com.xstv.desktop.app.util;

import android.content.Context;

public class LauncherState {
    private static final String TAG = LauncherState.class.getSimpleName();

    private static LauncherState INSTANCE;

    private Context mHostContext;
    private boolean isVisibleToUser;
    private String appFocusTag;
    private String appInFolderFocusTag;

    public static LauncherState getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LauncherState();
        }
        return INSTANCE;
    }

    public void setHostContext(Context hostContext) {
        this.mHostContext = hostContext;
    }

    public Context getHostContext() {
        return mHostContext;
    }

    public String getAppFocusTag() {
        return appFocusTag;
    }

    public void setAppFocusTag(String appFocusTag) {
        this.appFocusTag = appFocusTag;
    }

    public String getAppInFolderFocusTag() {
        return appInFolderFocusTag;
    }

    public void setAppInFolderFocusTag(String appInFolderFocusTag) {
        this.appInFolderFocusTag = appInFolderFocusTag;
    }

    public boolean isVisibleToUser() {
        return isVisibleToUser;
    }

    public void setVisibleToUser(boolean visibleToUser) {
        isVisibleToUser = visibleToUser;
    }
}
