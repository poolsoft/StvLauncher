/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xstv.launcher.ui.widget;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.Fragment.SavedState;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.xstv.base.BaseFragment;
import com.xstv.base.LetvLog;

import java.util.ArrayList;

//import com.stv.launcher.logic.manager.LePluginShell;

/**
 * Implementation of {@link android.support.v4.view.PagerAdapter} that uses a {@link Fragment} to manage each page. This class also handles saving and restoring of fragment's state.
 * <p>
 * This version of the pager is more useful when there are a large number of pages, working more like a list view. When pages are not visible to the user, their entire fragment may be destroyed, only
 * keeping the saved state of that fragment. This allows the pager to hold on to much less memory associated with each visited page as compared to {@link FragmentPagerAdapter} at the cost of
 * potentially more overhead when switching between pages.
 * <p>
 * When using FragmentPagerAdapter the host ViewPager must have a valid ID set.
 * </p>
 * <p>
 * Subclasses only need to implement {@link #getItem(int)} and {@link #getCount()} to have a working adapter.
 * <p>
 * Here is an example implementation of a pager containing fragments of lists: {@sample development/samples/Support13Demos/src/com/example/android/supportv13/app/FragmentStatePagerSupport.java
 * complete}
 * <p>
 * The <code>R.layout.fragment_pager</code> resource of the top-level fragment is: {@sample development/samples/Support13Demos/res/layout/fragment_pager.xml complete}
 * <p>
 * The <code>R.layout.fragment_pager_list</code> resource containing each individual fragment's layout is: {@sample development/samples/Support13Demos/res/layout/fragment_pager_list.xml complete}
 */
public abstract class FragmentStatePagerAdapter extends PagerAdapter {
    private static final String TAG = "FragmentStatePagerAdapter";
    private static final boolean DEBUG = true;

    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction = null;

    private ArrayList<SavedState> mSavedState = new ArrayList<SavedState>();
    private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
    private Fragment mCurrentPrimaryItem = null;

    public FragmentStatePagerAdapter(FragmentManager fm) {
        mFragmentManager = fm;
    }

    /**
     * Return the Fragment associated with a specified position.
     */
    public abstract Fragment getItem(int position);

    @Override
    public void startUpdate(ViewGroup container) {
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // If we already have this item instantiated, there is nothing
        // to do. This can happen when we are restoring the entire pager
        // from its saved state, where the fragment manager has already
        // taken care of restoring the fragments we previously had instantiated.
        if (mFragments.size() > position) {
            Fragment f = mFragments.get(position);
            if (f != null) {
                if (DEBUG)
                    LetvLog.v(TAG, "instantiateItem item by mFragments #" + position + ": f=" + f);
                return f;
            }
        }

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }

        Fragment fragment = getItem(position);
        if (DEBUG)
            LetvLog.v(TAG, "Adding item #" + position + ": f=" + fragment + " container=" + container);
        if (mSavedState.size() > position) {
            Fragment.SavedState fss = mSavedState.get(position);
            if (fss != null) {
                fragment.setInitialSavedState(fss);
            }
        }
        while (mFragments.size() <= position) {
            mFragments.add(null);
        }
        fragment.setMenuVisibility(false);
        fragment.setUserVisibleHint(false);
        mFragments.set(position, fragment);
        if (DEBUG)
            LetvLog.v(TAG, "instantiateItem mCurTransaction=" + mCurTransaction + " f=" + fragment + " container=" + container);
        if (mCurTransaction != null) {
            mCurTransaction.add(container.getId(), fragment);
        }

        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        if (DEBUG)
            LetvLog.v(TAG, "Removing item #" + position + ": f=" + object
                    + " v=" + ((Fragment) object).getView());
        while (mSavedState.size() <= position) {
            mSavedState.add(null);
        }

        // 这里destroy时，除了remove操作，还会保存了state；假如destroy掉的item包含有子fragment，saveState操作会保存子fragment信息，
        // 并且在item恢复时，会把子fragment恢复。有时候我们并不希望这样做。
        if (fragment instanceof BaseFragment && ((BaseFragment) fragment).isSaveStateEnable()) {
            SavedState ss = null;
            try {
                ss = mFragmentManager.saveFragmentInstanceState(fragment);
            } catch (Exception e) {
                LetvLog.w(TAG, "Fragment->" + fragment + " is not currently in the FragmentManager");
            }
            if (ss != null) {
                mSavedState.set(position, ss);
            }
        }

        mFragments.set(position, null);
        mCurTransaction.remove(fragment);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;
        if (fragment == null) {
            return;
        }
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }

            /**
             * Fix bug {@link TPRJECT-19945} which occurs only once.
             */
            try {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            } catch (Exception e) {
                LetvLog.w(TAG, "setPrimaryItem exception " + e);
            }

            mCurrentPrimaryItem = fragment;
        }
        //when launcher onResume, current fragment may not get the right visiblity state.
        try {
            fragment.setMenuVisibility(true);
            fragment.setUserVisibleHint(true);
        } catch (Exception e) {
            LetvLog.w(TAG, "setPrimaryItem exception " + e);
        }
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getView() == view;
    }

    @Override
    public Parcelable saveState() {
        Bundle state = null;
        if (mSavedState.size() > 0) {
            state = new Bundle();
            Fragment.SavedState[] fss = new Fragment.SavedState[mSavedState.size()];
            mSavedState.toArray(fss);
            state.putParcelableArray("states", fss);
        }
        for (int i = 0; i < mFragments.size(); i++) {
            Fragment f = mFragments.get(i);
            if (f != null) {
                if (state == null) {
                    state = new Bundle();
                }
                String key = "f" + i;
                mFragmentManager.putFragment(state, key, f);
            }
        }
        return state;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state != null) {
            Bundle bundle = (Bundle) state;
            bundle.setClassLoader(loader);
            Parcelable[] fss = bundle.getParcelableArray("states");
            mSavedState.clear();
            mFragments.clear();
            if (fss != null) {
                for (int i = 0; i < fss.length; i++) {
                    mSavedState.add((Fragment.SavedState) fss[i]);
                }
            }
            Iterable<String> keys = bundle.keySet();
            for (String key : keys) {
                if (key.startsWith("f")) {
                    int index = Integer.parseInt(key.substring(1));
                    Fragment f = mFragmentManager.getFragment(bundle, key);
                    if (f != null) {
                        while (mFragments.size() <= index) {
                            mFragments.add(null);
                        }
                        f.setMenuVisibility(false);
                        mFragments.set(index, f);
                    } else {
                        LetvLog.w(TAG, "Bad fragment at key " + key);
                    }
                }
            }
        }
    }

    @Override
    public final int getItemPosition(Object object) {
        return needReload(object) ? PagerAdapter.POSITION_NONE : PagerAdapter.POSITION_UNCHANGED;
    }

    // 是否刷新fragment
    public boolean needReload(Object object) {
        return false;
    }
}
