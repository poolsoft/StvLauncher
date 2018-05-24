
package com.xstv.launcher.ui.widget;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;

import com.xstv.library.base.BaseFragment;
import com.xstv.library.base.LetvLog;

import java.util.ArrayList;
import java.util.List;

public class LauncherAdapter extends FragmentStatePagerAdapter {

    public static final String TAG = LauncherAdapter.class.getSimpleName();

    private ViewPagerSpace mViewPagerSpace;
    private FragmentActivity mActivity;
    private final List<FragmentPresenter> mFragmentPresenters = new ArrayList<FragmentPresenter>(10);
    private final List<Fragment> mNeedReloadFragments = new ArrayList<Fragment>(3);

    private boolean mNeedLoadAll = false;
    private boolean mUpdating;
    private static final boolean DEBUG = false;

    public LauncherAdapter(FragmentActivity activity, ViewPagerSpace metroSpace) {
        super(activity.getSupportFragmentManager());
        if (DEBUG) {
            FragmentManager.enableDebugLogging(true);
        }
        mActivity = activity;
        mViewPagerSpace = metroSpace;
    }

    @Override
    public BaseFragment getItem(int position) {
        LetvLog.d(TAG, "### getItem " + position + " ###");
        if (position < mFragmentPresenters.size() && position >= 0) {
            return mFragmentPresenters.get(position).getFragmentNonNull();
        }
        LetvLog.d(TAG, "### getItem " + position + " >>> null ###");
        return null;
    }

    public FragmentPresenter getItemInstance(int position) {
        if (position < 0 || position > mFragmentPresenters.size() - 1) {
            return null;
        }
        return mFragmentPresenters.get(position);
    }

    @Override
    public boolean needReload(Object obj) {
        BaseFragment needLoadF = (BaseFragment) obj;
        boolean need = false;
        if (mNeedLoadAll) {
            need = true;
        } else {
            for (Fragment hasSignF : mNeedReloadFragments) {
                if (hasSignF != null && hasSignF == needLoadF) {
                    need = true;
                    break;
                }
            }
        }
        if (DEBUG) {
            LetvLog.d(TAG, "needReload [" + (needLoadF != null ? needLoadF.tag : "null") + "], need=" + need);
        }
        if (need) {
            mNeedReloadFragments.remove(needLoadF);
        }
        return need;
    }

    public void signNeedReloadItem(BaseFragment fragment) {
        mNeedReloadFragments.add(fragment);
    }

    public List<FragmentPresenter> getList() {
        return mFragmentPresenters;
    }

    public void printFragmentManagerPool() {
        if (DEBUG) {
            LetvLog.d(TAG, "============ Fragment Pool ============");
            ArrayList<Fragment> fs = (ArrayList<Fragment>) mActivity.getSupportFragmentManager().getFragments();
            if (fs != null) {
                for (Fragment f : fs) {
                    if (f != null && f instanceof BaseFragment) {
                        LetvLog.d(TAG, "###### Pool -> " + ((BaseFragment) f).tag);
                    }
                }
            }
            LetvLog.d(TAG, "=======================================");
        }
    }

    public void printFragmentCachePool() {
        if (DEBUG) {
            LetvLog.d(TAG, "============ Cache Pool ============");
            for (FragmentPresenter fa : mFragmentPresenters) {
                if (fa.getFragment() != null) {
                    LetvLog.d(TAG, "###### Pool -> " + fa.getFragment().tag);
                }
            }
            LetvLog.d(TAG, "=======================================");
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (DEBUG) {
            LetvLog.d(TAG, "notifyDataSetChanged mNeedLoadAll=" + mNeedLoadAll + " ---> begin");
        }
        mUpdating = true;
        super.notifyDataSetChanged();
        mUpdating = false;
        if (DEBUG) {
            LetvLog.d(TAG, "notifyDataSetChanged mNeedLoadAll=" + mNeedLoadAll + " ---> end");
        }
    }

    /**
     * ViewPager填充完数据后第一次notifyDataSetChanged,默认会进入0； 为了不必要的加载，需要自己控制默认进入的index;
     *
     * @param defaultItem
     */
    public void notifyDataSetChanged(int defaultItem) {
        mViewPagerSpace.setDefaultItem(defaultItem);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFragmentPresenters.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        if (position < mFragmentPresenters.size()) {
            mFragmentPresenters.get(position).destroy();
        }
    }

    public void addItem(FragmentPresenter addPresenter) {
        LetvLog.d(TAG, " addItem ");
        mFragmentPresenters.add(addPresenter);
    }

    protected void removeItem(FragmentPresenter rmPresenter) {
        mFragmentPresenters.remove(rmPresenter);
    }

    public void clear() {
        mFragmentPresenters.clear();
        mNeedReloadFragments.clear();
    }

    public void resetList(ArrayList<FragmentPresenter> list) {
        mFragmentPresenters.clear();
        mFragmentPresenters.addAll(list);
    }

    public List<Fragment> getNeedReloadItems() {
        return mNeedReloadFragments;
    }

    public void setNeedReloadAll(boolean need) {
        mNeedLoadAll = need;
        if (mNeedLoadAll) {
            mNeedReloadFragments.clear();
        }
    }

    /**
     * @param position
     * @return name to be shown on the tab bar ...
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentPresenters.get(position).info.getName();
    }

    /**
     * @return whether adapter notifyDataChanging
     */
    public boolean isUpdating() {
        return mUpdating;
    }
}
