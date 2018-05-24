package com.xstv.library.base.menu;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Created by ll on 15-12-15.
 */
public class AutoCloseDialog extends Dialog {
    private static final String TAG = "AutoCloseDialog";
    protected Context mContext;
    private boolean mCancelCloseSystemDialog = true;
    protected boolean mCancelForCloseSystemDialog = false;

    private boolean mCancelTimeout = true;
    protected boolean mCancelForTimeout = false;

    public static final int DEF_TIMEOUT = 30 * 1000;
    public static final int MSG_TIMEOUT = 100;

    private int mTimeout = DEF_TIMEOUT;

    public AutoCloseDialog(Context context) {
        this(context, 0);
    }

    public AutoCloseDialog(Context context, int theme) {
        super(context, theme);
        this.mContext = context;
    }

    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_TIMEOUT:
                    processTimeout();
                    break;
            }
        }
    };

    private void processTimeout() {
        Log.i(TAG, "no operation timeout");
        mCancelForTimeout = true;
        cancel();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, " ACTION_CLOSE_SYSTEM_DIALOGS ");
            mCancelForCloseSystemDialog = true;
            cancel();
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mCancelTimeout) {
            mHandler.removeMessages(MSG_TIMEOUT);
            mHandler.sendEmptyMessageDelayed(MSG_TIMEOUT, mTimeout);
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mCancelTimeout) {
            mHandler.removeMessages(MSG_TIMEOUT);
            mHandler.sendEmptyMessageDelayed(MSG_TIMEOUT, mTimeout);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void show() {
        super.show();
        if (mCancelCloseSystemDialog)
            mCancelForCloseSystemDialog = false;
        if (mCancelTimeout)
            mCancelForTimeout = false;
        if (mCancelCloseSystemDialog) {
            mContext.registerReceiver(mBroadcastReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        }
        if (mCancelTimeout) {
            mHandler.removeMessages(MSG_TIMEOUT);
            mHandler.sendEmptyMessageDelayed(MSG_TIMEOUT, mTimeout);
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        try {
            if (mCancelCloseSystemDialog) {
                mContext.unregisterReceiver(mBroadcastReceiver);
            }
            if (mCancelTimeout && mHandler != null) {
                mHandler.removeMessages(MSG_TIMEOUT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCancelCloseSystemDialog(boolean cancelCloseSystemDialog) {
        this.mCancelCloseSystemDialog = cancelCloseSystemDialog;
    }

    public void setCancelTimeout(boolean cancelTimeout) {
        this.mCancelTimeout = cancelTimeout;
    }

    public void setTimeout(int timeout) {
        this.mTimeout = timeout;
    }
}
