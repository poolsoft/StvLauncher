
package com.xstv.library.base.menu;

import android.content.Context;
import android.view.View;

import com.xstv.library.base.Logger;

public class LetvOptionsMenu {
    private static Logger sLogger = Logger.getLogger(Logger.MODULE_SDK, "LetvOptionsMenu");

    private ILetvOptionsMenu mOptionMenu;
    private boolean isOldVersionMenu;

    public LetvOptionsMenu(Context hostContext) {
        init(hostContext, isOldPlatform(hostContext));
    }

    public LetvOptionsMenu(Context hostContext, boolean isUseOldMenu) {
        init(hostContext, isUseOldMenu);
    }

    public LetvOptionsMenu(Context hostContext, int theme) {
        init(hostContext, isOldPlatform(hostContext), theme);
    }

    public LetvOptionsMenu(Context hostContext, int theme, boolean isUseOldMenu) {
        init(hostContext, isUseOldMenu, theme);
    }

    private void init(Context hostContext, boolean isUseOldMenu) {
        init(hostContext, isUseOldMenu, -1);
    }

    private void init(Context hostContext, boolean isUseOldMenu, int theme) {
        isOldVersionMenu = isUseOldMenu;
        sLogger.i("init isOldVersionMenu = " + isOldVersionMenu);
        try {
            if (isUseOldMenu) {
                if (theme == -1) {
                    mOptionMenu = new LetvOptionsOldMenu(hostContext);
                } else {
                    mOptionMenu = new LetvOptionsOldMenu(hostContext, theme);
                }
            } else {

            }
        } catch (NoClassDefFoundError error) {
            sLogger.e("LetvOptionsNewMenu error!!!", error);
        }
    }

    /**
     * add menu item name
     *
     * @param names menu item name
     */
    public void addItems(CharSequence... names) {
        if (mOptionMenu == null) {
            return;
        }
        mOptionMenu.addItems(names);
    }

    /**
     * show a menu use new name @see #showMenu()
     */
    @Deprecated
    public void showView() {
        if (mOptionMenu == null) {
            return;
        }
        mOptionMenu.showMenu();
    }

    /**
     * show a menu
     */
    public void showMenu() {
        if (mOptionMenu == null) {
            return;
        }
        mOptionMenu.showMenu();
    }

    /**
     * cancel a showing menu use new name @see #hideMenu()
     */
    @Deprecated
    public void cancel() {
        if (mOptionMenu == null) {
            return;
        }
        mOptionMenu.hideMenu();
    }

    /**
     * hide menu
     */
    public void hideMenu() {
        if (mOptionMenu == null) {
            return;
        }
        mOptionMenu.hideMenu();
    }

    public void hideMenu(boolean isImmediately) {
        if (mOptionMenu == null) {
            return;
        }
        mOptionMenu.hideMenu(isImmediately);
    }

    /**
     * is old version menu
     *
     * @return false
     */
    public boolean isOldVersionMenu() {
        return isOldVersionMenu;
    }

    /**
     * menu show state
     *
     * @return
     */
    public boolean isShowing() {
        if (mOptionMenu == null) {
            return false;
        }
        return mOptionMenu.isMenuShowing();
    }

    private boolean isOldPlatform(Context context) {
        try {
            context.getClassLoader().loadClass("letv.app.LetvMenu");
            return true;
        } catch (ClassNotFoundException e) {
            // Ignore
        }
        return true;
    }

    /**
     * set item click listener
     *
     * @param itemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        if (mOptionMenu == null) {
            return;
        }
        mOptionMenu.setOnItemClickListener(itemClickListener);
    }

    public interface OnItemClickListener {
        /**
         * when click item callback
         *
         * @param view     item view
         * @param position item position
         */
        void onItemClick(View view, int position);
    }
}
