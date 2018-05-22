
package com.xstv.desktop.app.util;

import android.content.Context;
import android.text.TextUtils;

import com.xstv.base.LetvLog;
import com.xstv.desktop.app.AppPluginActivator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlGenerator {
    private static final String TAG = UrlGenerator.class.getSimpleName();
    //观星后台
    //正式服务器URL
    public static final String POSID_URL = "http://stargazer-scloud.cp21.ott.cibntv.net";
    public static final String POSID_HOST_TAG = "stargazer_proxy";
    public static final int POSID_SUCCESS_CODE = 10000;
    public static final String POSID_PARAM = "/proxy/api/v1/promotion";

    private boolean is;


    private static String getHost(Context context, String hostTag) {
        String host = "";//LetvManagerUtil.getDomain(context, hostTag);
        if (!TextUtils.isEmpty(host) && !host.startsWith("http")) {
            host = "http://" + host;
        }
        return host;
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    public static String getPosidStr(){
        StringBuilder builder = getUrlPrefix();
        builder.append("apps_main_2018,");
        builder.append("apps_show_2018");
        return builder.toString();
    }

    public static StringBuilder getUrlPrefix(){
        StringBuilder builder = new StringBuilder();
        String host = getHost(AppPluginActivator.getContext(), POSID_HOST_TAG);
        LetvLog.d(TAG, "getPosidStr host = " + host);
        if(TextUtils.isEmpty(host)){
            host = POSID_URL;
        }
        //host = "http://stargazer-sandbox.scloud.cp21.ott.cibntv.net";
        builder.append(host);
        builder.append(POSID_PARAM);
        //builder.append("?mac=" + LetvManagerUtil.getLetvMac());
        builder.append("&posid=");
        return builder;
    }
}
