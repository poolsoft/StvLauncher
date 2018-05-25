
package com.xstv.launcher.ui.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xstv.launcher.R;
import com.xstv.launcher.logic.manager.NetworkManager;
import com.xstv.launcher.logic.manager.StatusReceiverManager;
import com.xstv.launcher.logic.manager.USBManager;
import com.xstv.launcher.logic.manager.WeatherManager;
import com.xstv.launcher.logic.model.WeatherInfo;
import com.xstv.library.base.LetvLog;

public class StatusBarView extends RelativeLayout {
    private static final String TAG = "StatusBarView";

    private ImageView mUsbView;
    private ImageView mUsbDividerView;
    private ImageView mNetworkView;
    private ImageView mWeatherIconView;
    private TextView mWeatherDegreeView;

    private StatusReceiverManager mStatusReceiverManager;
    private WeatherInfo mWeatherInfo;

    public StatusBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        setViewData(context);
    }

    private void initView(Context context) {
        inflate(context, R.layout.status_bar, this);
        mUsbView = (ImageView) findViewById(R.id.status_bar_usb);
        mUsbDividerView = (ImageView) findViewById(R.id.status_bar_usb_divider);
        mNetworkView = (ImageView) findViewById(R.id.status_bar_network);
        mWeatherIconView = (ImageView) findViewById(R.id.status_bar_weather_icon);
        mWeatherDegreeView = (TextView) findViewById(R.id.status_bar_weather_degree);
    }

    private void setViewData(Context context) {
        mStatusReceiverManager = new StatusReceiverManager(context);
        NetworkManager.setNetworkIcon(context, mNetworkView);
        setUsbIcon();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mStatusReceiverManager.addNetWorkReceiver(mNetworkReceiver);
        mStatusReceiverManager.addUsbReceiver(mUsbReceiver);
        mStatusReceiverManager.addWeatherReceiver(mWeatherReceiver);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mStatusReceiverManager.unRegisterReceiver(mNetworkReceiver);
        mStatusReceiverManager.unRegisterReceiver(mUsbReceiver);
        mStatusReceiverManager.unRegisterReceiver(mWeatherReceiver);
    }

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setUsbIcon();
        }
    };

    private void setUsbIcon() {
        if (USBManager.getUsbStorageNum(getContext()) > 0) {
            mUsbDividerView.setVisibility(View.VISIBLE);
            mUsbView.setVisibility(View.VISIBLE);
        } else {
            mUsbDividerView.setVisibility(View.GONE);
            mUsbView.setVisibility(View.GONE);
        }
    }

    private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            LetvLog.d(TAG, "mNetworkReceiver");
            NetworkManager.setNetworkIcon(context, mNetworkView);
        }
    };

    private BroadcastReceiver mWeatherReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            updateWeather();
        }
    };

    private void updateWeather() {
        // 天气弹出正在展示，不用更新天气信息
        mWeatherInfo = WeatherManager.getWeatherInfo(getContext());
        if (mWeatherInfo == null) {
            mWeatherDegreeView.setVisibility(View.GONE);
            return;
        }
        try {
            if (mWeatherInfo.getUnit() == 1) {
                mWeatherDegreeView.setText(mWeatherInfo.getTemperature() + getResources().getString(R.string.weather_degree_unit_f));
            } else {
                mWeatherDegreeView.setText(mWeatherInfo.getTemperature() + getResources().getString(R.string.weather_degree_unit_c));
            }
            int id = mWeatherInfo.getImage_icon();
            Context weatherContext = WeatherManager.getWeatherContext(getContext());
            Resources weatherResources = weatherContext.getResources();
            mWeatherIconView.setImageDrawable(weatherResources.getDrawable(id));
            mWeatherDegreeView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            LetvLog.d(TAG, "updateWeather e = " + e);
        }
    }
}