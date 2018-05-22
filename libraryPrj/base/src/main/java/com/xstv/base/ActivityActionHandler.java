
package com.xstv.base;

/**
 * Created by panfeng on 15-9-11. Activity向Fragment通讯接口，在Fragment中实现，处理收到的Activity请求
 */
public interface ActivityActionHandler {
    /**
     * Activity向Fragment发送的消息类型
     */
    public static final int ACTIVITY_ACTION_START_HAND_DETECT = 0;
    public static final int ACTIVITY_ACTION_STOP_HAND_DETECT = ACTIVITY_ACTION_START_HAND_DETECT + 1;
    public static final int ACTIVITY_ACTION_ADD_HOVERLISTENER = ACTIVITY_ACTION_STOP_HAND_DETECT + 1;
    public static final int ACTIVITY_ACTION_REMOVE_HOVERLISTENER = ACTIVITY_ACTION_ADD_HOVERLISTENER + 1;
    public static final int ACTIVITY_ACTION_DESKTOP_MOVE_UP = ACTIVITY_ACTION_REMOVE_HOVERLISTENER + 1;
    public static final int ACTIVITY_ACTION_DESKTOP_MOVE_DOWN = ACTIVITY_ACTION_DESKTOP_MOVE_UP + 1;
    public static final int ACTIVITY_ACTION_DESKTOP_PAGE_STATUS = ACTIVITY_ACTION_DESKTOP_MOVE_DOWN + 1;
    public static final int ACTIVITY_ACTION_DESKTOP_TAB_ONCLICK = ACTIVITY_ACTION_DESKTOP_PAGE_STATUS + 1;
    public static final int ACTIVITY_ACTION_CHECK_HAND_STATUS = ACTIVITY_ACTION_DESKTOP_TAB_ONCLICK + 1;

    public Object onActivityAction(int what, Object arg);
}
