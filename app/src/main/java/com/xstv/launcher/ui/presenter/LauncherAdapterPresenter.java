
package com.xstv.launcher.ui.presenter;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.xstv.launcher.logic.manager.DataModel;
import com.xstv.launcher.provider.db.ScreenInfo;
import com.xstv.launcher.ui.activity.Launcher;
import com.xstv.launcher.ui.widget.FragmentPresenter;
import com.xstv.launcher.ui.widget.ITabStrip;
import com.xstv.launcher.ui.widget.LauncherAdapter;
import com.xstv.launcher.ui.widget.TabActionHandler;
import com.xstv.launcher.ui.widget.TabPagerBindStrategy;
import com.xstv.launcher.ui.widget.TabSpace;
import com.xstv.launcher.ui.widget.TabStripImpl;
import com.xstv.launcher.ui.widget.ViewPagerSpace;
import com.xstv.library.base.BaseFragment;
import com.xstv.library.base.LetvLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class LauncherAdapterPresenter implements TabActionHandler, TabSpace.OnTabChangedListener,
        OnPageChangeListener, TabPagerBindStrategy {

    static final int MSG_UPDATE_FRAGMENT_VISIBLE = 10;
    static final int MSG_SET_DESKTOP = 11;
    static final int MSG_SET_FRAGMENT_OFFSET = 12;
    private int msgDelaytime = 2000;

    public interface OnScreenSwitchedListener {
        void onScreenSelected(String screenTag, int index, int total);

        void onScreenScrolling(int state);
    }

    abstract class HideNewTipsTimeTask implements Runnable {
        int index;
        // pkg name
        String tag;

        public void setTarget(int index, String tag) {
            this.index = index;
            this.tag = tag;
        }
    }

    private static final String TAG = LauncherAdapterPresenter.class.getSimpleName();

    private TabSpace mTabSpace;
    private ViewPagerSpace mViewPagerSpace;
    private Launcher mLauncher;
    private LauncherAdapter mAdapter;
    private MyFragmentPresenterCallback mFragmentPresenterCallback;

    private int mLastTab = -1;
    private int mCurrentTab = -1;
    private int mCurrentShownTab;
    private List<OnScreenSwitchedListener> mOnScreenSwitchedListeners = new ArrayList<OnScreenSwitchedListener>(2);
    private MyWeakHandler mHandler = new MyWeakHandler(this);
    private boolean hasCheckFirstLoadCompleted;
    private int mFirstInPosition;
    private static final boolean DEBUG = true;
    private boolean mIsScrolling;


    public LauncherAdapterPresenter(Launcher activity, TabSpace tabSpace, ViewPagerSpace viewPagerSpace) {
        mLauncher = activity;
        mTabSpace = tabSpace;
        mViewPagerSpace = viewPagerSpace;
        mAdapter = new LauncherAdapter(activity, viewPagerSpace);
        mFragmentPresenterCallback = new MyFragmentPresenterCallback();
        mTabStrip = tabSpace.getTabStrip();

        viewPagerSpace.setAdapter(mAdapter);
        viewPagerSpace.setOffscreenPageLimit(10);
        // ViewPager's action to bind with mTabStrip
        setViewPager(viewPagerSpace);
        mTabStrip.setBindStrategy(this);
        tabSpace.setOnTabChangedListener(this);
        tabSpace.setTabActionHandler(this);
    }

    public void bindScreens(List<ScreenInfo> orderedScreens, String lastScreenSelect) {
        LetvLog.d(TAG, "### bindScreens " + orderedScreens);
        if (orderedScreens != null && orderedScreens.size() > 0) {
            clear();

            for (ScreenInfo screen : orderedScreens) {
                addTab(screen, null, false);
            }

            /**
             * Default into screen is boot-into, if it's null, default is index=0;
             */
            int firstIndex = 0;
            ScreenInfo firstIntoScreen = mAdapter.getList().get(firstIndex).info;
            firstIntoScreen.isFristIn = true;
            mHandler.sendEmptyMessage(MSG_SET_DESKTOP);
            LetvLog.i(TAG, "bindScreens firstIntoIndex=" + firstIndex + " name=" + firstIntoScreen.getName());
            firstNotifyDataSetChanged(firstIndex);
            mFirstInPosition = firstIndex;

            final int pagerIndex = firstIndex;
            /**
             * Post delay in order to request focus on tab success.
             */
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mTabSpace == null || (mLauncher != null && mLauncher.isFinishing())) {
                        return;
                    }
                    if (mTabSpace.getVisibility() != View.VISIBLE) {
                        return;
                    }
                    setCurrentTabImmediately(pagerIndex);
                }
            }, 1000);
        }
    }

    public void dispatchAddEvent(List<String> addList) {
        LetvLog.d(TAG, "dispatchAddEvent =>" + addList);
    }

    public void dispatchRemoveEvent(List<String> rmList) {
        //TODO not used
        LetvLog.d(TAG, "dispatchRemoveEvent =>" + rmList);
        removeTab(null, false);
    }

    public void dispatchUpgradeEvent(List<ScreenInfo> upgradeList) {
        LetvLog.i(TAG, "dispatchUpgradeEvent =>" + upgradeList);
    }

    public void dispatchLockChangeEvent(List<ScreenInfo> changedList) {
        LetvLog.d(TAG, "dispatchLockChangeEvent =>" + changedList);
    }

    public void dispatchTempUnLockEvent(String pluginPackageName) {
        LetvLog.d(TAG, "dispatchTempUnLockEvent " + pluginPackageName);
    }

    public void dispatchRedDotEvent(String pluginPackageName) {
        boolean hited = false;
        if (mAdapter != null) {
            List<FragmentPresenter> fragmentPresenters = mAdapter.getList();
            for (FragmentPresenter fragmentPresenter : fragmentPresenters) {
                if (fragmentPresenter.info != null && fragmentPresenter.info.getPackageName().equals(pluginPackageName)) {
                    fragmentPresenter.info.setIsNew(true);
                    notifyTabIsNewTips(pluginPackageName, true);
                    hited = true;
                    break;
                }
            }
        }
        LetvLog.d(TAG, "dispatchRedDotEvent " + pluginPackageName + ", hit=" + hited);
    }

    private void addTab(ScreenInfo screen, Bundle args, boolean immediately) {
        LetvLog.d(TAG, "addTab " + screen);
        mAdapter.addItem(new FragmentPresenter(screen, mFragmentPresenterCallback));
        if (immediately) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void removeTab(String tabName, boolean immediately) {

    }

    public void setCurrentTab(String tab) {
        if (mLauncher.isStopped()) {
            mTabSpace.setCurrentTab(tab, true);
        } else {
            mTabSpace.setCurrentTab(tab);
        }
    }

    public void setCurrentTab(int tabIndex) {
        mTabSpace.setCurrentTab(tabIndex);
    }

    /**
     * Without animation
     */
    public void setCurrentTabImmediately(String tab) {
        mTabSpace.setCurrentTab(tab, true);
    }

    /**
     * Without animation
     */
    public void setCurrentTabImmediately(int tabIndex) {
        mTabSpace.setCurrentTab(tabIndex, true);
    }

    /**
     * @return adapter Position <b color=red> NOT<b/> tab posotion
     */
    public int getCurrentTab() {
        return mCurrentTab;
    }

    public String getItemIdentifier(int position) {
        if (position < 0 || position > mAdapter.getList().size() - 1) {
            return null;
        }
        return mAdapter.getList().get(position).info.getPackageName();
    }

    public boolean hasBindScreen() {
        return mAdapter.getList().size() > 0 && mViewPagerSpace.getAdapter().getCount() > 0;
    }

    public String getCurrentScreenPackageName() {
        if (mCurrentTab < mAdapter.getList().size() && mCurrentTab > -1) {
            return mAdapter.getList().get(mCurrentTab).info.getPackageName();
        }
        return null;
    }

    public BaseFragment getCurrentFragment() {
        if (mAdapter != null) {
            FragmentPresenter fp = mAdapter.getItemInstance(mCurrentTab);
            if (fp != null) {
                return mAdapter.getItemInstance(mCurrentTab).getFragment();
            }
        }
        return null;
    }

    public FragmentPresenter getCurrentFragmentPresenter() {
        return mAdapter.getItemInstance(mCurrentTab);
    }

    public BaseFragment getFragment(int position) {
        if (mAdapter != null) {
            FragmentPresenter fp = mAdapter.getItemInstance(position);
            if (fp != null) {
                return fp.getFragment();
            }
        }
        return null;
    }

    public FragmentPresenter getFragmentPresenter(String packageName) {
        if (mAdapter != null) {
            for (FragmentPresenter presenter : mAdapter.getList()) {
                if (presenter.info.getPackageName().equals(packageName)) {
                    return presenter;
                }
            }
        }
        return null;
    }

    public FragmentPresenter getFragmentPresenter(int position) {
        if (mAdapter != null) {
            return mAdapter.getItemInstance(position);
        }
        return null;
    }

    public TabSpace getTabSpace() {
        return mTabSpace;
    }

    public Context getContext() {
        return mLauncher.getApplicationContext();
    }

    public int getFirstInPosition() {
        return mFirstInPosition;
    }

    public void firstNotifyDataSetChanged(int defaultItem) {
        mAdapter.notifyDataSetChanged(defaultItem);
    }

    public void addScreenSwitchedListener(OnScreenSwitchedListener l) {
        mOnScreenSwitchedListeners.add(l);
    }

    public void removeScreenSwitchedListener(OnScreenSwitchedListener l) {
        mOnScreenSwitchedListeners.remove(l);
    }

    public boolean isRightDesktop() {
        return mCurrentTab == mAdapter.getList().size() - 1;
    }

    public boolean onHomeKeyHandled() {
        BaseFragment c = getCurrentFragment();
        if (c != null) {
            return c.onHomeKeyEventHandled();
        }
        return false;
    }

    public void rightMove() {
        mViewPagerSpace.setCurrentItem(mViewPagerSpace.getCurrentItem() + 1);
    }

    public void leftMove() {
        if (mViewPagerSpace.getCurrentItem() > 0) {
            mViewPagerSpace.setCurrentItem(mViewPagerSpace.getCurrentItem() - 1);
        }
    }

    private void clear() {
        mAdapter.clear();
    }

    public void destory() {
        List<FragmentPresenter> fragmentPresenters = mAdapter.getList();
        for (FragmentPresenter fh : fragmentPresenters) {
            if (fh.getFragment() != null) {
                fh.getFragment().onCrush();
            }
        }
        mAdapter.clear();
    }

    /**
     * 当插件安装成功，需要将真正的插件fragment替换到ViewPager中时，会调用此方法
     * <p>
     * <p/>
     * 当一个插件Fragment请求添加到UI时，有两种可能性:
     * <p>
     * 1.添加到的index还在ViewPager缓存区中，处理过程如下:
     * -> attachItem(FragmentPresenter N)
     * -> Adapter.notifyDataSetChanged()
     * -> N is be sign to need reload
     * -> Adapter destroy N'fragment
     * -> Adapter create N'fragment
     * <p>
     * 2.添加到的index已经在ViewPager缓存区外了，处理过程如下:
     * -> attachItem(FragmentPresenter N)
     * -> N.destroy() (手动销毁loading fragment)
     * -> N.create()  (当N再次进入缓存区时，会调用create())
     *
     * @param fragmentPresenter
     */
    protected void attachItem(FragmentPresenter fragmentPresenter) {
        if (fragmentPresenter == null) {
            LetvLog.d(TAG, "attachItem is null");
            return;
        }
        LetvLog.d(TAG, "attachItem " + fragmentPresenter.info.getName() + " f=" + fragmentPresenter.getFragment());
        if (mAdapter.isUpdating()) {
            LetvLog.w(TAG, "Warning: adapter is updating");
        }
        BaseFragment needAttach = fragmentPresenter.getFragment();
        if (needAttach != null) {
            needAttach.setInstanceCacheEnabled(false);
            final List<Fragment> poolFragments = mLauncher.getSupportFragmentManager().getFragments();
            if (poolFragments != null && !poolFragments.contains(needAttach)) {
                fragmentPresenter.destroy();
            } else {
                boolean focusNeedReset = needAttach.getContainer() != null && needAttach.getContainer().hasFocus();
                mAdapter.signNeedReloadItem(needAttach);
                mAdapter.notifyDataSetChanged();
                if (focusNeedReset && TextUtils.equals(getCurrentScreenPackageName(), needAttach.pluginPkgName)) {
                    setCurrentTab(getCurrentTab());
                }
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mTabStrip.onScrollChanged(position, positionOffset, positionOffsetPixels);
        if (delegatePageListener != null) {
            delegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
        if (delegatePageListeners != null) {
            for (int i = 0; i < delegatePageListeners.size(); i++) {
                ViewPager.OnPageChangeListener listener = delegatePageListeners.get(i);
                if (listener != null) {
                    listener.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        final boolean pagerActive = mViewPagerSpace.hasFocus();
        final boolean switchLeft = mCurrentTab > position;
        final String fromPlugPkgName = getCurrentScreenPackageName();

        mLastTab = mCurrentTab;
        final BaseFragment lastSelected = getFragment(mLastTab);
        mCurrentTab = position;
        final BaseFragment currentSeleted = getFragment(position);

        LetvLog.d(TAG, "onPageSelected=" + position + "[" + (currentSeleted != null ? currentSeleted.tag : null) + "] lastPos=" + mLastTab + "  pagerActive="
                + pagerActive + "  switchLeft=" + switchLeft + " fromPlugPkgName=" + fromPlugPkgName);

        /**
         * notify lose selected
         */
        if (lastSelected != null) {
            lastSelected.notifyFragmentSelectPre(false);
        }
        /**
         * notify be selected
         */
        if (currentSeleted != null) {
            boolean handleFocusIn = false;
            currentSeleted.notifyFragmentSelectPre(true);
            if (pagerActive) {
                currentSeleted.setContentDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                handleFocusIn = currentSeleted.onFocusRequested(switchLeft ? BaseFragment.FOCUS_RIGHT_IN : BaseFragment.FOCUS_LEFT_IN);
                LetvLog.d(TAG, "onPageSelected [" + currentSeleted.tag + "] onFocusRequested=" + handleFocusIn);
            }
            if (!handleFocusIn && !mTabSpace.hasFocus()) {
                /** Focus correct: set focus on current tab */
                //TODO this is caused because the logic above is not fully stable.....sometimes still lose focus.
                //if still has no focus...let the tab get the focus
                LetvLog.d(TAG, "=============※ Focus correct ※=============");
                mTabStrip.getTabItem(TabStripImpl.parsePager2TabPosition(position)).requestFocus();
            }
        }

        /**
         * 当Tab快速切换时，如果上一次shown的item与当前被select的item相隔大于1 个单位时
         * 表示上一次被show的item完全不可见，此时可以通知shown的item为 not shown.
         *
         * TODO 取决于滑动过程中通知show=false会不会导致卡顿，如果会卡顿， 则只在滑动结束时{onPageScrollStateChanged}通知.
         */
        if (mCurrentShownTab != -1 && Math.abs(mCurrentTab - mCurrentShownTab) > 1) {
            BaseFragment shownItem = getFragment(mCurrentShownTab);
            if (shownItem != null) {
                mCurrentShownTab = -1;
                shownItem.setContentDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                shownItem.notifyFragmentShowChanged(false);
            }
        }
        // Must after call notifyFragmentSelectPre
        notifyScreenSwitched();
        // 从notifyScreenSwitched的onPageSelectListener中抽离设置当前桌面代码
        mHandler.removeMessages(MSG_SET_DESKTOP);
        mHandler.sendEmptyMessageDelayed(MSG_SET_DESKTOP, msgDelaytime);

        /** make sure tab select state. */
        setTabCurrentItem(pager.getCurrentItem());

        if (delegatePageListener != null) {
            delegatePageListener.onPageSelected(position);
        }
        if (delegatePageListeners != null) {
            for (int i = 0; i < delegatePageListeners.size(); i++) {
                ViewPager.OnPageChangeListener listener = delegatePageListeners.get(i);
                if (listener != null) {
                    listener.onPageSelected(position);
                }
            }
        }
        /** for debug */
        mAdapter.printFragmentCachePool();
        mAdapter.printFragmentManagerPool();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == 2) {
            if (DEBUG) {
                LetvLog.d(TAG, " pager scroll ---> start to leave " + mCurrentTab);
            }
            /**
             * Switch-animation start
             */
            notifyScrollStateChanged(0);
            mIsScrolling = true;
        } else if (state == 0) {
            boolean activityOnstop = mLauncher.isStopped();
            if (DEBUG) {
                LetvLog.d(TAG, " pager scroll --->  end to entry " + mCurrentTab + "  lastShowTab="
                        + mCurrentShownTab + " activityOnstop=" + activityOnstop);
            }
            /**
             * Notify fragment shown state.
             *
             * But if current onStop, should ignore this event
             */
            if (!activityOnstop) {
                final FragmentPresenter incomer = getFragmentPresenter(mCurrentTab);
                if (mCurrentShownTab == mCurrentTab) {
                    /**
                     * 当 A select=true、show=true时(静止在A桌面)，快速 A --> B --> A，
                     * A会 select=false -> select=true -> show=true ;
                     * A并不会发生show的变化(因为切换动画前和切换动画后，都是显示的A)，
                     * 但Live插件对该响应有问题，故此情况下，调用 notifyFragmentShowChangedForce(true)
                     * 强制让Live显示
                     */
                    if (incomer != null && incomer.getFragment() != null) {
                        incomer.getFragment().notifyFragmentShowChangedForce(true);
                    }
                } else {
                    final FragmentPresenter leaver = getFragmentPresenter(mCurrentShownTab);
                    if (leaver != null && leaver.getFragment() != null) {
                        leaver.getFragment().notifyFragmentShowChanged(false);
                        leaver.getFragment().setContentDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                    }
                    if (incomer != null && incomer.getFragment() != null) {
                        incomer.getFragment().notifyFragmentShowChanged(true);
                    }
                }
            }
            mCurrentShownTab = mCurrentTab;

            /**
             * Switch-animation end
             */
            notifyScrollStateChanged(1);

            mIsScrolling = false;

            mHandler.removeMessages(MSG_UPDATE_FRAGMENT_VISIBLE);
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_FRAGMENT_VISIBLE, msgDelaytime);
        }


        if (delegatePageListener != null) {
            delegatePageListener.onPageScrollStateChanged(state);
        }

        if (delegatePageListeners != null) {
            for (int i = 0; i < delegatePageListeners.size(); i++) {
                ViewPager.OnPageChangeListener listener = delegatePageListeners.get(i);
                if (listener != null) {
                    listener.onPageScrollStateChanged(state);
                }
            }
        }
    }

    @Override
    public void onTabChanged(String tabTag, boolean immediately) {
        int select = mTabSpace.getSelection();
        LetvLog.d(TAG, "onTabChanged " + tabTag + " pos=" + select + " immediately=" + immediately);
        if (mCurrentTab == -1) {
            LetvLog.d(TAG, "first in workspace");
            mCurrentTab = select;
            mCurrentShownTab = mCurrentTab; // make sure mCurrentShownTab non-null at first time.
            BaseFragment fragment = getFragment(mCurrentTab);
            if (fragment != null) {
                LetvLog.d(TAG, "first to show " + mCurrentShownTab + " [" + fragment.tag + "]");
                fragment.notifyFragmentSelectPreImmediately(true);
                fragment.notifyFragmentShowChanged(true);
                setTabCurrentItem(pager.getCurrentItem());
                notifyScreenSwitched();
            }
            mAdapter.printFragmentCachePool();
            mAdapter.printFragmentManagerPool();
        }
        if (immediately) {
            /**
             * Don't use setCurrentItem(item, smoothScroll), it will lost
             * ViewPager.onPageScrollStateChanged() callback.
             */
            mViewPagerSpace.setCurrentItemImmediately(select);
        } else {
            mViewPagerSpace.setCurrentItem(select);
        }
    }

    @Override
    public boolean onTabAction(int what) {
        switch (what) {
            case TabActionHandler.ACTION_FOCUS_DOWN:
                final BaseFragment current = getCurrentFragment();
                boolean handled = false;
                if (current != null) {
                    current.setContentDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                    handled = current.onFocusRequested(BaseFragment.FOCUS_TOP_IN);
                } else {
                    LetvLog.d(TAG, "onTabFocusDown=" + mCurrentTab + " but current fragment is null  ");
                }
                LetvLog.d(TAG, "======> onTabFocusDown to f=" + current + " handled=" + handled);
                return handled;
            case TabActionHandler.ACTION_BACK_MAIN_SCREEN:
                return true;
        }
        return false;
    }

    public void reSortTabs(ArrayList<ScreenInfo> newScreenInfos) {
        long begin = LetvLog.methodBegin(TAG);
        final List<FragmentPresenter> oldFPs = mAdapter.getList();
        mTabSpace.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        FragmentPresenter beforeShowFP = getCurrentFragmentPresenter();

        /**
         * 编辑后去掉的fragment
         */
        ArrayList<FragmentPresenter> beRemoves = new ArrayList<FragmentPresenter>(5);
        /**
         * 编辑后保留的fragment
         */
        ArrayList<FragmentPresenter> noChanges = new ArrayList<FragmentPresenter>(5);
        /**
         * 编辑后所有的fragment
         */
        ArrayList<FragmentPresenter> afterSortScreens = new ArrayList<FragmentPresenter>(8);

        for (FragmentPresenter oldPager : oldFPs) {
            boolean needRemove = true;
            for (ScreenInfo newInfo : newScreenInfos) {
                if (oldPager.info.getPackageName().equals(newInfo.getPackageName())) {
                    needRemove = false;
                    break;
                }
            }
            if (needRemove) {
                beRemoves.add(oldPager);
            }
        }
        for (ScreenInfo screen : newScreenInfos) {
            boolean hasInstance = false;
            for (FragmentPresenter oldPager : oldFPs) {
                if (oldPager.info.getPackageName().equals(screen.getPackageName())) {
                    noChanges.add(oldPager);
                    afterSortScreens.add(oldPager);
                    hasInstance = true;
                    break;
                }
            }
            if (!hasInstance) {
                afterSortScreens.add(new FragmentPresenter(screen, mFragmentPresenterCallback));
            }
        }

        mAdapter.resetList(afterSortScreens);
        mCurrentTab = mCurrentTab >= afterSortScreens.size() ? afterSortScreens.size() - 1 : mCurrentTab;
        LetvLog.d(TAG, "mNeedUpdateFragments size=" + mAdapter.getNeedReloadItems().size());

        mAdapter.setNeedReloadAll(true);
        mAdapter.notifyDataSetChanged();
        mAdapter.setNeedReloadAll(false);
        notifyScreenSwitched();

        mTabSpace.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        BaseFragment currentShowItem = getFragment(mCurrentTab);
        if (beforeShowFP != null) {
            BaseFragment beforeShowItem = beforeShowFP.getFragment();
            if (!beRemoves.contains(beforeShowFP) && beforeShowItem != null && beforeShowItem != currentShowItem) {
                beforeShowItem.notifyFragmentSelectPre(false);
                beforeShowItem.notifyFragmentShowChanged(false);
            }
        }
        if (currentShowItem != null) {
            mCurrentShownTab = mCurrentTab;
            currentShowItem.notifyFragmentSelectPreImmediately(true);
            currentShowItem.notifyFragmentShowChangedForce(true);
        }

        removeFragmentInPool(beRemoves);
        LetvLog.methodEnd(TAG, begin, "reSortTabs true");
    }

    private void removeFragmentInPool(ArrayList<FragmentPresenter> array) {
        for (FragmentPresenter attach : array) {
            if (attach.getFragment() != null) {
                LetvLog.d(TAG, "manual remove f=" + attach.getFragment());
                attach.getFragment().onCrush();
                mLauncher.getSupportFragmentManager().beginTransaction().remove(attach.getFragment()).commitAllowingStateLoss();
            }

            //FIXME-xubin clear bitmap memory cache
        }
        array.clear();
    }

    public void onActivityStart(boolean isAdvance) {
        BaseFragment cf = getCurrentFragment();
        LetvLog.d(TAG, "onActivityStart --> currentTab=" + mCurrentTab + " currentShow=" + mCurrentShownTab + "  cf=" + cf);
        if (cf != null) {
            cf.onActivityStart();
            if (mCurrentTab == mCurrentShownTab && cf.isViewCreated()) {
                cf.notifyFragmentShowChangedForce(true);
            }
        }

        if (!isAdvance) {
            notifyScreenSwitched();
        }
    }

    public void onActivityStop(boolean isAdvance) {
        BaseFragment cf = getCurrentFragment();
        LetvLog.d(TAG, "onActivityStop --> currentTab=" + mCurrentTab + " currentShow=" + mCurrentShownTab + "  cf=" + cf);
        if (cf != null) {
            cf.onActivityStop();
            BaseFragment lastShownF = getFragment(mCurrentShownTab);
            if (lastShownF != null) {
                lastShownF.notifyFragmentShowChanged(false);
            }
        }

        if (isAdvance) {
            notifyFragmentOffsetWhenActivityStop();
        }
    }

    public void notifyScreenSwitched() {
        FragmentPresenter ft = null;
        try {
            ft = mAdapter.getItemInstance(mCurrentTab);
        } catch (Exception e) {
            LetvLog.d(TAG, "notifyScreenSwitched e=" + e);
        }
        if (ft != null) {
            int index = mCurrentTab;
            int total = mAdapter.getCount();
            String tag = ft.info.getPackageName();
            LetvLog.d(TAG, "notifyScreenSwitched " + tag + " total=" + total);
            for (OnScreenSwitchedListener l : mOnScreenSwitchedListeners) {
                l.onScreenSelected(tag, index, total);
            }

            /** Hide isNew tips on tab. */
            if (ft.info.getIsNew()) {
                mHandler.removeCallbacks(mHideNewTipsTimeTask);
                mHideNewTipsTimeTask.setTarget(index, tag);
                LetvLog.i(TAG, "delay to hide NEW view [" + index + "," + tag + "]");
                mHandler.postDelayed(mHideNewTipsTimeTask, 5000);
            }
        }

        mHandler.removeMessages(MSG_SET_FRAGMENT_OFFSET);
        mHandler.sendEmptyMessageDelayed(MSG_SET_FRAGMENT_OFFSET, msgDelaytime);
    }

    private void notifyFragmentOffsetOnScreen() {
        List<FragmentPresenter> list = mAdapter.getList();
        for (int i = 0; i < list.size(); i++) {
            FragmentPresenter fp = list.get(i);
            fp.setOffsetOnScreen(Math.abs(i - getCurrentTab()));
        }
    }

    //桌面进入后台时，设置一个更大的offset，避免后台时fragment的offset在可刷新数据的范围内 (即避免后台fragment存在刷新行为)
    private void notifyFragmentOffsetWhenActivityStop() {
        List<FragmentPresenter> list = mAdapter.getList();
        for (int i = 0; i < list.size(); i++) {
            FragmentPresenter fp = list.get(i);
            fp.setOffsetOnScreen(10);
        }
    }

    private HideNewTipsTimeTask mHideNewTipsTimeTask = new HideNewTipsTimeTask() {
        @Override
        public void run() {
            if (index == mCurrentTab) {
                LetvLog.i(TAG, "Hide isNew icon [" + tag + "]");
                mTabSpace.notifyIsNewTips(tag, false);
                updateInfoByHideIsNewTips(index);
            }
        }
    };

    private void notifyScrollStateChanged(int state) {
        for (OnScreenSwitchedListener l : mOnScreenSwitchedListeners) {
            l.onScreenScrolling(state);
        }
        ArrayList<Fragment> fs = (ArrayList<Fragment>) mLauncher.getSupportFragmentManager().getFragments();
        if (fs != null) {
            for (Fragment f : fs) {
                if (f != null && f instanceof BaseFragment) {
                    ((BaseFragment) f).setFragmentScrollState(state);
                }
            }
        }
    }

    public void notifyTabIsNewTips(String tag, boolean show) {
        mTabSpace.notifyIsNewTips(tag, show);
    }

    private void updateInfoByHideIsNewTips(int index) {
        if (mAdapter != null) {
            FragmentPresenter fp = mAdapter.getItemInstance(index);
            if (fp != null && fp.info != null) {
                fp.info.setIsNew(false);
                DataModel.getInstance().updateScreenInfoIsNew(fp.info.getPackageName(), false);
            }
        }
    }

    /***************************************************************
     * Bind Strategy
     ************/
    private ITabStrip mTabStrip;
    private ViewPager pager;
    private TabStripObserver mObserver;
    /**
     * outer pagerListener ,developer should listen view pager's event from this listener
     */
    private ViewPager.OnPageChangeListener delegatePageListener;
    private List<OnPageChangeListener> delegatePageListeners;

    @Override
    public int getCount() {
        return mAdapter.getCount();
    }

    /**
     * set the viewpager witch is bind to this TabStrip
     * should be called after the pager's {@link ViewPager#setAdapter(PagerAdapter)}
     */
    private void setViewPager(ViewPager pager) {
        if (pager == null || pager.getAdapter() == null) {
            throw new IllegalStateException(
                    "ViewPager does not have adapter instance.");
        }
        this.pager = pager;
        //noinspection deprecation
        pager.setOnPageChangeListener(this);

        if (mAdapter != null && mObserver != null) {
            mAdapter.unregisterDataSetObserver(mObserver);
        }
        mAdapter = (LauncherAdapter) pager.getAdapter();

        //add data Observer to viewPager's adapter.when adapter calls notifyStrategyChanged();this TabStrip will change as well
        if (mAdapter != null) {
            if (mObserver == null) {
                mObserver = new TabStripObserver();
            }
            mAdapter.registerDataSetObserver(mObserver);
        }
    }

    /**
     * @param position TabPosition
     */
    @Override
    public void setPagerCurrentItem(int position) {
        if (pager.getCurrentItem() == TabStripImpl.parseTab2PagerPosition(position)) {
            return;
        }
        //  need to parse TabItem position to adapter position
        LetvLog.i(TAG, "AdapterPosition : " + TabStripImpl.parseTab2PagerPosition(position) + "-- TabItem Position :" + position);
        pager.setCurrentItem(TabStripImpl.parseTab2PagerPosition(position));
        if (mTabSpace.getVisibility() != View.VISIBLE) {
            mTabSpace.showTab();
        }
    }

    /**
     * @param position AdapterPosition
     */
    @Override
    public void setTabCurrentItem(int position) {
        //  need to parse adapter position to TabItem position
        LetvLog.i(TAG, "AdapterPosition : " + position + "-- TabItem Position :" + TabStripImpl.parsePager2TabPosition(position));
        mTabStrip.scrollToChild(TabStripImpl.parsePager2TabPosition(position));
        if (mTabSpace.getVisibility() != View.VISIBLE) {
            mTabSpace.showTab();
        }
    }

    @Override
    public ScreenInfo getPageTitle(int position) {
        return mAdapter.getList().get(position).info;
    }


    @Deprecated
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        this.delegatePageListener = listener;
    }

    public void addOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        if (delegatePageListeners == null) {
            delegatePageListeners = new ArrayList<OnPageChangeListener>();
        }
        delegatePageListeners.add(listener);
    }

    public void removeOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        if (delegatePageListeners != null) {
            delegatePageListeners.remove(listener);
        }
    }

    public void clearOnPageChangeListener() {
        if (delegatePageListeners != null) {
            delegatePageListeners.clear();
        }
    }

    /**
     * observe the adapter's data change event.
     * when viewpager's adapter called notifyStrategyChanged();
     * this Observer will be notified
     */
    private class TabStripObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            mTabStrip.notifyStrategyChanged();
        }

        @Override
        public void onInvalidated() {
            mTabStrip.notifyStrategyChanged();
        }
    }

    /***************************************************************
     * Bind Strategy
     ************/

    private class MyFragmentPresenterCallback implements FragmentPresenter.FragmentPresenterCallback {

        @Override
        public Context getContext() {
            return LauncherAdapterPresenter.this.getContext();
        }

        @Override
        public BaseFragment getCurrentFragment() {
            return LauncherAdapterPresenter.this.getCurrentFragment();
        }

        @Override
        public void onUpgradeFinished(FragmentPresenter presenter) {
            notifyTabIsNewTips(presenter.info.getPackageName(), true);
        }

        @Override
        public void onRequestItemAttach(final FragmentPresenter presenter) {
            LetvLog.d("onRequestItemAttach -> " + presenter != null ? presenter.info.getPackageName() : "null");
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    attachItem(presenter);
                }
            };
            if (mAdapter.isUpdating()) {
                mHandler.post(r);
            } else {
                r.run();
            }
        }

        @Override
        public void onFirstLoadFailed() {

        }

        @Override
        public boolean isBootIntoSignalFirst() {
            return false;
        }

        @Override
        public boolean isPageScrolling() {
            return mIsScrolling;
        }

        @Override
        public boolean isActivityStop() {
            return mLauncher != null && mLauncher.isStopped();
        }
    }

    static class MyWeakHandler extends Handler {
        private final WeakReference<LauncherAdapterPresenter> weakReference;

        MyWeakHandler(LauncherAdapterPresenter adapterPresenter) {
            weakReference = new WeakReference<LauncherAdapterPresenter>(adapterPresenter);
        }

        @Override
        public void handleMessage(Message msg) {
            LauncherAdapterPresenter adapterPresenter = weakReference.get();
            if (adapterPresenter != null) {
                int position = adapterPresenter.getCurrentTab();
                switch (msg.what) {
                    case MSG_UPDATE_FRAGMENT_VISIBLE:
                        break;
                    case MSG_SET_DESKTOP:
                        break;
                    case MSG_SET_FRAGMENT_OFFSET:
                        List<FragmentPresenter> list = adapterPresenter.mAdapter.getList();
                        for (int i = 0; i < list.size(); i++) {
                            FragmentPresenter fp = list.get(i);
                            fp.setOffsetOnScreen(Math.abs(i - position));
                        }
                        break;
                    default:
                }
            }
        }
    }
}
