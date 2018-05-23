
package com.stv.plugin.demo.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;

import com.stv.plugin.demo.DemoApplication;
import com.xstv.base.Logger;

import java.util.LinkedList;

public class IdleTaskLooper {

    public interface Impl {
        void _do();
    }

    private Logger mLogger = Logger.getLogger(DemoApplication.PLUGINTAG, "IdleTaskLooper");
    private boolean mUIScrolling = false;
    private ImplHandler mHandler = new ImplHandler();
    private final LinkedList<Impl> mQueue = new LinkedList<Impl>();
    private MessageQueue mMessageQueue = Looper.myQueue();

    class ImplHandler extends Handler implements MessageQueue.IdleHandler {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                synchronized (mQueue) {
                    if (mQueue.size() == 0) {
                        return;
                    }
                    Impl impler = mQueue.removeFirst();
                    try {
                        impler._do();
                    } catch (Exception e) {
                        mLogger.e("do exception=" + e);
                    }
                }
                synchronized (mQueue) {
                    if (mQueue.size() > 0) {
                        scheduleNext();
                    }
                }
            }
        }

        /**
         * When handle looper idle will callback this.
         */
        @Override
        public boolean queueIdle() {
            mHandler.removeCallbacks(delayCheckScrollStateRunnable);
            if (!mUIScrolling) {
                mHandler.sendEmptyMessage(0);
            } else {
                mLogger.d("delayCheckScrollStateRunnable.run");
                mHandler.postDelayed(delayCheckScrollStateRunnable, 100);
            }
            return false;
        }

        Runnable delayCheckScrollStateRunnable = new Runnable() {
            @Override
            public void run() {
                queueIdle();
            }
        };
    }

    public void onUIScrollStateChanged(boolean isScrolling) {
        mUIScrolling = isScrolling;
    }

    public void addToLast(Impl impler) {
        synchronized (mQueue) {
            mQueue.addLast(impler);
            if (mQueue.size() == 1) {
                scheduleNext();
            }
        }
    }

    public void addToFirst(Impl impler) {
        synchronized (mQueue) {
            mQueue.addFirst(impler);
            if (mQueue.size() == 1) {
                scheduleNext();
            }
        }
    }

    public void cancelAll() {
        mMessageQueue.removeIdleHandler(mHandler);
        synchronized (mQueue) {
            mQueue.clear();
        }
    }

    private void scheduleNext() {
        if (mQueue.size() > 0) {
            mMessageQueue.addIdleHandler(mHandler);

            /**
             * Only avoid not callback idle when current looper is idling.
             *
             * Looper.isIdling() is better but it is hide method.
             */
            mHandler.sendEmptyMessage(2016090110);
        }
    }
}
