package com.xstv.library.base.http;


import com.xstv.library.base.Logger;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import static com.xstv.library.base.http.HttpManager.SDK_HTTP_LOG_TAG;
import static com.xstv.library.base.http.HttpManager.USE_SYSTEM_OUT_PRINT;


/**
 * Http request callback.
 */
public abstract class HttpCallback {

    /**
     * Called when the request could not be executed.
     *
     * @param request Original request object of {@link HttpRequest}
     * @param e       Exception
     */
    public abstract void onHttpFailure(HttpRequest request, IOException e);

    /**
     * Called when the HTTP response was successfully returned by remote server.
     * Note this does not mean that the request was really successful.
     *
     * @param response HttpResponse {@link HttpResponse}
     * @throws IOException
     */
    public abstract void onHttpResponse(HttpResponse response) throws IOException;

    private Callback mCallback = new Callback() {
        @Override
        public final void onFailure(Call call, IOException e) {
            if (USE_SYSTEM_OUT_PRINT) {
                System.out.println("onFailure: " + e.getMessage());
            } else {
                Logger.getLogger(SDK_HTTP_LOG_TAG, "HttpCallback").d("onFailure ", e);
            }
            Request request = call.request();
            HttpRequest hq = HttpManager.getHttpRequest(request);
            if (hq != null) {
                HttpFailureRetryPolicy policy = hq.getRetryPolicy();
                if (policy != null) {
                    boolean retry = policy.retry(hq, HttpCallback.this);
                    if (!retry) {
                        onHttpFailure(hq, e);
                    }
                    removeRequest(request);
                } else {
                    onHttpFailure(hq, e);
                    removeRequest(request);
                }
            } else {
                onHttpFailure(hq, e);
            }

        }

        @Override
        public final void onResponse(final Call call, final Response response) throws IOException {
            if (response != null) {
                if (USE_SYSTEM_OUT_PRINT) {
                    System.out.println("onResponse: " + ", isSuccessful: " + response.isSuccessful());
                } else {
                    Logger.getLogger(SDK_HTTP_LOG_TAG, "HttpCallback").d(response.message()
                            + ", isSuccessful: " + response.isSuccessful()
                            + "->" + response.code() + "   " + "onResponse: " + response.toString());
                }
                HttpResponse ret = new HttpResponse(response);
                onHttpResponse(ret);
                Request request = response.request();
                removeRequest(request);
            }
        }
    };

    private void removeRequest(Request request) {
        HttpManager.removeHttpRequest(request);
    }


    Callback getCallback() {
        return mCallback;
    }
}
