
package com.xstv.library.base.menu;

public interface ILetvOptionsMenu {

    /**
     * add menu item name
     *
     * @param names menu item name
     */
    void addItems(CharSequence... names);

    /**
     * show a menu
     */
    void showMenu();

    /**
     * cancel a showing menu
     */
    void hideMenu();

    /**
     * cancel a showing menu, without animation
     * @param isImmediately
     */
    void hideMenu(boolean isImmediately);

    /**
     * is showing
     */
    boolean isMenuShowing();

    /**
     * item click listener
     * @param itemClickListener
     */
    void setOnItemClickListener(LetvOptionsMenu.OnItemClickListener itemClickListener);

    /**
     * for onItemClick expand later
     * @param positon
     */
    void onItemClick(int positon);
}
