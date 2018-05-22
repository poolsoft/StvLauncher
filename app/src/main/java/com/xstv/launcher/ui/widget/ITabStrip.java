package com.xstv.launcher.ui.widget;


/**
 * Created by shaodong on 16-9-29.
 */

public interface ITabStrip {
    <T extends TabItemNew> void addTab(int position, T v, boolean isActive, boolean isNew);
    <T extends TabItemNew> T getTabItem(int position);
    int getTabCount();
    void scrollToChild(int position);
    void notifyStrategyChanged();
    void setBindStrategy(TabPagerBindStrategy strategy);
    void setTabText(String newText, int position);

    /**
     * This method will be invoked when the current page is scrolled, either as part
     * of a programmatically initiated smooth scroll or a user initiated touch scroll.
     *
     * @param position Position index of the first page currently being displayed.
     *                 Page position+1 will be visible if positionOffset is nonzero.
     * @param positionOffset Value from [0, 1) indicating the offset from the page at position.
     * @param positionOffsetPixels Value in pixels indicating the offset from position.
     */
    void onScrollChanged(int position, float positionOffset, int positionOffsetPixels);
}
