
package com.xstv.launcher.logic.manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.widget.ImageView;

import com.xstv.launcher.R;
import com.xstv.library.base.LetvLog;

public class NetworkManager {

    public static final String PROPERTY_CONNECTED = "1";
    public static final String PROPERTY_UNCONNECTED = "0";

    public static final String TAG = "NetworkCheckUtil";

    public static final String ACTION_NETWORK_CHANGE = "com.stv.net.misc.internet";

    public static final String INTERNET_INT_PROPERTY = "net.stv.internet";

    public static boolean isNetworkOkInSystemProperties() {
        String str = SystemProperties.get(INTERNET_INT_PROPERTY);
        LetvLog.d(TAG, "INTERNET_INT_PROPERTY = " + str);
        return !PROPERTY_UNCONNECTED.equals(str);
    }

    /**
     * 获取信号WIFI信号强度
     *
     * @param context
     * @return
     */
    public static int getWifiStrength(Context context) {
        int strength = 0;
        // Wifi的连接速度及信号强度：
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info.getBSSID() != null) {
            // 链接信号强度
            strength = WifiManager.calculateSignalLevel(info.getRssi(), 4);
        }
        return strength;
    }

    /**
     * 检测网络是否可用
     *
     * @return 网络是否可用
     */
    public static boolean isNetworkConnected(Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static synchronized String setNetworkIcon(Context context, ImageView icon) {
        LetvLog.d(TAG, "setNetworkIcon ... ");
        String networkName = null;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        LetvLog.d(TAG, "activeNetwork ... " + activeNetwork);
        if (activeNetwork == null) {
            icon.setImageLevel(10);
            networkName = context.getResources().getString(R.string.network_disconnected);
        } else {
            boolean isConnected = activeNetwork.isConnectedOrConnecting();
            int type = activeNetwork.getType();
            LetvLog.d(TAG, "isConnected = " + isConnected + ", type = " + type);
            if (isConnected) {
                if (type == ConnectivityManager.TYPE_ETHERNET) {
                    if (isConnected) {
                        icon.setImageLevel(1);
                        networkName = context.getResources().getString(R.string.network_connected);
                    } else {
                        icon.setImageLevel(0);
                        networkName = context.getResources().getString(R.string.network_disconnected);
                    }
                } else if (type == ConnectivityManager.TYPE_WIFI) {
                    if (isConnected) {
                        icon.setImageLevel(NetworkManager.getWifiStrength(context) + 2);
                        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        networkName = wifiInfo.getSSID();
                        LetvLog.d(TAG, "networkName = " + networkName);
                        if (null != networkName && networkName.startsWith("\"") && networkName.length() > 2) {
                            networkName = networkName.substring(1, networkName.length() - 1);
                            LetvLog.d(TAG, "networkName sub = " + networkName);
                        }
                    } else {
                        icon.setImageLevel(NetworkManager.getWifiStrength(context) + 2 + 4);
                        networkName = context.getResources().getString(R.string.network_disconnected);
                    }
                }
            } else {
                icon.setImageLevel(10);
                networkName = context.getResources().getString(R.string.network_disconnected);
            }
        }
        return networkName;
    }
}