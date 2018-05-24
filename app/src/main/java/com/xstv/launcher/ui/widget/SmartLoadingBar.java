package com.xstv.launcher.ui.widget;

import android.graphics.Color;
import android.os.Process;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xstv.launcher.R;
import com.xstv.launcher.ui.activity.Launcher;
import com.xstv.library.base.LetvLog;
import com.xstv.library.base.WeakHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xubin on 16-7-13.
 */
public class SmartLoadingBar {

    private final String TAG = "SmartLoadingBar";

    public interface OnPreHideListener {
        void onPreHide();
    }

    class ViewHolder {
        View cibnRoot;
        View fullRoot;
        TextView cibnTipsTv;
        TextView fullTipsTv;
    }

    private View mLoadingBar;
    private ViewGroup mAttachViewGroup;

    private long mRealVisibleTime;
    private long mMaxAutoHideTime = 3000;
    private long mMinVisibleTime = -1;
    private long mBeginTime = -1;
    private boolean isShown;
    private List<OnPreHideListener> mPreHideListeners = new ArrayList<OnPreHideListener>(1);
    private Timer mTimer;
    private boolean hasRequestHide;
    private boolean isCibnCertificationLoading;
    private WeakHandler<SmartLoadingBar> mHandler = new WeakHandler<SmartLoadingBar>(SmartLoadingBar.this);
    private HideTimerTask mHideTask;

    private OnHideListener mOnHideListerer;
    private boolean mIsTimerWaiting;

    private static class HideTimerTask extends TimerTask {
        WeakReference<SmartLoadingBar> weakReference;
        private boolean hasRunning = false;
        private final long heartBeatTime = 100;

        HideTimerTask(SmartLoadingBar smartLoadingBar) {
            super();
            weakReference = new WeakReference<SmartLoadingBar>(smartLoadingBar);
        }

        @Override
        public void run() {
            SmartLoadingBar smartLoadingBar = weakReference.get();
            if (smartLoadingBar == null) {
                return;
            }
            if (!hasRunning) {
                hasRunning = true;
                Thread.currentThread().setName(smartLoadingBar.TAG);
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
            }

            if (smartLoadingBar.mIsTimerWaiting) {
                LetvLog.i(Launcher.TAG, "SmartLoadingBar hide timer waiting");
                return;
            }

            if (smartLoadingBar.isShown) {
                smartLoadingBar.mRealVisibleTime += heartBeatTime;
                long now = SystemClock.elapsedRealtime();
                LetvLog.i(Launcher.TAG, "SmartLoadingBar  mRealVisibleTime=" + smartLoadingBar.mRealVisibleTime + " min=" + smartLoadingBar.mMinVisibleTime
                        + " max=" + smartLoadingBar.mMaxAutoHideTime + " hasRequestHide=" + smartLoadingBar.hasRequestHide);
                LetvLog.i(Launcher.TAG, "SmartLoadingBar  show Time " + (now - smartLoadingBar.mBeginTime));

                if (smartLoadingBar.mRealVisibleTime < smartLoadingBar.mMinVisibleTime) {//最小显示时间
                    LetvLog.i(Launcher.TAG, "SmartLoadingBar show not reach min time ");
                } else if (smartLoadingBar.hasRequestHide) {//存在主动隐藏请求
                    LetvLog.i(Launcher.TAG, "SmartLoadingBar hide by request ");
                    smartLoadingBar.postHideForcibly();
                } else if (smartLoadingBar.mRealVisibleTime >= smartLoadingBar.mMaxAutoHideTime || (now - smartLoadingBar.mBeginTime) > smartLoadingBar.mMaxAutoHideTime) {//显示超时
                    LetvLog.i(Launcher.TAG, "SmartLoadingBar show reach max time ");
                    smartLoadingBar.postHideForcibly();
                }
            } else {
                if (smartLoadingBar.mTimer != null) {
                    smartLoadingBar.mHideTask.cancel();
                    smartLoadingBar.mTimer.cancel();
                    smartLoadingBar.mHideTask = null;
                    smartLoadingBar.mTimer = null;
                }
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
            }
        }
    }

    public SmartLoadingBar(ViewGroup attachView, OnHideListener listener) {
        mTimer = new Timer(true);
        mAttachViewGroup = attachView;
        mOnHideListerer = listener;
    }

    public void setTimerWaitting(boolean waited) {
        mRealVisibleTime = 0;
        mBeginTime = SystemClock.elapsedRealtime();
        mIsTimerWaiting = waited;
    }

    public void setMaxRealVisibleTime(long t) {
        mMaxAutoHideTime = t;
    }

    public void setMinRealVisibleTime(long t) {
        mMinVisibleTime = t;
    }

    public void calculateRealVisibleTime() {
        mHideTask = new HideTimerTask(this);
        mTimer = new Timer(TAG);
        mBeginTime = SystemClock.elapsedRealtime();
        mTimer.schedule(mHideTask, 0, 100);
//        mHandler.sendEmptyMessage(0);
    }

    public boolean isShown() {
        return isShown;
    }

    public void showDefault() {
        isShown = true;
        resetView();
        ViewHolder viewHolder = (ViewHolder) mLoadingBar.getTag();
        LetvLog.d(Launcher.TAG, "loading view show");
        isCibnCertificationLoading = false;
        viewHolder.cibnRoot.setVisibility(View.GONE);
        viewHolder.fullRoot.setVisibility(View.VISIBLE);
        viewHolder.fullTipsTv.setText("Loading");
        mLoadingBar.setBackgroundResource(R.drawable.ic_launcher_bg);
        /**
         * Re add loadingbar to ensure on the view top.
         */
        mAttachViewGroup.addView(mLoadingBar);
    }

    public void showEditing() {
        isShown = true;
        resetView();
        ViewHolder viewHolder = (ViewHolder) mLoadingBar.getTag();
        viewHolder.cibnRoot.setVisibility(View.GONE);
        viewHolder.fullRoot.setVisibility(View.VISIBLE);
        viewHolder.fullTipsTv.setText(R.string.launcher_editing);
        mLoadingBar.setBackgroundColor(Color.parseColor("#cc000000"));

        /**
         * Re add loadingbar to ensure on the view top.
         */
        mAttachViewGroup.addView(mLoadingBar);
    }

    private void resetView() {
        if (mLoadingBar == null) {
            mLoadingBar = LayoutInflater.from(mAttachViewGroup.getContext()).inflate(R.layout.launcher_loading_bar, null);
            ViewHolder holder = new ViewHolder();
            holder.cibnRoot = mLoadingBar.findViewById(R.id.ly_cibn);
            holder.cibnTipsTv = (TextView) mLoadingBar.findViewById(R.id.cibn_tips);
            holder.fullRoot = mLoadingBar.findViewById(R.id.ly_full);
            holder.fullTipsTv = (TextView) mLoadingBar.findViewById(R.id.full_tips);
            mLoadingBar.setTag(holder);
        } else if (mLoadingBar.getParent() != null) {
            ((ViewGroup) mLoadingBar.getParent()).removeView(mLoadingBar);
        }
    }

    /**
     * 请求隐藏，隐藏前通知调用者：
     * 1、如果达条件立刻隐藏
     * 2、不达条件，通过timer定时检测隐藏条件
     *
     * @param l
     */
    public void hideByPreset(OnPreHideListener l) {
        if (!mPreHideListeners.contains(l)) {
            mPreHideListeners.add(l);
        }
        hideByPreset();
    }

    /**
     * 请求隐藏 ：
     * 1、如果达条件立刻隐藏
     * 2、不达条件，通过timer定时检测隐藏条件
     */
    public void hideByPreset() {
        /**
         * 如果设置了最小显示时间，需要达到此时间后再隐藏
         */
        if (mMinVisibleTime != -1 && mRealVisibleTime < mMinVisibleTime) {
            hasRequestHide = true;
            LetvLog.i(Launcher.TAG, "SmartLoading-hideByPreset(), need wait min time");
        } else {
            LetvLog.i(Launcher.TAG, "SmartLoading-hideByPreset(), has shown min time");
            hideForcibly();
        }
    }

    /**
     * 强制隐藏
     */
    public void hideForcibly() {
        dispatchOnPreHide();
        if (isShown) {
            isShown = false;
            hasRequestHide = false;
            mRealVisibleTime = 0;

            LetvLog.i(Launcher.TAG, "SmartLoadingBar-hideForcibly()");
            if (mLoadingBar != null && mLoadingBar.getParent() != null) {
                ((ViewGroup) mLoadingBar.getParent()).removeView(mLoadingBar);
                if (mOnHideListerer != null) {
                    mOnHideListerer.onHide(isCibnCertificationLoading);
                }
                mLoadingBar = null;
            }
        }
    }

    public void removePreHideListener(OnPreHideListener l) {
        mPreHideListeners.remove(l);
    }

    private void postHideForcibly() {
        mHandler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                hideForcibly();
            }
        });
        if (mTimer != null) {
            mHideTask.cancel();
            mTimer.cancel();
            mHideTask = null;
            mTimer = null;
        }
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
    }

    private void dispatchOnPreHide() {
        for (OnPreHideListener l : mPreHideListeners) {
            l.onPreHide();
        }
    }

    public interface OnHideListener {
        void onHide(boolean isCibnCertificationLoading);
    }
}
