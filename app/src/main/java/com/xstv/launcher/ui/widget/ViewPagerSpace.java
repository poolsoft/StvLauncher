
package com.xstv.launcher.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;

import com.xstv.base.BaseFragment;
import com.xstv.base.LetvLog;
import com.xstv.launcher.ui.activity.Launcher;
import com.xstv.launcher.ui.presenter.LauncherAdapterPresenter;

import java.lang.reflect.Field;

public class ViewPagerSpace extends ViewPager implements LauncherAdapterPresenter.OnScreenSwitchedListener {

    private static final String TAG = ViewPagerSpace.class.getSimpleName();


    private FrameLayout mLauncherRootView;
    private Launcher mLauncher;
    private Drawable mBackgroundDrawable;


    private FragmentPagerScroller mScroller;
    private static final boolean sFindFocusBeforeTrunPage = false;

    public ViewPagerSpace(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(false);
        initCustomScroller();
    }

    private void initCustomScroller() {
        mScroller = new FragmentPagerScroller(getContext(), new ScrollInterpolator());
        try {
            Field scrollerField;
            scrollerField = ViewPager.class.getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            scrollerField.set(this, mScroller);
        } catch (Exception e) {
            LetvLog.d("FragmentViewPager", "setScroller(mScroller) e = " + e);
        }
    }

    @Override
    protected boolean canScroll(View v, boolean arg1, int arg2, int arg3, int arg4) {
        if (v instanceof HorizontalScrollView) {
            return false;
        }
        return super.canScroll(v, arg1, arg2, arg3, arg4);
    }

    @Override
    public boolean executeKeyEvent(KeyEvent event) {
        if (sFindFocusBeforeTrunPage) {
            return super.executeKeyEvent(event);
        }
        boolean handled = false;
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    handled = arrowScrollNotFindFocus(FOCUS_LEFT);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    handled = arrowScrollNotFindFocus(FOCUS_RIGHT);
                    break;
                case KeyEvent.KEYCODE_TAB:
                    if (Build.VERSION.SDK_INT >= 11) {
                        // The focus finder had a bug handling FOCUS_FORWARD and FOCUS_BACKWARD
                        // before Android 3.0. Ignore the tab key on those devices.
                        handled = arrowScrollNotFindFocus(FOCUS_FORWARD);
                    }
                    break;
            }
        }
        return handled;
    }

    private boolean arrowScrollNotFindFocus(int direction) {
        if (!currentCanDragOut()) {
            LetvLog.d("FragmentViewPager", "can not dragout, ignore arrowScrollNotFindFocus");
            return true;
        }
        boolean handled = false;
        if (direction == FOCUS_LEFT || direction == FOCUS_BACKWARD) {
            // Trying to move left and nothing there; try to page.
            handled = pageLeftCopyFromParent();
        } else if (direction == FOCUS_RIGHT || direction == FOCUS_FORWARD) {
            // Trying to move right and nothing there; try to page.
            handled = pageRightCopyFromParent();
        }
        if (handled) {
            playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
        }
        LetvLog.d("FragmentViewPager", "page Switch=" + handled);
        return handled;
    }

    private boolean pageLeftCopyFromParent() {
        int mCurItem = getCurrentItem();
        if (mCurItem > 0) {
            setCurrentItem(mCurItem - 1, true);
            return true;
        }
        return false;
    }

    private boolean pageRightCopyFromParent() {
        int mCurItem = getCurrentItem();
        if (getAdapter() != null && mCurItem < (getAdapter().getCount() - 1)) {
            setCurrentItem(mCurItem + 1, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean arrowScroll(int direction) {
        if (!currentCanDragOut()) {
            LetvLog.d("FragmentViewPager", "can not dragout, ignore arrowScroll");
            return true;
        }
        return super.arrowScroll(direction);
    }

    private boolean currentCanDragOut() {
        FragmentPresenter fp = getAdapter().getItemInstance(getCurrentItem());
        if (fp != null) {
            BaseFragment bf = fp.getFragment();
            if (bf == null || bf.canKeyDragOut()) {
                return true;
            }
        }
        return false;
    }

    public void setCurrentItemImmediately(int item) {
        mScroller.setDuration(10);
        setCurrentItem(item);
        mScroller.setDuration(mScroller.getDefaultDuration());
    }

    @Override
    public LauncherAdapter getAdapter() {
        return (LauncherAdapter) super.getAdapter();
    }

    public void setDefaultItem(int defaultItem) {
        Field mCurItem;
        try {
            mCurItem = ViewPager.class.getDeclaredField("mCurItem");
            mCurItem.setAccessible(true);
            mCurItem.set(this, defaultItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ScrollInterpolator implements Interpolator {
        public ScrollInterpolator() {
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1;
        }
    }

    public void setLauncher(Launcher launcher) {
        mLauncher = launcher;
    }

    public void setLauncherRootView(FrameLayout root) {
        mLauncherRootView = root;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = super.dispatchKeyEvent(event);
        if (!handled) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                int keyCode = event.getKeyCode();
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    LetvLog.d(TAG, "fragment up key");
                    BaseFragment current = getAdapter() != null ? getAdapter().getItemInstance(getCurrentItem()).getFragment() : null;
                    if (current != null && (current.canKeyDragOut() || current.canTouchDragOut())) {
                        mLauncher.getAdapterPresenter().setCurrentTab(getCurrentItem());
                    }
                    handled = true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    LetvLog.d(TAG, "workspace left key");
                    BaseFragment current = getAdapter() != null ? getAdapter().getItemInstance(getCurrentItem()).getFragment() : null;
                    if (current != null && current.getContainer() != null) {
                        doReboundAnimation(current.getContainer());
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    LetvLog.d(TAG, "workspace right key");
                    BaseFragment current = getAdapter() != null ? getAdapter().getItemInstance(getCurrentItem()).getFragment() : null;
                    if (current != null && current.getContainer() != null) {
                        doReboundAnimation(current.getContainer());
                    }
                    return true;
                }
            }
        }
        return handled;
    }

    private void doReboundAnimation(View target) {
        TranslateAnimation ani = new TranslateAnimation(0, -30, 0, 0);
        ani.setDuration(100);
        ani.setRepeatCount(1);
        ani.setRepeatMode(Animation.REVERSE);
        ani.setInterpolator(new AccelerateDecelerateInterpolator());
        target.startAnimation(ani);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!currentCanTouchDragOut()) {
            // current screen disable switch
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean currentCanTouchDragOut() {
        if (getAdapter() != null) {
            FragmentPresenter fp = getAdapter().getItemInstance(getCurrentItem());
            if (fp != null) {
                BaseFragment current = fp.getFragment();
                return current == null || current.canTouchDragOut();
            }
        }
        return true;
    }

    public boolean currentCanKeyDragOut() {
        if (getAdapter() != null) {
            FragmentPresenter fp = getAdapter().getItemInstance(getCurrentItem());
            if (fp != null) {
                BaseFragment current = fp.getFragment();
                return current == null || current.canKeyDragOut();
            }
        }
        return true;
    }

    @Override
    public void onScreenSelected(String screenTag, int index, int total) {
        if (mLauncherRootView != null) {
            /*if (mBackgroundDrawable == null) {
                mBackgroundDrawable = getContext().getResources().getDrawable(R.drawable.lb_background);
            }
            mLauncherRootView.setBackgroundDrawable(mBackgroundDrawable);*/
        }
    }

    @Override
    public void onScreenScrolling(int state) {

    }

    public void checkNeedEmptyBackground() {

    }
}
