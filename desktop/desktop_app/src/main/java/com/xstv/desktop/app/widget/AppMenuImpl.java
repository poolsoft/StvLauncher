
package com.xstv.desktop.app.widget;

import android.content.Context;
import android.view.View;

import com.xstv.desktop.app.R;
import com.xstv.desktop.app.interfaces.IAppMenu;
import com.xstv.desktop.app.listener.OnAppMenuListener;
import com.xstv.desktop.app.util.LauncherState;
import com.xstv.library.base.menu.LetvOptionsMenu;

public class AppMenuImpl implements IAppMenu {

    private static final String TAG = AppMenuImpl.class.getSimpleName();

    private LetvOptionsMenu mOptionsMenu;

    private OnAppMenuListener mOnMenuListener;

    private String moveApp;
    private String addApp;
    private String newFolder;
    private String uninstallApp;
    private String manageApp;
    private String feedback;

    public AppMenuImpl(Context context) {
        init(context);
    }

    private void init(Context context) {
        mOptionsMenu = new LetvOptionsMenu(LauncherState.getInstance().getHostContext());
        moveApp = context.getResources().getString(R.string.menu_move_app);
        addApp = context.getResources().getString(R.string.menu_add_app);
        newFolder = context.getResources().getString(R.string.menu_new_folder);
        uninstallApp = context.getResources().getString(R.string.menu_uninstall_app);
        manageApp = context.getResources().getString(R.string.menu_manage_app);
        feedback = context.getResources().getString(R.string.menu_feedback);
    }

    public void initAppItemMenu() {
        //mOptionsMenu.addItems(moveApp, newFolder, uninstallApp, manageApp, feedback);
        mOptionsMenu.addItems(uninstallApp);
        mOptionsMenu.setOnItemClickListener(new LetvOptionsMenu.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                switch (pos) {
                    case 0:
                        if (mOnMenuListener != null) {
                            //mOnMenuListener.moveApp();
                            mOnMenuListener.deleteApp();
                        }
                        break;
                    case 1:
                        if (mOnMenuListener != null) {
                            mOnMenuListener.newFolder();
                        }
                        break;
                    case 2:
                        if (mOnMenuListener != null) {
                            mOnMenuListener.deleteApp();
                        }
                        break;
                    case 3:
                        if (mOnMenuListener != null) {
                            mOnMenuListener.manageApp();
                        }
                        break;
                    case 4:
                        if (mOnMenuListener != null) {
                            mOnMenuListener.feedBack();
                        }
                        break;
                }
                hideMenu();
            }
        });
    }

    public void initFolderItemMenu() {
        mOptionsMenu.addItems(moveApp, addApp, uninstallApp, manageApp, feedback);
        mOptionsMenu.setOnItemClickListener(new LetvOptionsMenu.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                switch (pos) {
                    case 0:
                        if (mOnMenuListener != null) {
                            mOnMenuListener.moveApp();
                        }
                        break;
                    case 1:
                        if (mOnMenuListener != null) {
                            mOnMenuListener.addApp();
                        }
                        break;
                    case 2:
                        if (mOnMenuListener != null) {
                            mOnMenuListener.deleteApp();
                        }
                        break;
                    case 3:
                        if (mOnMenuListener != null) {
                            mOnMenuListener.manageApp();
                        }
                        break;
                    case 4:
                        if (mOnMenuListener != null) {
                            mOnMenuListener.feedBack();
                        }
                        break;
                }
                hideMenu();
            }
        });
    }

    @Override
    public void setOnAppMenuListener(OnAppMenuListener onMenuListener) {
        this.mOnMenuListener = onMenuListener;
    }

    @Override
    public void showMenu() {
        if (mOptionsMenu != null) {
            mOptionsMenu.showView();
        }
    }

    @Override
    public void hideMenu() {
        if (mOptionsMenu != null && mOptionsMenu.isShowing()) {
            mOptionsMenu.cancel();
        }
    }

    @Override
    public boolean isMenuShowing() {
        return mOptionsMenu != null && mOptionsMenu.isShowing();
    }
}
