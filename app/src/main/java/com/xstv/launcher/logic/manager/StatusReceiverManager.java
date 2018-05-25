package com.xstv.launcher.logic.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.xstv.base.LetvLog;

public class StatusReceiverManager {
    private static final String TAG = "StatusReceiverManager";
    private Context mContext;

    private static final String ACTION_UPDATE_WEATHER = "com.letv.action.update_weather";

    public StatusReceiverManager(Context context) {
        this.mContext = context;
    }

    public void addNetWorkReceiver(BroadcastReceiver receiver) {
        IntentFilter networkFilter = new IntentFilter();
        networkFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(receiver, networkFilter);
        LetvLog.d(TAG, "addNetWorkReceiver done ");
    }

    public void addUsbReceiver(BroadcastReceiver receiver) {
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
        usbFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        usbFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        usbFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        usbFilter.addDataScheme("file");
        mContext.registerReceiver(receiver, usbFilter);
        LetvLog.d(TAG, "addUsbReceiver done ");
    }

    public void addWeatherReceiver(BroadcastReceiver receiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_WEATHER);
        mContext.registerReceiver(receiver, intentFilter);
        LetvLog.d(TAG, "addWeatherReceiver done ");
    }

    public void unRegisterReceiver(BroadcastReceiver receiver) {
        mContext.unregisterReceiver(receiver);
    }
}