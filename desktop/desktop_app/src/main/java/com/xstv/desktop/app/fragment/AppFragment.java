
package com.xstv.desktop.app.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.xstv.library.base.ActivityActionHandler;
import com.xstv.library.base.BaseFragment;
import com.xstv.library.base.FragmentActionHandler;
import com.xstv.library.base.LetvLog;
import com.xstv.desktop.app.AppPluginActivator;
import com.xstv.desktop.app.R;
import com.xstv.desktop.app.bean.ContentBean;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.interfaces.DataChangeObserver;
import com.xstv.desktop.app.interfaces.IAppFragment;
import com.xstv.desktop.app.listener.OnDataChangeListener;
import com.xstv.desktop.app.presenter.FragmentPresenter;
import com.xstv.desktop.app.util.BitmapCache;
import com.xstv.desktop.app.util.IconFilterUtil;
import com.xstv.desktop.app.util.LauncherState;
import com.xstv.desktop.app.util.Utilities;
import com.xstv.desktop.app.widget.AppFolderWorkspace;
import com.xstv.desktop.app.widget.AppWorkspace;
import com.xstv.desktop.app.widget.BaseWorkspace;

import java.lang.ref.WeakReference;
import java.util.List;

public class AppFragment extends BaseFragment implements OnDataChangeListener, IAppFragment {
    private static final String TAG = AppFragment.class.getSimpleName();

    private AppWorkspace mAppWorkspace;
    private AppFolderWorkspace mAppFolderWorkspace;

    private FragmentPresenter mPresenter;

    private View mRootView;
    private View mLoadingView;

    //for optimize overdraw
    private FrameLayout mViewRoot;

    private Handler mHandler = new Handler();
    private Runnable mRefreshUIRunnable;

    private boolean isRefreshedUI;
    private boolean gainSelect;
    private boolean gainShow;
    private boolean isUseNewLifecycle;

    private boolean isKeyDragOut = true;
    private boolean isTouchDragOut = true;

    @Override
    public View onInflaterContent(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LauncherState.getInstance().setHostContext(inflater.getContext());
        BitmapCache.getInstance().setIsRelease(false);
        LayoutInflater pluginInflater = LayoutInflater.from(AppPluginActivator.getContext());
        View rootView = pluginInflater.inflate(R.layout.app_fragment_layout, null, false);
        isUseNewLifecycle = Utilities.verifySupportSdk(Utilities.support_sdk_version_102);
        LetvLog.i(TAG, "=== onInflaterContent ===isUseNewLifecycle: " + isUseNewLifecycle);

        mAppWorkspace = (AppWorkspace) rootView.findViewById(R.id.app_workspace);
        mAppWorkspace.setAppFragment(this);

        mAppFolderWorkspace = (AppFolderWorkspace) rootView.findViewById(R.id.app_folder_workspace);
        mAppFolderWorkspace.setOnDataChangeListener(mAppWorkspace);
        mAppFolderWorkspace.setAppFragment(this);

        mPresenter = new FragmentPresenter();
        mPresenter.attachView(this);
        mPresenter.registerDataObserver(new WeakReference<DataChangeObserver>(mAppWorkspace));
        mPresenter.registerDataObserver(new WeakReference<DataChangeObserver>(mAppFolderWorkspace));

        mRootView = rootView;

        Context context = LauncherState.getInstance().getHostContext();
        int id = context.getResources().getIdentifier("activity_root", "id", context.getPackageName());
        mViewRoot = (FrameLayout) getActivity().findViewById(id);

        if (!isUseNewLifecycle) {
            handlerData();
        }

        return rootView;
    }

    @Override
    public AppWorkspace getAppWorkspace() {
        return mAppWorkspace;
    }

    @Override
    public AppFolderWorkspace getFolderWorkspace() {
        return mAppFolderWorkspace;
    }

    @Override
    public void onFragmentSeletedPre(boolean gainSelect) {
        LetvLog.i(TAG, "onFragmentSeletedPre gainSelect = " + gainSelect + " gainShow = " + gainShow);
        this.gainSelect = gainSelect;
        if (!gainSelect) {
            if (!gainShow) {
                if (mAppFolderWorkspace != null && (mAppFolderWorkspace.getVisibility() == View.VISIBLE)) {
                    mAppFolderWorkspace.hide();
                }
                if (mAppWorkspace != null && (mAppWorkspace.getVisibility() != View.VISIBLE)) {
                    mAppWorkspace.show();
                }
            }
        }
    }

    public void onFragmentOffsetChanged(int offset) {
        if (!isUseNewLifecycle) {
            return;
        }
        if (mRootView != null) {
            handlerData();
        }
    }

    private void handlerData() {
        if (mPresenter == null) {
            return;
        }

        LetvLog.i(TAG, "handlerData isRefreshedUI = " + isRefreshedUI + " isCanUpdate() = "
                + isCanUpdate() + " isCanInit() = " + isCanInit());

        if (isRefreshedUI) {
            if (isCanUpdate()) {
                //更新数据
                mPresenter.updateData();
            }
        } else {
            if (isCanInit()) {
                //加载数据
                mPresenter.loadData();
            }
        }
    }

    @Override
    public void onFragmentShowChanged(boolean gainShow) {
        this.gainShow = gainShow;
        LetvLog.d(TAG, "onFragmentShowChanged gainShow = " + gainShow);
        if (mAppWorkspace == null || mAppFolderWorkspace == null) {
            LetvLog.i(TAG, "onFragmentShowChanged view is Null");
            return;
        }
        LauncherState.getInstance().setVisibleToUser(gainShow);
        mAppWorkspace.onUserVisibleHint(gainShow);
        mAppFolderWorkspace.onUserVisibleHint(gainShow);
        if (gainShow) {
            //DownloadAppPresenter.getInstance().bindService();
            if (mFragmentHandler != null) {
                mFragmentHandler.onFragmentAction(this, FragmentActionHandler.FRAGMENT_ACTION_CHECK_HAND_DETECT_ENTER, null);
            }

            // for use theme
            if (mRootView != null) {
                if (IconFilterUtil.isUsedTheme()) {
                    mRootView.setBackgroundResource(R.drawable.app_fragment_background);
                } else {
                    mRootView.setBackground(null);
                }
            }
            removeRootViewBg();
        } else {
            String screen = "";
            String packageName = AppPluginActivator.getContext().getPackageName();
            LetvLog.i(TAG, "onFragmentShowChanged gainSelect " + gainSelect + " screen = " + screen +
                    " packageName = " + packageName);
            if (!gainSelect || !packageName.equals(screen)) {// 不在应用桌面（不包括点击进入应用的情况）
                if (mAppFolderWorkspace != null && (mAppFolderWorkspace.getVisibility() == View.VISIBLE)) {
                    mAppFolderWorkspace.hide();
                }
                if (mAppWorkspace != null && (mAppWorkspace.getVisibility() != View.VISIBLE)) {
                    mAppWorkspace.show();
                }
                LauncherState.getInstance().setAppFocusTag(null);
                LauncherState.getInstance().setAppInFolderFocusTag(null);
            }
        }
    }

    private void removeRootViewBg() {
        if (mViewRoot == null) {
            return;
        }
        mViewRoot.postDelayed(new Runnable() {
            @Override
            public void run() {
                View view = mViewRoot.getChildAt(0);
                LetvLog.i(TAG, " find index 0 view = " + view);
                /*if (view instanceof SimpleDraweeView && view.getVisibility() == View.VISIBLE) {
                    LetvLog.i(TAG, " remove bg ");
                    if(getActivity() != null){
                        getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(0));
                    }else{
                        LetvLog.w(TAG, "getActivity return null.");
                    }
                    mViewRoot.setBackgroundResource(0);
                    if(mRootView != null){
                        mRootView.setBackground(null);
                    }
                }*/
            }
        }, 1000);
    }

    public void onFragmentShowChanged(boolean gainShow, Object[] objects) {
        LetvLog.d(TAG, "onFragmentShowChanged with param objects = " + objects);
        if (mRootView == null) {
            return;
        }
        if (!this.gainShow) {
            onFragmentShowChanged(gainShow);
        }
    }

    @Override
    public boolean onFocusRequested(int requestDirection) {
        LetvLog.i(TAG, "onFocusRequested requestDirection = " + requestDirection);
        if (mAppWorkspace == null) {
            return false;
        }
        boolean consumed = false;
        int direction = View.FOCUS_DOWN;
        if (requestDirection == FOCUS_TOP_IN) {
            direction = View.FOCUS_DOWN;
        } else if (requestDirection == FOCUS_LEFT_IN) {
            direction = View.FOCUS_LEFT;
        } else if (requestDirection == FOCUS_RIGHT_IN) {
            direction = View.FOCUS_RIGHT;
        }
        if (mAppWorkspace.getVisibility() == View.VISIBLE) {
            consumed = mAppWorkspace.onFocus(direction);
        }
        return consumed;
    }

    /**
     * Call when home_key press,if you have consumed this key return true.
     *
     * @return true : not back to live. false : back to live.
     */
    @Override
    public boolean onHomeKeyEventHandled() {
        LetvLog.d(TAG, " onHomeKeyEventHandled AppWorkspace.mState = " + AppWorkspace.mState +
                " AppFolderWorkspace.mState = " + AppFolderWorkspace.mState);
        if (mAppFolderWorkspace != null && (mAppFolderWorkspace.getVisibility() == View.VISIBLE ||
                AppWorkspace.mState == BaseWorkspace.State.STATE_FOLDER_OPENED)) {
            mAppFolderWorkspace.hide();
            return true;
        }

        if (mAppWorkspace != null) {
            boolean isBackToHome = mAppWorkspace.backToHome();
            if (!isBackToHome) {
                if (isAsMainScreen()) {
                    mAppWorkspace.resetScrollAndBackToTab();
                }
            }
            return isBackToHome;
        }
        return false;
    }

    @Override
    public void setHoverListener(View.OnHoverListener onHoverListener) {
        if (mAppWorkspace != null) {
            mAppWorkspace.setOnHoverListener(onHoverListener);
        }
    }

    @Override
    public void onShowLoading() {
        if (mLoadingView == null) {
            Context context = AppPluginActivator.getContext();
            mLoadingView = View.inflate(context, R.layout.workspace_loading, null);
        }
        if (((ViewGroup) mRootView).indexOfChild(mLoadingView) == -1) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mLoadingView.getLayoutParams();
            if (params == null) {
                params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
            }
            ((ViewGroup) mRootView).addView(mLoadingView, params);
        }
    }

    @Override
    public void onHideLoading() {
        if (mLoadingView != null && mLoadingView.getVisibility() == View.VISIBLE && (((ViewGroup) mRootView)
                .indexOfChild(mLoadingView) != -1)) {
            mLoadingView.setVisibility(View.INVISIBLE);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ((ViewGroup) mRootView).removeView(mLoadingView);
                    mLoadingView = null;
                }
            });

        }
    }

    @Override
    public void onNotifyUI(final List<ContentBean> contentBeanList, final boolean isHasServerData, final List<ItemInfo> itemInfoList) {
        LetvLog.i(TAG, "onNotifyUI isFragmentScrolling = " + isScrolling() + " isRefreshedUI = " + isRefreshedUI);

        if (!isScrolling()) {// 能刷新ui
            LetvLog.i(TAG, "onNotifyUI success");
            notifyUIInternal(contentBeanList, isHasServerData, itemInfoList);
        } else {
            mHandler.removeCallbacks(mRefreshUIRunnable);
            mRefreshUIRunnable = new Runnable() {
                @Override
                public void run() {
                    LetvLog.d(TAG, "onNotifyUI retry data");
                    if (!isScrolling()) {
                        notifyUIInternal(contentBeanList, isHasServerData, itemInfoList);
                    } else {
                        mHandler.postDelayed(mRefreshUIRunnable, 100);
                    }
                }
            };
            mHandler.postDelayed(mRefreshUIRunnable, 100);
        }
    }

    private void notifyUIInternal(List<ContentBean> contentBeanList, boolean ishasServerData, List<ItemInfo> itemInfoList) {
        if (isRefreshedUI && ishasServerData) {
            if (mAppWorkspace != null) {
                mAppWorkspace.updateHeader(contentBeanList);
            }
            return;
        }

        if (itemInfoList == null) {
            LetvLog.e(TAG, "notifyUIInternal itemInfoList is null.");
            return;
        }

        isRefreshedUI = true;
        if (mAppWorkspace != null) {
            mAppWorkspace.setData(contentBeanList, itemInfoList);
        }
        onHideLoading();
        // 数据加载完成后使用onFragmentAction接口将消息发送给框架。
        String packageName = AppPluginActivator.getContext().getPackageName();
        String params = "normal";
        if (itemInfoList == null || itemInfoList.size() == 0) {
            params = "error";
        }
        if (mFragmentHandler != null) {
            mFragmentHandler.onFragmentAction(this, 15, packageName + "|" + params);
        }
    }

    @Override
    public void showStatusbar() {
        LetvLog.i(TAG, "showStatusbar");
        if (mFragmentHandler != null) {
            mFragmentHandler.onFragmentAction(this, FragmentActionHandler.FRAGMENT_ACTION_SHOW_STATUSBAR, null);
        }
    }

    @Override
    public void hideStatusBar() {
        LetvLog.i(TAG, "hideStatusBar");
        if (mFragmentHandler != null) {
            mFragmentHandler.onFragmentAction(this, FragmentActionHandler.FRAGMENT_ACTION_HIDE_STATUSBAR, null);
        }
    }

    @Override
    public void showTabView() {
        LetvLog.i(TAG, "showTabView");
        if (mFragmentHandler != null) {
            mFragmentHandler.onFragmentAction(this, FragmentActionHandler.FRAGMENT_ACTION_SHOW_TAB, null);
        }
    }

    @Override
    public void hideTabView() {
        LetvLog.i(TAG, "hideTabView");
        if (mFragmentHandler != null) {
            mFragmentHandler.onFragmentAction(this, FragmentActionHandler.FRAGMENT_ACTION_HIDE_TAB, null);
        }
    }

    @Override
    public Object onActivityAction(int what, Object arg) {
        switch (what) {
            case ActivityActionHandler.ACTIVITY_ACTION_DESKTOP_MOVE_DOWN:
                if (mAppWorkspace != null && mAppWorkspace.getVisibility() == View.VISIBLE) {
                    mAppWorkspace.scrollDownByHandDetect();
                }
                break;
            case ActivityActionHandler.ACTIVITY_ACTION_DESKTOP_MOVE_UP:
                if (mAppWorkspace != null && mAppWorkspace.getVisibility() == View.VISIBLE) {
                    mAppWorkspace.scrollUpByHandDetect();
                }
                break;
            case ActivityActionHandler.ACTIVITY_ACTION_DESKTOP_PAGE_STATUS:
                if (mAppWorkspace != null && mAppWorkspace.getVisibility() == View.VISIBLE) {
                    return mAppWorkspace.getPageStatus();
                }
                break;
            case ActivityActionHandler.ACTIVITY_ACTION_CHECK_HAND_STATUS:
                if (mAppFolderWorkspace != null && mAppFolderWorkspace.isShown()) {
                    return false;
                } else {
                    return true;
                }
                // ActivityActionHandler.ACTIVITY_ACTION_STR_MODE_OFF = 9
            case 9:
                LetvLog.i(TAG, "onActivityAction ACTIVITY_ACTION_STR_MODE_OFF");
                isRefreshedUI = false;
                if (mAppFolderWorkspace != null) {
                    mAppFolderWorkspace.hide();
                }
                if (mAppWorkspace != null) {
                    mAppWorkspace.show();
                }
                break;
        }
        return super.onActivityAction(what, arg);
    }

    @Override
    public void startAppAnim() {
        if (mFragmentHandler != null) {
            //FRAGMENT_ACTION_START_ACTIVITY_ANIM = 17
            mFragmentHandler.onFragmentAction(this, 17, null);
        }
    }

    @Override
    public void backToTab() {
        if (mFragmentHandler != null) {
            mFragmentHandler.onFragmentAction(this, FragmentActionHandler.FRAGMENT_ACTION_BACK_KEY, null);
        }
    }

    @Override
    public void checkHandDetectEnter() {
        if (mFragmentHandler != null) {
            mFragmentHandler.onFragmentAction(this, FragmentActionHandler.FRAGMENT_ACTION_CHECK_HAND_DETECT_ENTER, null);
        }
    }

    @Override
    public boolean canKeyDragOut() {
        return isKeyDragOut;
    }

    @Override
    public boolean canTouchDragOut() {
        return isTouchDragOut;
    }

    @Override
    public void setKeyDragOut(boolean isKeyDragOut) {
        this.isKeyDragOut = isKeyDragOut;
    }

    @Override
    public void setTouchDragOut(boolean is) {
        this.isTouchDragOut = is;
    }

    private boolean isCanInit() {
        if (isUseNewLifecycle) {
            return isInitEnabled();
        }
        return true;
    }

    private boolean isCanUpdate() {
        if (isUseNewLifecycle) {
            return isUpdateEnabled();
        }
        return true;
    }

    private boolean isScrolling() {
        if (isUseNewLifecycle) {
            return isFragmentScrolling();
        }
        return false;
    }

    @Override
    public void onDestroyContent() {
        super.onDestroyContent();
        mHandler.removeCallbacksAndMessages(null);
        if (!Utilities.verifySupportSdk("1.0.3")) {
            LetvLog.i(TAG, "onDestroyContent sdk <= 1.0.3 is not destory");
            return;
        }
        LetvLog.i(TAG, "=== onDestroyContent ===");
        isRefreshedUI = false;
        if (mAppWorkspace != null) {
            mAppWorkspace.setAppFragment(null);
            mAppWorkspace.onRelease();
            mAppWorkspace = null;
        }
        if (mAppFolderWorkspace != null) {
            mAppFolderWorkspace.setAppFragment(null);
            mAppFolderWorkspace.setOnDataChangeListener(null);
            mAppFolderWorkspace.onRelease();
            mAppFolderWorkspace = null;
        }
        if (mPresenter != null) {
            mPresenter.release();
        }
        if (mRootView != null) {
            mRootView.setBackground(null);
            mRootView = null;
        }
        BitmapCache.getInstance().setIsRelease(true);
        BitmapCache.getInstance().releaseCache();
        LauncherState.getInstance().setHostContext(null);
        mLoadingView = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LetvLog.i(TAG, "=== onDetach ===");
    }

    @Override
    public void onCrush() {
        super.onCrush();
        LetvLog.i(TAG, "=== onCrush ===");
        mPresenter.crush();
        mPresenter = null;
    }
}
