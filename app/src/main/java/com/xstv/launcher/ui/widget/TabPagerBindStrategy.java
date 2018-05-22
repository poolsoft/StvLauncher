package com.xstv.launcher.ui.widget;

/**
 * Created by shaodong on 16-9-29.
 */

public interface TabPagerBindStrategy {
    int getCount();
    void setPagerCurrentItem(int position);
    void setTabCurrentItem(int position);
    <T> T getPageTitle(int position);
}
