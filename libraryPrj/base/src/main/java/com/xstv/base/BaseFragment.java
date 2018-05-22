
package com.xstv.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public abstract class BaseFragment extends Fragment implements ActivityActionHandler {

    private Logger mLogger = Logger.getLogger("SDK", "BaseFragment");

    public interface OnUIPreListener {
        void onUIPre(BaseFragment fragment);

        void onUIRecycle(BaseFragment fragment);
    }

    private final Runnable mInSelectedRunnable = new Runnable() {
        public void run() {
            if (BaseFragment.this.getActivity() == null) {
                return;
            }
            if (!mBeNotifySelect) {
                long start = System.currentTimeMillis();

                mBeNotifySelect = true;
                onFragmentSeletedPre(true);

                mLogger.d("==> onFragmentSelectedPre  [" + tag + "] true" + ", take " + (System.currentTimeMillis() - start) + " ms");
            }
        }
    };

    private final WeakHandler<BaseFragment> mHandler = new WeakHandler<BaseFragment>(this);

    public static final int FOCUS_LEFT_IN = 0x00000011;
    public static final int FOCUS_TOP_IN = 0x00000012;
    public static final int FOCUS_RIGHT_IN = 0x00000013;
    public static final int FOCUS_BOTTOM_IN = 0x00000014;
    public static final int STATE_SCROLLING = 0;
    public static final int STATE_SCROLLING_END = 1;

    // This is used to save widget context string, but invalid on EUI6.0
    public static final int sPluginPackageNameSaveId = 0x20160519;
    public String tag;
    public String pluginPkgName;
    // fragment content view cache
    public View mContainer;
    public FragmentActionHandler mFragmentHandler;

    private int mId;
    private boolean mFragmentSaveStateEnable = false;
    private boolean mFragmentCacheEnabled = false;
    private boolean mContainerCanGetFocus = false;
    private boolean mBeNotifyShown = false;
    private boolean mBeNotifySelect = false;
    private boolean mOnViewCreated;
    private boolean mLazyDisplay;
    private ArrayList<OnUIPreListener> mOnUIPreListeners = new ArrayList<OnUIPreListener>(2);
    private int mOffsetOnScreen;

    private static final int DELAYCALLBACK = 50;
    private int mMaxInitOffset;
    private int mMaxUpdateOffset;
    private boolean mDesktopScrolling;

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return Return the View for the fragment's UI
     */
    public abstract View onInflaterContent(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    /**
     * Replace {@link #onDestroyView()}
     */
    public void onDestroyContent() {

    }

    /**
     * Call before switching animation
     *
     * @param gainSelect
     */
    public abstract void onFragmentSeletedPre(boolean gainSelect);

    /**
     * Completely visible or invisible.
     *
     * @param gainShow
     */
    public abstract void onFragmentShowChanged(boolean gainShow);

    /**
     * Completely visible or invisible.
     * If visible, callback with params.
     * <p/>
     * attach[0] = from
     * attach[1] = params
     *
     * @param gainShown
     * @param attach
     */
    public void onFragmentShowChanged(boolean gainShown, Object[] attach) {
        mLogger.i("==> onFragmentShowChanged [" + tag + "], attach=" + attach);
    }

    /**
     * Focus from other part to enter.
     *
     * @param requestDirection FOCUS_LEFT_IN,FOCUS_TOP_IN,FOCUS_RIGHT_IN,FOCUS_BOTTOM_IN
     * @return whether it has been handled
     */
    public abstract boolean onFocusRequested(int requestDirection);

    /**
     * If return true, frame will not handle home key this time.
     *
     * @return Whether consumed home key.
     */
    public abstract boolean onHomeKeyEventHandled();

    /**
     * In some cases you don't want to be drag out by key event, you can return false.
     *
     * @return whether can switch fragment
     */
    public boolean canKeyDragOut() {
        return true;
    }

    /**
     * In some cases you don't want to be drag out by touch event, you can return false.
     *
     * @return
     */
    public boolean canTouchDragOut() {
        return true;
    }

    /**
     * To tell you desktop scroll state.
     *
     * @param state BaseFragment.STATE_SCROLLING or BaseFragment.STATE_SCROLLING_END
     */
    public void onFragmentScrollStateChanged(int state) {
    }

    /**
     * To tell you distance from which is showing desktop offset has changed.
     *
     * @param offset
     */
    public void onFragmentOffsetChanged(int offset) {

    }

    public abstract void setHoverListener(View.OnHoverListener listener);

    /**
     * It can not be called by yourself.
     */
    public final void notifyFragmentShowChangedForce(boolean hasShow) {
        mBeNotifyShown = !hasShow;
        notifyFragmentShowChanged(hasShow);
    }

    /**
     * It can not be called by yourself.
     */
    public final void notifyFragmentShowChanged(boolean hasShow) {
        if (getActivity() == null) {
            return;
        }
        long start = System.currentTimeMillis();
        if (hasShow) {
            if (!mBeNotifyShown) {
                /** ========== In case not call onSelect before onShow ========== */
                if (!mBeNotifySelect) {
                    mInSelectedRunnable.run();
                }
                /** ============================================================= */

                if (mLazyDisplay) {
                    mContainer.setVisibility(View.VISIBLE);
                }

                mBeNotifyShown = true;
                onFragmentShowChanged(true);
                if (mFragmentHandler != null) {
                    mFragmentHandler.onFragmentAction(BaseFragment.this,
                            FragmentActionHandler.FRAGMENT_ACTION_FIRST_SHOWN_COMPLETED, null);
                }
            }
        } else {
            if (mBeNotifyShown) {
                mBeNotifyShown = false;
                onFragmentShowChanged(false);
            }
        }
        mLogger.d("==> onFragmentShowChanged  [" + tag + "] " + hasShow + ", take " + (System.currentTimeMillis() - start) + " ms");
    }

    /**
     * It can not be called by yourself.
     */
    public final void notifyFragmentShowChanged(boolean gainShown, Object[] attach) {
        if (getActivity() == null) {
            return;
        }
        long start = System.currentTimeMillis();
        mBeNotifyShown = gainShown;
        if (mLazyDisplay && mBeNotifyShown) {
            mContainer.setVisibility(View.VISIBLE);
        }
        onFragmentShowChanged(gainShown, attach);
        mLogger.d("==> onFragmentShowChanged withAttach [" + tag + "] " + gainShown + ", take " + (System.currentTimeMillis() - start) + " ms");
    }

    /**
     * It can not be called by yourself.
     */
    public final void notifyFragmentSelectPreImmediately(boolean hasSelect) {
        if (getActivity() == null) {
            return;
        }
        mHandler.removeCallbacks(mInSelectedRunnable);
        if (hasSelect) {
            mInSelectedRunnable.run();
        } else {
            if (mBeNotifySelect) {
                mBeNotifySelect = false;
                long start = System.currentTimeMillis();
                onFragmentSeletedPre(false);
                mLogger.d("==> onFragmentSelectedPre  [" + tag + "] false" + ", take " + (System.currentTimeMillis() - start) + " ms");
            }
        }
    }

    /**
     * It can not be called by yourself.
     */
    public final void notifyFragmentSelectPre(boolean hasSelect) {
        if (getActivity() == null) {
            return;
        }
        mHandler.removeCallbacks(mInSelectedRunnable);
        if (hasSelect) {
            mHandler.postDelayed(mInSelectedRunnable, DELAYCALLBACK);
        } else {
            if (mBeNotifySelect) {
                mBeNotifySelect = false;
                long start = System.currentTimeMillis();
                onFragmentSeletedPre(false);
                mLogger.d("==> onFragmentSelectedPre  [" + tag + "] false" + ", take " + (System.currentTimeMillis() - start) + " ms");
            }
        }
    }

    /**
     * Will callback when fragment remove from launcher thoroughly.
     * You should override this and release something.
     */
    public void onCrush() {
        mLogger.i("==> onFragmentCrush [" + tag + "]");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mContainer == null) {
            long start = System.currentTimeMillis();
            mContainer = onInflaterContent(inflater, container, savedInstanceState);
            mLogger.d("==> onInflaterContent [" + tag + "]" + ", take " + (System.currentTimeMillis() - start) + " ms");
            if (mLazyDisplay) {
                mContainer.setVisibility(View.INVISIBLE);
            }
            /**
             * We need to know which plugin use Fresco to request image url;
             * So we record plugin package name with fragment container set tag;
             * When container's item(Fresco.DraweeView) submitRequest, we can get item's parent(mContainer)
             * tag, the tag is mean which plugin package name submit request.
             *
             * -> pluginPkgName will init when new fragment;
             */
            mContainer.setTag(sPluginPackageNameSaveId, pluginPkgName);

            /**
             * record container focusable.
             */
            mContainerCanGetFocus = mContainer.isFocusable();

            /**
             * block focus when init.
             */
            setContentDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        }
        return mContainer;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mOnViewCreated = true;

        for (OnUIPreListener onUIPreListener : mOnUIPreListeners) {
            onUIPreListener.onUIPre(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        onDestroyContent();
        mOnViewCreated = false;
        if (mContainer != null) {
            ViewGroup parent = (ViewGroup) mContainer.getParent();
            if (parent != null) {
                parent.removeView(mContainer);
            }
            if (!isInstanceCacheEnabled()) {
                mContainer = null;
            }
        }

        for (OnUIPreListener onUIPreListener : mOnUIPreListeners) {
            onUIPreListener.onUIRecycle(this);
        }
    }

    /**
     * It can not be called by yourself.
     */
    @Deprecated
    public final void onActivityStart() {
    }

    /**
     * It can not be called by yourself.
     */
    @Deprecated
    public final void onActivityStop() {
    }

    /**
     * If you has state saved, next create the same fragment class,
     * will try to restore last state.
     *
     * @param enabled Whether enable to save fragment state
     */
    public void setSaveStateEnable(boolean enabled) {
        mFragmentSaveStateEnable = enabled;
    }

    /**
     * @return Whether enable to save fragment state
     */
    public boolean isSaveStateEnable() {
        return mFragmentSaveStateEnable;
    }

    /**
     * If current fragment be switch out, fragment will destroy view generally,
     * however, some special cases don't wish destroy and reCreate frequently,
     * than you can set instance cache enable.
     * <p/>
     * You should be more careful to use this, because it will occupancy memory.
     * <p/>
     * Deprecated. It is no longer recommended to use this}.
     *
     * @param enabled Whether save fragment instance
     */
    @Deprecated
    public void setInstanceCacheEnabled(boolean enabled) {
        // mFragmentCacheEnabled = enabled;
    }

    /**
     * @return Whether fragment instance cache enabled
     */
    @Deprecated
    public boolean isInstanceCacheEnabled() {
        return mFragmentCacheEnabled;
    }

    /**
     * Set the descendant focusability of this fragment root view group. This defines the
     * relationship between this view group and its descendants when looking for a view to
     * take focus in {@link ViewGroup#requestFocus(int, android.graphics.Rect)}.
     *
     * @param focusability one of
     *                     {@link ViewGroup#FOCUS_BEFORE_DESCENDANTS},
     *                     {@link ViewGroup#FOCUS_AFTER_DESCENDANTS},
     *                     {@link ViewGroup#FOCUS_BLOCK_DESCENDANTS}.
     */
    public void setContentDescendantFocusability(int focusability) {
        if (mContainer != null && mContainer instanceof ViewGroup) {
            ((ViewGroup) mContainer).setDescendantFocusability(focusability);
            if (mContainerCanGetFocus) {
                mContainer.setFocusable(focusability != ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof FragmentActionHandler) {
            mFragmentHandler = (FragmentActionHandler) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentHandler = null;
    }

    /**
     * Action from launcher activity.
     *
     * @param what
     * @param arg
     * @return handle result
     */
    public Object onActivityAction(int what, Object arg) {
        switch (what) {
            case ActivityActionHandler.ACTIVITY_ACTION_DESKTOP_PAGE_STATUS:
                return 0;
            case ActivityActionHandler.ACTIVITY_ACTION_CHECK_HAND_STATUS:
                return true;
        }
        return null;
    }

    /**
     * It can not be called by yourself.
     */
    public final void offStateBeforeDesktroy() {
        if (mBeNotifySelect || mBeNotifyShown) {
            long start = System.currentTimeMillis();
            if (mBeNotifySelect) {
                mBeNotifySelect = false;
                onFragmentSeletedPre(false);
            }
            if (mBeNotifyShown) {
                mBeNotifyShown = false;
                onFragmentShowChanged(false);
            }
            mLogger.d("==> offStateBeforeDesktroy [" + tag + "]" + ", take " + (System.currentTimeMillis() - start) + " ms");
        }
    }

    /**
     * Destroy root view.
     */
    public final void destoryViewInstance() {
        if (mContainer != null && mContainer instanceof ViewGroup) {
            ((ViewGroup) mContainer).removeAllViews();
            mContainer.destroyDrawingCache();
        }
        mContainer = null;
    }

    /**
     * @return fragment root view
     */
    public View getContainer() {
        return mContainer;
    }

    /**
     * No longer use.
     */
    @Deprecated
    public final void setAsMainScreen(boolean asMain) {
    }

    /**
     * @return is as the home screen.
     */
    public boolean isAsMainScreen() {
        return false;
    }

    /**
     * It can not be called by yourself.
     */
    public final void setOffsetOnScreen(int count) {
        long start = System.currentTimeMillis();
        mOffsetOnScreen = count;
        onFragmentOffsetChanged(mOffsetOnScreen);
        mLogger.d("==> onFragmentOffsetChanged [" + tag + "]" + ", take " + (System.currentTimeMillis() - start) + " ms");
    }

    /**
     * @return The distance to being show fragment.
     */
    public final int getOffsetOnScreen() {
        return mOffsetOnScreen;
    }

    /**
     * @return fragment container view has created or not
     */
    public final boolean isViewCreated() {
        return mOnViewCreated;
    }

    /**
     * @return fragment has {@link BaseFragment#onFragmentShowChanged(boolean)} true
     */
    public final boolean isShown() {
        return mBeNotifyShown;
    }

    /**
     * @return fragment has {@link BaseFragment#onFragmentSeletedPre(boolean)} } true
     */
    public final boolean isSelected() {
        return mBeNotifySelect;
    }

    /**
     * It can not be called by yourself.
     * <p>
     * If lazy load, will pre set container View.INVISIBL
     *
     * @param lazy
     */
    public final void setLazyDisplay(boolean lazy) {
        mLazyDisplay = lazy;
    }

    /**
     * It can not be called by yourself.
     */
    public final void addUIPreListener(OnUIPreListener l) {
        if (l != null) {
            if (mOnViewCreated) {
                l.onUIPre(this);
            }
            if (!mOnUIPreListeners.contains(l)) {
                mOnUIPreListeners.add(l);
            }
        }
    }

    public final void clearUIPreListener() {
        mOnUIPreListeners.clear();
    }

    /**
     * It can not be called by yourself.
     */
    public final void setFragmentScrollState(int state) {
        if (getActivity() == null) {
            return;
        }
        mDesktopScrolling = state == BaseFragment.STATE_SCROLLING;
        onFragmentScrollStateChanged(state);
    }

    /**
     * It can not be called by yourself.
     */
    public final void setMaxInitOffset(int offset) {
        mMaxInitOffset = offset;
    }

    /**
     * It can not be called by yourself.
     */
    public final void setMaxUpdateOffset(int offset) {
        mMaxUpdateOffset = offset;
    }

    /**
     * Returns the enabled status for this fragment data/UI init.
     *
     * @return True if this fragment allow data/UI init, false otherwise.
     */
    protected boolean isInitEnabled() {
        return mOffsetOnScreen <= mMaxInitOffset;
    }

    /**
     * Returns the enabled status for this fragment data/UI update.
     *
     * @return True if this fragment allow data/UI update, false otherwise.
     */
    protected final boolean isUpdateEnabled() {
        return mOffsetOnScreen <= mMaxUpdateOffset;
    }

    /**
     * Whether the desktop is switching.
     */
    protected final boolean isFragmentScrolling() {
        return mDesktopScrolling;
    }
}