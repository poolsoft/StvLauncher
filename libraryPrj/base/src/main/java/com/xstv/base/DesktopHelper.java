package com.xstv.base;


import android.os.Handler;
import android.os.HandlerThread;

import java.util.ArrayList;
import java.util.List;

public class DesktopHelper {

    private static final String TAG = "SDKDesktopHelper";

    private static final DesktopHelper sInstance = new DesktopHelper();

    private List<DesktopSwitchObserver> mListenerList;
    private Handler mHandler;
    private StartSwitchRunnable mStartRunnable;
    private EndSwitchRunnable mEndRunnable;


    private DesktopHelper() {
        mListenerList = new ArrayList<DesktopSwitchObserver>();
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    /**
     * Get Desktop Helper instance.
     *
     * @return instance
     */
    public static DesktopHelper getInstance() {
        return sInstance;
    }

    /**
     * Add desktop switch observer.
     *
     * @param observer
     */
    public void addDesktopSwitchObserver(DesktopSwitchObserver observer) {
        synchronized (mListenerList) {
            mListenerList.add(observer);
        }
    }

    /**
     * Remove desktop switch observer.
     *
     * @param observer
     */
    public void removeDesktopSwitchObserver(DesktopSwitchObserver observer) {
        synchronized (mListenerList) {
            if (mListenerList.contains(observer)) {
                mListenerList.remove(observer);
            }
        }
    }

    /**
     * Desktop switch start.
     *
     * @param currentTag
     * @param toTag
     */
    public void startSwitch(String currentTag, String toTag) {
        if (mStartRunnable != null) {
            mHandler.removeCallbacks(mStartRunnable);
        } else {
            mStartRunnable = new StartSwitchRunnable(currentTag, toTag);
        }
        mHandler.postDelayed(mStartRunnable, 2000);
    }

    /**
     * Desktop switch end.
     *
     * @param tag
     */
    public void endSwitch(String tag) {
        if (mEndRunnable != null) {
            mHandler.removeCallbacks(mEndRunnable);
        } else {
            mEndRunnable = new EndSwitchRunnable(tag);
        }
        mHandler.postDelayed(mEndRunnable, 2000);

    }

    /**
     * Get current desktop.
     *
     * @return
     */
    public String getCurrentDesktop() {
        return "";
    }

    private void notifyStartSwitch(String startTag, String toTag) {
        synchronized (mListenerList) {
            for (DesktopSwitchObserver observer : mListenerList) {
                observer.onSwitchStart(startTag, toTag);
            }
        }
    }

    /**
     * Desktop switch observer
     */
    public interface DesktopSwitchObserver {
        void onSwitchStart(String currentTag, String toTag);

        void onSwitchEnd(String tag);
    }

    private class StartSwitchRunnable implements Runnable {

        String startTag;
        String toTag;

        StartSwitchRunnable(String startTag, String toTag) {
            this.startTag = startTag;
            this.toTag = toTag;
        }

        @Override
        public void run() {
            notifyStartSwitch(startTag, toTag);
        }
    }

    private class EndSwitchRunnable implements Runnable {

        private String toTag;

        public EndSwitchRunnable(String toTag) {
            this.toTag = toTag;
        }

        @Override
        public void run() {
            Logger.getLogger(TAG, "EndSwitchRunnable").d(toTag);
            synchronized (mListenerList) {
                for (DesktopSwitchObserver observer : mListenerList) {
                    observer.onSwitchEnd(toTag);
                }
            }
        }
    }

}
