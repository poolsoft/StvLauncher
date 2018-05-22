package com.xstv.base;

/**
 * Created by panfeng on 15-9-11. Fragment向Activity通讯接口，在Activity中实现，处理收到的Fragment请求
 */
public interface FragmentActionHandler {
    /**
     * Fragment向Activity发送的消息类型
     */
    public static final int FRAGMENT_ACTION_HIDE_TAB = 0;
    public static final int FRAGMENT_ACTION_SHOW_TAB = FRAGMENT_ACTION_HIDE_TAB + 1;
    public static final int FRAGMENT_ACTION_HIDE_ON_ANIM_TAB = FRAGMENT_ACTION_SHOW_TAB + 1;
    // TODO: temp modify for Conference
    public static final int FRAGMENT_ACTION_SWITCH_DESKTOP = FRAGMENT_ACTION_HIDE_ON_ANIM_TAB + 1;
    public static final int FRAGMENT_ACTION_BACK_KEY = FRAGMENT_ACTION_SWITCH_DESKTOP + 1;
    public static final int FRAGMENT_ACTION_CHECK_HAND_DETECT_ENTER = FRAGMENT_ACTION_BACK_KEY + 1;
    public static final int FRAGMENT_ACTION_HIDE_STATUSBAR = FRAGMENT_ACTION_CHECK_HAND_DETECT_ENTER + 1;
    public static final int FRAGMENT_ACTION_SHOW_STATUSBAR = FRAGMENT_ACTION_HIDE_STATUSBAR + 1;

    public static final int FRAGMENT_ACTION_FIRST_SHOWN_COMPLETED = FRAGMENT_ACTION_SHOW_STATUSBAR+1;

    public Object onFragmentAction(BaseFragment who, int what, Object arg);
}
