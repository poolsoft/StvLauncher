package com.xstv.library.base;

import android.view.View;

public interface Page {

    /*//开始滚进屏幕
    public final static int STATE_ANIMATION_START_IN = 0;
    //开始滚出屏幕
    public final static int STATE_ANIMATION_START_OUT = 1;
    //结束滚进屏幕
    public final static int STATE_ANIMATION_END_IN = 2;
    //结束滚出屏幕
    public final static int STATE_ANIMATION_END_OUT = 3;
    //屏幕开始滚动
    public final static int STATE_ON_SIDE_ANIMATION_START = 4;
    //屏幕结束滚动
    public final static int STATE_ON_SIDE_ANIMATION_END = 5;*/

    public final static int SCROLL_STATE_IDLE = 0;
    public final static int SCROLL_STATE_DRAGGING = 1;


    View getView();

    void destory();

    /**
     * 四种状态:
     * 1.state=SCROLL_STATE_DRAGGING && Math.ads(pageOnPosition-selectedPosition)=0
     * 2.state=SCROLL_STATE_IDLE && Math.ads(pageOnPosition-selectedPosition)=0
     * <p>
     * 3.state=SCROLL_STATE_DRAGGING && Math.ads(pageOnPosition-selectedPosition)>=1
     * 4.state=SCROLL_STATE_IDLE && Math.ads(pageOnPosition-selectedPosition)>=1
     *
     * @param pageOnPosition   当前Page所处角标
     * @param selectedPosition 与ViewPager.onPageSelected(int position)一致
     * @param state            滚动状态
     * @see Page#SCROLL_STATE_IDLE
     * @see Page#SCROLL_STATE_DRAGGING
     */
    void onPageScrollStateChanged(int pageOnPosition, int selectedPosition, int state);

    void onForeground();

    void onBackGround();

    boolean onFocusRequested(int requestDirection);

    boolean onHomeKeyEventHandled();

    boolean canKeyDragOut();

    boolean canTouchDragOut();

    Object onActivityAction(int what, Object arg);

    int getMainScreen();

}
