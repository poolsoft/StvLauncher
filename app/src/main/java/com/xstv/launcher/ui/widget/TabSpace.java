package com.xstv.launcher.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xstv.launcher.R;
import com.xstv.launcher.ui.activity.Launcher;
import com.xstv.library.base.LetvLog;

import java.util.ArrayList;

public class TabSpace extends RelativeLayout {
    private static final String TAG = TabSpace.class.getSimpleName();

    public interface OnTabChangedListener {
        void onTabChanged(String tabTag, boolean immediately);
    }

    public interface OnTabSpaceStatusListener {
        void onStatusChanged(boolean up);
    }

    private int mUnFocusCount;
    private Paint mDrawLinePaint = new Paint();

    private Launcher mLauncher;
    private ImageButton mManagerView;
    private TextView mPopGuideEditTv;
    private TabStripImpl mTabStrip;

    private TabActionHandler mTabActionHandler;
    private OnTabSpaceStatusListener mOnTabStatusListenter;


    public TabSpace(Context context) {
        this(context, null);
    }

    public TabSpace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabSpace(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTabStrip = (TabStripImpl) findViewById(R.id.tab_strip);
        mManagerView = (ImageButton) findViewById(R.id.manager_bt);
//        mTabStrip.setImportanceListener(this);
        if (false) {
            mManagerView.setOnFocusChangeListener(mManagerFocusChangeListener);
            mManagerView.setOnKeyListener(new ManagerButtonKeyListener());
            mManagerView.setOnClickListener(new ManagerButtonClickListener());
        } else {
            mManagerView.setVisibility(View.INVISIBLE);// Need occupancy space.
        }

        mDrawLinePaint.setColor(Color.parseColor("#33ffffff"));
        mDrawLinePaint.setStrokeWidth(1);
    }

/*    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        // 禁止ViewGrounp在子view中寻找焦点
        return true;
    }*/

    public void layout() {
        String[] unfocus = new String[0];
        mUnFocusCount = unfocus.length;
    }

    public void setPopGuideEditTv(TextView tv) {
        mPopGuideEditTv = tv;
        if (mManagerView.getVisibility() == View.VISIBLE && mPopGuideEditTv != null) {
            mPopGuideEditTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = false;
        //TODO 如果ScrollView正在滚动中，不响应按键
//        if (!mHScrollView.isScrollingFinish()) {
//            return true;
//        }
        if (event.getAction() == KeyEvent.ACTION_DOWN && mTabStrip.hasFocus()) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (null != mOnTabStatusListenter) {
                        mOnTabStatusListenter.onStatusChanged(false);
                    }
                    View nextLeft = findNextFocus(mTabStrip, mTabStrip.getSelectedPosition(), View.FOCUS_LEFT);
                    handled = nextLeft != null && nextLeft.requestFocus();
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (null != mOnTabStatusListenter) {
                        mOnTabStatusListenter.onStatusChanged(false);
                    }
                    int tabCount = mTabStrip.getTabCount();
                    if (tabCount > 0 && mTabStrip.getTabItem(tabCount - 1).hasFocus()) {
                        if (mManagerView.getVisibility() == View.VISIBLE) {
                            handled = mManagerView.requestFocus();
                        } else {
                            handled = true; // can't focus right
                        }
                    } else {
                        View nextRight = findNextFocus(mTabStrip, mTabStrip.getSelectedPosition(), View.FOCUS_RIGHT);
                        handled = nextRight != null && nextRight.requestFocus();
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (null != mOnTabStatusListenter) {
                        mOnTabStatusListenter.onStatusChanged(false);
                    }
                    handled = mTabActionHandler != null && mTabActionHandler.onTabAction(TabActionHandler.ACTION_FOCUS_DOWN);
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (null != mOnTabStatusListenter) {
                        mOnTabStatusListenter.onStatusChanged(true);
                    }
                    handled = true;
                    // PlaySoundUtils.getInstance().play(getContext(), SoundEffectUtil.FOCUS_END);
                    break;
                case KeyEvent.KEYCODE_BACK:
                case KeyEvent.KEYCODE_ESCAPE:
                    if (null != mOnTabStatusListenter) {
                        mOnTabStatusListenter.onStatusChanged(false);
                    }
                    break;
                default:
                    break;
            }

            LetvLog.d(TAG, "dispatchKeyEvent handled = " + handled);
            if (handled) {
                // PlaySoundUtils.getInstance().play(getContext(), SoundEffectUtil.FOCUS);
            }
        }
        return handled || super.dispatchKeyEvent(event);
    }

    private View findNextFocus(TabStripImpl root, int focused, int d) {
        View next = null;
        int nextPos = -1;
        int childNums = root.getTabCount();
        switch (d) {
            case View.FOCUS_LEFT:
                nextPos = focused - 1;
                if (nextPos >= 0 && nextPos < childNums) {
                    next = root.getTabItem(nextPos);
                }
                break;
            case View.FOCUS_RIGHT:
                nextPos = focused + 1;
                if (nextPos >= 0 && nextPos < childNums) {
                    next = root.getTabItem(nextPos);
                }
                break;
        }
        LetvLog.d(TAG, "findNextFocus " + next);
        if (next != null && next.isFocusable()) {
            return next;
        }
        return null;
    }

    public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
        mTabStrip.setLauncher(launcher);
    }

    public void setOnTabChangedListener(OnTabChangedListener l) {
        mTabStrip.setOnTabChangeListener(l);
    }

    public void setOnTabStatusListenter(OnTabSpaceStatusListener l) {
        mOnTabStatusListenter = l;
    }

    public void setTabActionHandler(TabActionHandler l) {
        mTabActionHandler = l;
    }

    public boolean requestManagerButtonFocus() {
        if (mManagerView != null) {
            return mManagerView.requestFocus();
        }
        return false;
    }

    //TODO need to be optimized : should not handle mUnFocusCount here;

    /**
     * @return AdapterPosition
     */
    public int getSelection() {
        return mTabStrip.getSelectedPosition() == -1 ? 0 : TabStripImpl.parseTab2PagerPosition(mTabStrip.getSelectedPosition());
    }

    /**
     * @return Current focused/Selected Tab's tag (the package name)
     */
    public String getCurrentTab() {
        return (String) mTabStrip.getTabItem(mTabStrip.getSelectedPosition()).getTag();
    }

    /**
     * @param index AdapterPosition
     */
    public void setCurrentTab(int index) {
        setCurrentTab(index, false);
    }

    public void setCurrentTab(String tag) {
        setCurrentTab(tag, false);
    }

    // Without animation
    public void setCurrentTab(String tag, boolean immediately) {
        int targetIndex = getPositionByScreenTag(tag);
        setCurrentTab(targetIndex, immediately);
    }

    /**
     * set the Tab that should be currently focused
     *
     * @param index       AdapterPosition
     * @param immediately whether the viewPager should perform scroll animation <br/>
     *                    true :not play
     */
    // Without animation
    public void setCurrentTab(int index, boolean immediately) {
        LetvLog.d(TAG, "setCurrentTab " + index + " " + immediately);
        mTabStrip.setCurrentTab(TabStripImpl.parsePager2TabPosition(index), immediately);
        if (getVisibility() != View.VISIBLE) {
            showTab();
        }
    }

    /**
     * @param tag the target Screen's tag(package name)
     * @return the index of the target screen (Viewpager position)
     * -1 if not found
     */
    private int getPositionByScreenTag(String tag) {
        int numTabs = getTabCount();
        for (int i = 0; i < numTabs; i++) {
            if (tag.equals(mTabStrip.getTabItem(i).getTag())) {
                return TabStripImpl.parseTab2PagerPosition(i);
            }
        }
        return -1;
    }

    public TabItemNew getItemByScreenTag(String tag) {
        int numTabs = getTabCount();
        for (int i = 0; i < numTabs; i++) {
            if (tag != null && tag.equals(mTabStrip.getTabItem(i).getTag())) {
                return mTabStrip.getTabItem(i);
            }
        }
        return null;
    }

    public void showTab() {
//        mTabStrip.resetImportanctShow();
        setVisibility(View.VISIBLE);

        if (mPopGuideEditTv != null && mManagerView.getVisibility() == View.VISIBLE) {
            mPopGuideEditTv.setVisibility(View.VISIBLE);
        }
    }

    public void hideTab() {
        setVisibility(View.INVISIBLE);
        if (null != mOnTabStatusListenter) {
            mOnTabStatusListenter.onStatusChanged(false);
        }

        if (mPopGuideEditTv != null) {
            mPopGuideEditTv.setVisibility(View.GONE);
        }
    }

    private OnFocusChangeListener mManagerFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (null != mOnTabStatusListenter) {
                mOnTabStatusListenter.onStatusChanged(false);
            }
        }
    };

    private class ManagerButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
        }

    }

    private class ManagerButtonKeyListener implements View.OnKeyListener {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        return true;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        int tabCount = getTabCount();
                        if (tabCount > 0 && mTabStrip.getTabItem(tabCount - 1).requestFocus()) {
                            //PlaySoundUtils.getInstance().play(getContext(), SoundEffectUtil.FOCUS);
                            return true;
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if (mTabActionHandler != null && mTabActionHandler.onTabAction(TabActionHandler.ACTION_FOCUS_DOWN)) {
                            return true;
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if (null != mOnTabStatusListenter) {
                            mOnTabStatusListenter.onStatusChanged(true);
                        }
                        //PlaySoundUtils.getInstance().play(getContext(), SoundEffectUtil.FOCUS_END);
                        return true;
                    default:
                        break;
                }
            }
            return false;
        }
    }

    public int getTabCount() {
        return mTabStrip == null ? 0 : mTabStrip.getTabCount();
    }

    public ITabStrip getTabStrip() {
        return mTabStrip;
    }

    public void notifyIsNewTips(String tag, boolean show) {
        TabItemNew item = getItemByScreenTag(tag);
        if (item != null) {
            if (show) {
                item.getTabTextView().showColorPoint();
            } else {
                item.getTabTextView().crushColorPoint();
            }
        }
    }
}
