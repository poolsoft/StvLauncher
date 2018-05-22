
package com.xstv.launcher.ui.widget;

import android.content.Context;

import com.xstv.base.BaseFragment;
import com.xstv.base.LetvLog;
import com.xstv.launcher.provider.db.ScreenInfo;


public class FragmentPresenter implements IWidgetPresenter {

    public interface FragmentPresenterCallback {
        Context getContext();

        BaseFragment getCurrentFragment();

        void onUpgradeFinished(FragmentPresenter presenter);

        void onRequestItemAttach(FragmentPresenter presenter);

        void onFirstLoadFailed();

        boolean isBootIntoSignalFirst();

        boolean isPageScrolling();

        boolean isActivityStop();
    }

    private static final String TAG = "FragmentPresenter";

    public ScreenInfo info;
    private BaseFragment mFragment;
    private int mOffsetOnScreen = -1;
    private FragmentPresenterCallback mFragmentPresenterCallback;

    public FragmentPresenter(ScreenInfo info, FragmentPresenterCallback l) {
        this.info = new ScreenInfo().copy(info);
        mFragmentPresenterCallback = l;
    }

    @Override
    public BaseFragment create() {
        if (mFragment != null) {
            LetvLog.w(TAG, ">>> Old fragment has not be destroy before create an new one <<<", null);
        }
        mFragment = FragmentCreateHelper.createNativeFragment(mFragmentPresenterCallback.getContext(), info);
        checkNeedNotifyFragment();
        return mFragment;
    }

    @Override
    public void destroy() {
        if (mFragment != null && !mFragment.isInstanceCacheEnabled()) {
            mFragment.destoryViewInstance();
            mFragment = null;
        }
    }

    private void checkNeedNotifyFragment() {
        if (!mFragmentPresenterCallback.isBootIntoSignalFirst() && mFragment != null) {
            if (mFragment.isViewCreated()) {
                LetvLog.d(TAG, "fragment[" + mFragment.tag + "] ui is pre, call showDefault");
                mCanNotifyFragmentListener.onUIPre(mFragment);
            } else {
                LetvLog.d(TAG, "fragment[" + mFragment.tag + "] ui is not pre, wait");
                mFragment.addUIPreListener(mCanNotifyFragmentListener);
            }
        }
    }

    // return an instance if it has created.
    public BaseFragment getFragment() {
        return mFragment;
    }

    public void setOffsetOnScreen(int offset) {
        mOffsetOnScreen = offset;
        if (mFragment != null) {
            //FIXME-xubin mFragment.setOffsetOnScreen(offset);
        }
    }

    String getPluginVersion() {
        if (info != null) {
            return info.getVersionCode();
        }
        return "";
    }

    BaseFragment getFragmentNonNull() {
        if (mFragment == null) {
            create();
        }
        return mFragment;
    }

    private final BaseFragment.OnUIPreListener mCanNotifyFragmentListener = new BaseFragment.OnUIPreListener() {

        @Override
        public void onUIPre(BaseFragment fragment) {
            if (fragment != null && mFragmentPresenterCallback != null) {
                boolean isScrolling = mFragmentPresenterCallback.isPageScrolling();
                boolean isCurrent = mFragmentPresenterCallback.getCurrentFragment() == fragment;
                //===================================================================
                // When create an fragment first time, we need do that:
                // 1. notify current scroll state.
                fragment.setFragmentScrollState(mFragmentPresenterCallback.isPageScrolling() ? BaseFragment.STATE_SCROLLING : BaseFragment.STATE_SCROLLING_END);
                // 2. notify select state if it's current item.
                if (!fragment.isSelected() && isCurrent) {
                    fragment.notifyFragmentSelectPreImmediately(true);
                }
                // 3. notify current offset.
                mFragment.setOffsetOnScreen(mOffsetOnScreen);
                // 4. notify shown state if it's current item.
                if (!fragment.isShown() && mFragmentPresenterCallback.getCurrentFragment() == fragment && !mFragmentPresenterCallback.isPageScrolling()) {
                    if (!mFragmentPresenterCallback.isActivityStop()) {
                        fragment.notifyFragmentShowChanged(true);
                    } else {
                        LetvLog.i(TAG, "Current on background, Ignore show");
                    }
                }
                //====================================================================
            }
        }

        @Override
        public void onUIRecycle(BaseFragment fragment) {

        }
    };
}
