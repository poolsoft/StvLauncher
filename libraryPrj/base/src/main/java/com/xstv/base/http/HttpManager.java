package com.xstv.base.http;

import com.xstv.base.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Managing the enqueue and cancel of HTTP requests {@link HttpRequest}
 */

public class HttpManager {

    /*
     * Log tag for http component of sdk.
     */
    static final String SDK_HTTP_LOG_TAG = "SDK_HTTP";

    /*
     * Log tag for this class.
     */
    private static final String TAG = "HttpManager";

    /**
     * Default charset of http component, UTF-8.
     */
    public static final String CHARSET_NAME = "UTF-8";

    /**
     * Default name and value separator '='.
     */
    public static final String NAME_VALUE_SEPARATOR = "=";

    /**
     * Default query parameter separator '&'.
     */
    public static final char QUERY_PARAM_SEPARATOR = '&';

    /**
     * Get method constant
     */
    public static final String METHOD_GET = "GET";

    /**
     * Post method constant
     */
    public static final String METHOD_POST = "POST";

    static final boolean USE_SYSTEM_OUT_PRINT = false;

    private static String sCurrentDesktop;

    private static OkHttpClient sOkHttpClient;

    private static List<RequestTriple> sRequestList = new ArrayList<RequestTriple>();

    private static Map<String, Integer> sRequestCounter = new HashMap<String, Integer>();

    private static Executor sCountThread = Executors.newSingleThreadExecutor();

    private static CertificateUtil sCertificateUtil = new CertificateUtil();

    private static OkHttpClient sClientWithCertificate;


    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(15, TimeUnit.SECONDS);
        builder.retryOnConnectionFailure(true);
        builder.readTimeout(15, TimeUnit.SECONDS);
        builder.writeTimeout(15, TimeUnit.SECONDS);
        sOkHttpClient = builder.build();
    }

    /**
     * 在当前线程发起 HTTP 请求并阻塞当前线程，直到请求完成或者发生错误
     * @param request
     * @return 请求成功，返回 HTTP Response
     * @throws IOException 请求失败
     */
    public static HttpResponse execute(HttpRequest request) throws IOException {
        return execute(request, false);
    }

    /**
     * 在当前线程发起 HTTP 请求并阻塞当前线程，直到请求完成或者发生错误
     * @param request
     * @param selfSignedCert 是否使用自定义的证书
     * @return 请求成功，返回 HTTP Response
     * @throws IOException 请求失败
     */
    public static HttpResponse execute(HttpRequest request, boolean selfSignedCert) throws IOException {
        checkAndCount(request);

        Request r = request.toOkRequest();
        Call call = makeCall(r, selfSignedCert);
        Response response = call.execute();

        return new HttpResponse(response);
    }

    /**
     * Add a {@link HttpRequest} into request queue.
     *
     * @param request
     * @param callback
     */
    public static void enqueue(HttpRequest request, HttpCallback callback) {
        enqueue(request, callback, false);
    }

    /**
     * Add a {@link HttpRequest} into request queue.
     * Notice: If selfSignedCert is true, The certificate must be added before calling this method.
     *
     * @param request
     * @param callback
     * @param selfSignedCert If use a self-signed certificate
     */
    public static void enqueue(HttpRequest request, HttpCallback callback, boolean selfSignedCert) {
        checkAndCount(request);

        Request r = request.toOkRequest();
        Call call = makeCall(r, selfSignedCert);

        RequestTriple rt = new RequestTriple(request, r, call);
        //This may be block main thread
        synchronized (sRequestList) {
            sRequestList.add(rt);
        }
        call.enqueue(callback.getCallback());
    }

    /**
     * Encoding the parameters using utf-8 charset and append them to the url
     *
     * @param url
     * @param params
     * @return Url with query parameters
     */
    public static String attachHttpGetParams(String url, List<HttpNameValuePair> params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                sb.append(QUERY_PARAM_SEPARATOR);
            }
            HttpNameValuePair pair = params.get(i);
            String name = pair.getName();
            String value = pair.getValue();
            try {
                String encodeValue = URLEncoder.encode(pair.getValue(), CHARSET_NAME);
                sb.append(name).append(NAME_VALUE_SEPARATOR).append(encodeValue);
            } catch (UnsupportedEncodingException e) {
                Logger.getLogger(SDK_HTTP_LOG_TAG, TAG).d("exception when parse " + value, e);
                continue;
            }
        }
        return url + "?" + sb.toString();
    }

    /**
     * Add https certificates
     *
     * @param certificates
     * @return
     */
    public synchronized static boolean addCertificates(InputStream... certificates) {
        boolean ret = sCertificateUtil.addCertificates(certificates);
        if (ret) {
            OkHttpClient.Builder builder = sOkHttpClient.newBuilder();
            builder.sslSocketFactory(sCertificateUtil.getSSLSocketFactory());
            sClientWithCertificate = builder.build();
        }
        return ret;
    }

    /**
     * Cancel a {@link HttpRequest} request.
     *
     * @param request Which to cancel
     */
    public static void cancel(HttpRequest request) {
        if (null == request) {
            return;
        }
        for (RequestTriple rt : sRequestList) {
            if (rt.httpRequest == request) {
                cancel(rt);
                break;
            }
        }
    }

    /**
     * Cancel all {@HttpRequest} whose tag is the same as parameter.
     *
     * @param tag
     */
    public void cancel(String tag) {
        if (null == tag) {
            return;
        }
        for (RequestTriple rt : sRequestList) {
            if (rt.httpRequest.getTag().equals(tag)) {
                cancel(rt);
            }
        }

    }


    /**
     * Get request numbers of the tag
     *
     * @param tag
     * @return
     */
    public static int getRequestCount(String tag) {
        synchronized (sRequestCounter) {
            Integer count = sRequestCounter.get(tag);
            if (count == null) {
                return 0;
            }
            return count;
        }
    }

    /**
     * Get Current request count
     *
     * @return
     */
    public synchronized static int getCurrentRequestCount() {
        return sRequestList.size();
    }

    /**
     * Set current desktop
     *
     * @param desktop
     */
    public void setCurrentDesktop(String desktop) {
        sCurrentDesktop = desktop;
        cancelOthers(desktop);
    }

    static HttpRequest getHttpRequest(Request request) {
        synchronized (sRequestList) {
            for (RequestTriple rt : sRequestList) {
                String tag = null;
                if (rt.okRequest.tag() != null && rt.okRequest.tag() instanceof okhttp3.HttpUrl) {
                    tag = ((okhttp3.HttpUrl) rt.okRequest.tag()).url().toString();
                } else if (rt.okRequest.tag() != null && rt.okRequest.tag() instanceof String) {
                    tag = (String) rt.okRequest.tag();
                }
                if (tag != null && tag.equals(request.tag())) {
                    return rt.httpRequest;
                }
            }
        }
        if (USE_SYSTEM_OUT_PRINT) {
            System.out.println("getHttpRequest but return null, request: " + request.tag());
        } else {
            Logger.getLogger(SDK_HTTP_LOG_TAG, TAG).d("getHttpRequest but return null, request: " + request.tag());
        }
        return null;
    }

    static void removeHttpRequest(Request request) {
        synchronized (sRequestList) {
            for (RequestTriple rt : sRequestList) {
                String tag = null;
                if (rt.okRequest.tag() != null && rt.okRequest.tag() instanceof okhttp3.HttpUrl) {
                    tag = ((okhttp3.HttpUrl) rt.okRequest.tag()).url().toString();
                } else if (rt.okRequest.tag() != null && rt.okRequest.tag() instanceof String) {
                    tag = (String) rt.okRequest.tag();
                }


                if (tag != null && tag.equals(request.tag())) {
                    sRequestList.remove(rt);
                    break;
                }
            }
        }
    }

    private void cancelOthers(String tag) {
        if (null == tag) {
            return;
        }
//        synchronized (sRequestList) {
//            for (RequestTriple rt : sRequestList) {
//                if (!rt.httpRequest.getTag().equals(tag)) {
//                    cancel(rt);
//                }
//            }
//        }

    }

    private static void checkAndCount(HttpRequest request) {
        if (request == null) {
            throw new NullPointerException("Request cannot be null");
        }
        if (request.getTag() == null) {
            throw new RuntimeException("Tag of request cannot be null");
        } else {
            sCountThread.execute(new CountRunnable(request.getTag()));
        }
        if (USE_SYSTEM_OUT_PRINT) {
            System.out.println(SDK_HTTP_LOG_TAG + "    " + TAG + "    " + request.getTag() + " enqueue " + request.getUrl());
        } else {
            Logger.getLogger(SDK_HTTP_LOG_TAG, TAG).d(request.getTag() + " enqueue " + request.getUrl());
        }

    }

    private static Call makeCall(Request r, boolean selfSignedCert) {
        Call call;
        if (selfSignedCert) {
            if (sClientWithCertificate == null) {
                throw new RuntimeException("Self signed certificate has not been added");
            }
            call = sClientWithCertificate.newCall(r);
        } else {
            call = sOkHttpClient.newCall(r);
        }
        return call;
    }


    private synchronized static void countRequest(String tag) {
        synchronized (sRequestCounter) {
            if (sRequestCounter.containsKey(tag)) {
                int count = sRequestCounter.get(tag);
                count += 1;
                sRequestCounter.put(tag, count);
            } else {
                sRequestCounter.put(tag, 1);
            }
        }
    }


    private static void cancel(final RequestTriple rt) {
        if (rt == null) {
            return;
        }
        sCountThread.execute(new Runnable() {
            @Override
            public void run() {
                if (rt != null) {
                    rt.okCall.cancel();
                }
                synchronized (sRequestList) {
                    sRequestList.remove(rt);
                }
            }
        });

    }

    private static class CountRunnable implements Runnable {
        private String mmTag;

        public CountRunnable(String tag) {
            mmTag = tag;
        }

        @Override
        public void run() {
            countRequest(mmTag);
        }
    }

    private static class RequestTriple {
        HttpRequest httpRequest;
        Request okRequest;
        Call okCall;

        RequestTriple(HttpRequest hr, Request r, Call c) {
            httpRequest = hr;
            okRequest = r;
            okCall = c;
        }
    }

}
