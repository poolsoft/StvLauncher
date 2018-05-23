package com.stv.plugin.demo.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.stv.plugin.demo.DemoApplication;
import com.stv.plugin.demo.data.DataManager;
import com.stv.plugin.demo.data.common.OnDataChangedListener;
import com.stv.plugin.demo.data.common.PosterHolder;
import com.stv.plugin.demo.util.IdleTaskLooper;
import com.stv.plugin.demo.widget.RootLayoutContainer;
import com.xstv.base.BaseFragment;
import com.xstv.base.Logger;
import com.xstv.desktop.R;

public class DemoFragment extends BaseFragment implements OnDataChangedListener {

    private Logger mLogger = Logger.getLogger(DemoApplication.PLUGINTAG, "DemoFragment");
    private RootLayoutContainer mLayoutContainer;

    private boolean hasShown;
    private DataManager mDataManager;
    private IdleTaskLooper mIdleTaskLooper;

    public DemoFragment() {
        mLogger.d("create fragment instance");
        mDataManager = DataManager.getInstance();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mIdleTaskLooper = new IdleTaskLooper();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        hasShown = false;
        mIdleTaskLooper.cancelAll();
        mIdleTaskLooper = null;
        mLayoutContainer = null;
    }

    /**
     * Inflater layout from plugin, need use widget context.
     *
     * @return fragment root view.
     */
    @Override
    public View onInflaterContent(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        mLogger.d(">>> onInflaterContent <<<");
        LayoutInflater widgetInflater = LayoutInflater.from(DemoApplication.sWidgetApplicationContext);
        mLayoutContainer = (RootLayoutContainer) widgetInflater.inflate(R.layout.demo_fragment_layout, null);
        mLayoutContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mLayoutContainer.bindFragment(this);
        return mLayoutContainer;
    }

    /**
     * onStart 表明桌面应用在前台
     */
    @Override
    public void onStart() {
        super.onStart();
        mLogger.d(">>> onStart <<<");
        mDataManager.setOnDataChangedListener(this);
        mDataManager.startTimer2RefreshData();
    }

    /**
     * onStop除了在fragment销毁前会调用外，桌面应用进入后台时，所有的fragment也会收到onStop
     * 在stop状态时，应该停掉数据的拉取及UI的刷新
     */
    @Override
    public void onStop() {
        super.onStop();
        mLogger.d(">>> onStop <<<");
        mDataManager.setOnDataChangedListener(null);
        mDataManager.stopTimer2RefreshData();
        mIdleTaskLooper.cancelAll();
    }

    /**
     * Widget be removed from launcher tab, need to release all resources.
     * Fragment在桌面管理中被取消时，除了正常走销毁周期onStop->onDestroy->onDetach外，还会调
     * 用此方法
     */
    @Override
    public void onCrush() {
        super.onCrush();
        mLogger.d(">>> onCrush <<<");
        mDataManager.destroy();
        mDataManager = null;
    }

    /**
     * Call before switching animation
     *
     * @param gainSelect true will be switch in, false will be switch out.
     */
    @Override
    public void onFragmentSeletedPre(boolean gainSelect) {
        if (mLayoutContainer != null) {
            mLayoutContainer.onLayoutSeletedPre(gainSelect);
        }
    }

    /**
     * Completely visible or invisible
     */
    @Override
    public void onFragmentShowChanged(boolean gainShow) {
        hasShown = gainShow;
        if (mLayoutContainer != null) {
            mLayoutContainer.onLayoutShowChanged(gainShow);
        }
    }

    /**
     * Desktop scroll state.
     */
    @Override
    public void onFragmentScrollStateChanged(int state) {
        if (mIdleTaskLooper != null) {
            mIdleTaskLooper.onUIScrollStateChanged(state == BaseFragment.STATE_SCROLLING);
        }
    }

    @Override
    public boolean onFocusRequested(int direction) {
        return isActive() && mLayoutContainer.onFocusRequested(direction);
    }

    @Override
    public boolean onHomeKeyEventHandled() {
        return false;
    }

    @Override
    public void setHoverListener(View.OnHoverListener onHoverListener) {

    }

    private boolean isActive() {
        return mLayoutContainer != null;
    }

    /**
     * DistanceShowCount： 当前fragment与屏幕中正在显示的fragment的角标距离
     *
     * @return 是否允许刷新
     */
    private boolean allowRefresh() {
        return hasShown || getOffsetOnScreen() <= 1;
    }

    @Override
    public void onDataInitialize(final PosterHolder posterDefault) {
        mLogger.d("### onDataInitialize ###");
        if (isActive()) {
            mIdleTaskLooper.cancelAll();
        /*mIdleTaskLooper.addToFirst(new IdleTaskLooper.Impl() {
            @Override
            public void _do() {
                mLayoutContainer.bindData(posterDefault);
            }
        });*/
            mLayoutContainer.bindData(posterDefault);
        }
    }

    @Override
    public void onDataChange(final PosterHolder posterCache) {
        mLogger.d("### onDataChange ###");
        if (isActive() && allowRefresh()) {
            mIdleTaskLooper.cancelAll();
            mIdleTaskLooper.addToFirst(new IdleTaskLooper.Impl() {
                @Override
                public void _do() {
                    //mLayoutContainer.bindData(posterCache);
                }
            });
        }
    }

    @Override
    public void onRefreshTimeLess(int time) {
        if (isActive() && hasShown) {
            //mLayoutContainer.updateRefreshTimeLess(mDataManager.getRefreshThreshold(), time);
        }
    }
}
