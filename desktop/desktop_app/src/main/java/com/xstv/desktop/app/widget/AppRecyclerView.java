
package com.xstv.desktop.app.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;

import com.xstv.library.base.LetvLog;
import com.xstv.desktop.app.adapter.BaseSpaceAdapter;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.util.LauncherState;

public class AppRecyclerView extends BaseRecyclerView implements ViewTreeObserver.OnTouchModeChangeListener {

    private static final String TAG = AppRecyclerView.class.getSimpleName();

    private static final int MSG_MOVE_END = 100;

    private GridLayoutManager mLayoutManager;
    private BaseSpaceAdapter mAdapter;

    private View mFocusedView;

    private long startTimeMillis;
    private boolean isMoving = false;
    private boolean isTouchModeChange = false;

    private boolean isUserVisible;

    private Handler mHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MOVE_END:
                    isMoving = false;
                    break;
            }
        }
    };

    public AppRecyclerView(Context context) {
        this(context, null);
    }

    public AppRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if (layout instanceof GridLayoutManager) {
            mLayoutManager = (GridLayoutManager) layout;
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        mAdapter = (BaseSpaceAdapter) adapter;
    }

    public void setUserVisible(boolean isUserVisible) {
        this.isUserVisible = isUserVisible;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().removeOnTouchModeChangeListener(this);
        getViewTreeObserver().addOnTouchModeChangeListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHander.removeCallbacksAndMessages(null);
        getViewTreeObserver().removeOnTouchModeChangeListener(this);
    }

    public boolean onFocus(int direction) {
        LetvLog.i(TAG, " onFocus mFocusedView = " + mFocusedView + " direction = " + direction);
        if (mFocusedView == null) {
            View view = mLayoutManager.findViewByPosition(0);
            LetvLog.d(TAG, "onFocus view = " + view);
            if (view instanceof BaseContent) {
                BaseContent content = (BaseContent) view;
                if (direction == View.FOCUS_LEFT) {
                    mFocusedView = content.getChildAt(0);
                }
                if (direction == View.FOCUS_RIGHT) {
                    mFocusedView = content.getChildAt(content.getChildCount() - 1);
                }
                if (direction == View.FOCUS_DOWN) {
                    mFocusedView = content.getChildAt(0);
                }
                smoothScrollToPosition(0);
            }
        } else {
            Object tagObj = mFocusedView.getTag();
            if (!(tagObj instanceof String)) {
                mFocusedView = null;
            } else {
                String tag = (String) mFocusedView.getTag();
                LetvLog.d(TAG, "onFocus tag = " + tag);
                String[] tags = tag.split(",");
                boolean isHit = false;
                if (tags.length == 2) {
                    int count = (mLayoutManager.getChildCount() - 1);
                    for (int i = 0; i < count; i++) {
                        View childview = mLayoutManager.getChildAt(i);
                        if (childview instanceof BaseContent) {
                            BaseContent baseContent = (BaseContent) childview;
                            String tag1 = (String) baseContent.getTag();
                            if (tag1 != null && tag1.equals(tags[0])) {
                                LetvLog.d(TAG, "onFocus tag1 = " + tag1);
                                int childCount = baseContent.getChildCount();
                                for (int i1 = 0; i1 < childCount; i1++) {
                                    View view = baseContent.getChildAt(i1);
                                    if (view instanceof BaseCellView) {
                                        String childTag = (String) view.getTag();
                                        if (childTag != null && childTag.equals(tag)) {
                                            LetvLog.d(TAG, "onFocus childTag = " + childTag);
                                            mFocusedView = view;
                                            isHit = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (isHit) {
                            break;
                        }
                    }
                } else if (tags.length == 1) {
                    int count = mLayoutManager.getChildCount() - 1;
                    for (int i = 0; i < count; i++) {
                        View childView = mLayoutManager.getChildAt(i);
                        if (childView instanceof BaseCellView) {
                            BaseCellView baseCellView = (BaseCellView) childView;
                            String tag1 = (String) baseCellView.getTag();
                            if (tag1 != null && tag1.equals(tag)) {
                                mFocusedView = childView;
                                isHit = true;
                                break;
                            }
                        }
                    }
                }

                if (!isHit) {
                    mFocusedView = null;
                }
            }
        }

        if (mFocusedView == null) {
            int positon = RecyclerView.NO_POSITION;
            try {
                positon = mLayoutManager.findFirstCompletelyVisibleItemPosition();
            } catch (Exception ex) {
                LetvLog.d(TAG, "onFocus error!!!", ex);
            }
            LetvLog.d(TAG, "onFocus position = " + positon);
            if (positon != RecyclerView.NO_POSITION) {
                View view = mLayoutManager.findViewByPosition(positon);
                if (view != null) {
                    if (view instanceof BaseContent) {
                        BaseContent group = (BaseContent) view;
                        int count = group.getChildCount();
                        for (int i = 0; i < count; i++) {
                            View child = group.getChildAt(i);
                            if (child != null && child instanceof BaseCellView) {
                                mFocusedView = child;
                                LetvLog.d(TAG, "onFocus find child = " + mFocusedView);
                                break;
                            }
                        }
                    } else {
                        mFocusedView = view;
                    }

                }
            }
        }
        LetvLog.i(TAG, "onFocus mFocusedView = " + mFocusedView);
        if (mFocusedView != null) {
            mFocusedView.setFocusable(true);
            mFocusedView.requestFocus();
        }
        return true;
    }

    public View getFocusedView() {
        View focusedChild = getFocusedChild();
        if (focusedChild instanceof BaseContent) {
            focusedChild = ((BaseContent) focusedChild).getFocusedChild();
        }
        return focusedChild;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean consumed = super.dispatchKeyEvent(event);
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        //LetvLog.d(TAG, " dispatchKeyEvent begin consumed = " + consumed + " action = " + action);
        if (isTouchModeChange) {
            isTouchModeChange = false;
            consumed = true;
        }
        if (action == KeyEvent.ACTION_DOWN && !consumed) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    View childView = mLayoutManager.getFocusedChild();
                    //LetvLog.d(TAG, "dispatchKeyEvent childView = " + childView);
                    View nextFocused = null;
                    boolean isInContent = false;
                    boolean isvalid = true;
                    if (childView != null) {
                        int direction = getFocusSearchDirection(keyCode);
                        if (mFocusedView != childView) {
                            LetvLog.d(TAG, "dispatchKeyEvent childView = " + childView + "isFocused = " + childView.isFocused()
                                    + " mHasFocus = " + childView.hasFocus());
                        }
                        if (childView.isFocused()) {
                            mFocusedView = childView;
                            nextFocused = focusSearch(childView, direction);
                            isInContent = false;
                        } else if (childView.hasFocus()) {
                            View srcFocusView = childView.findFocus();
                            mFocusedView = srcFocusView;
                            nextFocused = focusSearch(srcFocusView, direction);
                            isInContent = true;
                        }
                        // 焦点丢失
                        if (nextFocused == null || !(nextFocused instanceof BaseCellView)) {
                            if (direction == View.FOCUS_UP) {
                                LetvLog.d(TAG, "dispatchKeyEvent lose focuse");
                                mFocusedView = null;
                                consumed = false;
                            }
                        } else {
                            if (nextFocused != null) {
                                if (!isInContent && (AppWorkspace.mState == BaseWorkspace.State.STATE_MOVE ||
                                        AppFolderWorkspace.mState == BaseWorkspace.State.STATE_MOVE)) {
                                    if (mFocusedView instanceof CellView) {
                                        boolean canMove = ((CellView) mFocusedView).canMove;
                                        boolean isMoved = false;
                                        if (!isMoving) {
                                            isMoving = true;
                                            mHander.sendEmptyMessageDelayed(MSG_MOVE_END, 100);
                                            if (canMove) {
                                                // int index = mLayoutManager.getPosition(mFocusedView);
                                                isMoved = actionMove(mFocusedView, nextFocused);
                                                if (isMoved) {
                                                    if (mFocusedView != null) {
                                                        mFocusedView.requestFocus();
                                                        playSoundEffect(SoundEffectConstants
                                                                .getContantForFocusDirection(direction));
                                                    }
                                                }
                                            } else {
                                                if (nextFocused instanceof CellView) {
                                                    nextFocused.requestFocus();
                                                    if (isDescendant(this, nextFocused)) {
                                                        mFocusedView = nextFocused;
                                                    }
                                                    playSoundEffect(SoundEffectConstants
                                                            .getContantForFocusDirection(direction));
                                                }
                                            }
                                        }
                                        consumed = true;
                                    }
                                } else if (!isInContent && (AppWorkspace.mState == BaseWorkspace.State.STATE_DELETE ||
                                        AppFolderWorkspace.mState == BaseWorkspace.State.STATE_DELETE)) {
                                    long endTimeMillis = System.currentTimeMillis();
                                    if (endTimeMillis - startTimeMillis >= 100) {
                                        isvalid = true;
                                        startTimeMillis = System.currentTimeMillis();
                                    } else {
                                        isvalid = false;
                                    }
                                }

                                if (!consumed && isvalid) {
                                    nextFocused.requestFocus();
                                    playSoundEffect(SoundEffectConstants
                                            .getContantForFocusDirection(direction));
                                    if (isDescendant(this, nextFocused)) {
                                        mFocusedView = nextFocused;
                                    }
                                    consumed = true;
                                }
                            }
                        }
                    }
                    break;
            }
        }
        //LetvLog.d(TAG, "dispatchKeyEvent mFocusedView = " + mFocusedView);
        return consumed;// ? true : super.dispatchKeyEvent(event)
    }

    private boolean isDescendant(View parent, View child) {
        if ((child == null) || !(parent instanceof ViewParent)) {
            return false;
        }
        ViewParent childParent = child.getParent();
        View viewParent = (childParent instanceof View) ? (View) childParent : null;
        return childParent == parent || isDescendant(parent, viewParent);
    }

    @Override
    public void resetSmoothScroll() {
        super.resetSmoothScroll();
        mFocusedView = null;
    }

    @Override
    public void resetScroll() {
        super.resetScroll();
        mFocusedView = null;
    }

    private int getFocusSearchDirection(int keyCode) {
        int direction = View.FOCUS_DOWN;
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            direction = View.FOCUS_DOWN;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            direction = View.FOCUS_UP;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            direction = View.FOCUS_LEFT;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            direction = View.FOCUS_RIGHT;
        }
        return direction;
    }

    private boolean actionMove(final View fromView, final View toView) {
        LetvLog.d(TAG, " actionMove fromView = " + fromView + " toView = " + toView);
        boolean moved = false;
        if (fromView == null || toView == null) {
            moved = false;
            return moved;
        }

        if ((fromView instanceof CellView) && (toView instanceof CellView)) {
            Object fromInfo = ((CellView) fromView).getItemInfo();
            Object toInfo = ((CellView) toView).getItemInfo();
            // To position must not top three big icon.
            if (fromInfo == null || toInfo == null) {
                moved = false;
                return moved;
            }

            int fromPosition = mLayoutManager.getPosition(fromView);
            int toPosition = mLayoutManager.getPosition(toView);
            mAdapter.moveItem(fromPosition, toPosition);
            moved = true;
        }
        return moved;
    }

    @Override
    public void onTouchModeChanged(boolean isInTouchMode) {
        if (!isUserVisible || !isShown()) {
            return;
        }

        if (isInTouchMode) {
            return;
        }

        View recoverFocusView = null;
        if (getParent() instanceof AppWorkspace) {
            String focusTag = LauncherState.getInstance().getAppFocusTag();
            if (focusTag != null) {
                String[] split = focusTag.split(",");
                try {
                    if (split.length == 2) {
                        //说明是海报位
                        int pos = Integer.parseInt(split[0]);
                        int index = Integer.parseInt(split[1]);
                        View view = mLayoutManager.findViewByPosition(pos);
                        if (view instanceof BaseContent) {
                            BaseContent content = (BaseContent) view;
                            int childCount = content.getChildCount();
                            if (index < childCount) {
                                recoverFocusView = content.getChildAt(index);
                            }
                        }
                    } else {
                        int pos = Integer.parseInt(focusTag);
                        recoverFocusView = mLayoutManager.findViewByPosition(pos);
                    }

                } catch (NumberFormatException ex) {
                }
            }
        } else if (getParent() instanceof AppFolderWorkspace) {
            String focusTag = LauncherState.getInstance().getAppInFolderFocusTag();
            if (focusTag != null) {
                try {
                    int pos = Integer.parseInt(focusTag);
                    recoverFocusView = mLayoutManager.findViewByPosition(pos);
                } catch (NumberFormatException ex) {
                }
            }
        }
        if (recoverFocusView == null) {
            int pos = mLayoutManager.findFirstCompletelyVisibleItemPosition();
            recoverFocusView = mLayoutManager.findViewByPosition(pos);
        }
        if (recoverFocusView != null) {
            if (recoverFocusView instanceof BaseCellView) {
                ItemInfo itemInfo = ((BaseCellView) recoverFocusView).getItemInfo();
                LetvLog.i(TAG, "onTouchModeChanged itemInfo = " + itemInfo);
            }
            recoverFocusView.requestFocus();
        }

        isTouchModeChange = true;
    }
}
