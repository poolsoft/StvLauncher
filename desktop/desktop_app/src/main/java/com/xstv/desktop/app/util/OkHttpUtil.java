
package com.xstv.desktop.app.util;


import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class OkHttpUtil {

    private static final OkHttpClient sOkHttpClient = new OkHttpClient();
    private static final String CHARSET_NAME = "UTF-8";
    static {
        //sOkHttpClient.setConnectTimeout(10, TimeUnit.SECONDS);
    }

    /**
     * 开启异步线程访问网络
     *
     * @param request
     * @param responseCallback
     */
    public static void enqueue(Request request, Callback responseCallback) {
        sOkHttpClient.newCall(request).enqueue(responseCallback);
    }

    /*public static String formatParams(List<BasicNameValuePair> params) {
        return URLEncodedUtils.format(params, CHARSET_NAME);
    }

    public static String attachHttpGetParams(String url, List<BasicNameValuePair> params) {
        return url + "?" + formatParams(params);
    }*/

    public static String attachHttpGetParam(String url, String name, String value) {
        return url + "?" + name + "=" + value;
    }

    public static void cancel(Request request) {
        //sOkHttpClient.cancel(request);
    }
}
