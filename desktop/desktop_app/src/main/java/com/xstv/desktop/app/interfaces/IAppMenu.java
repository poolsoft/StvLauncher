
package com.xstv.desktop.app.interfaces;


import com.xstv.desktop.app.listener.OnAppMenuListener;

public interface IAppMenu {


    void setOnAppMenuListener(OnAppMenuListener onMenuListener);

    void initAppItemMenu();

    void initFolderItemMenu();

    void showMenu();

    void hideMenu();

    boolean isMenuShowing();
}
