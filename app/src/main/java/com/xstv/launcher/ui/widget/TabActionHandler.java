
package com.xstv.launcher.ui.widget;

public interface TabActionHandler {

    int ACTION_FOCUS_DOWN = 0;
    int ACTION_BACK_MAIN_SCREEN = 1;

    boolean onTabAction(int what);
}
