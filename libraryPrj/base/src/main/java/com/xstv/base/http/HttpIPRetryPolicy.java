package com.xstv.base.http;


import com.xstv.base.Logger;

/**
 * Retry request with ip address when failure. For example, when domain hijacking.
 * When retry, {@link HttpIPRetryPolicy} will create a new {@link HttpRequest}
 * and enqueue it to {@link HttpManager}
 */
public class HttpIPRetryPolicy extends HttpFailureRetryPolicy {

    private static final String TAG = "HttpIPRetryPolicy";

    private String[] mIPAddresses;

    private int mCurrentIndex;

    /**
     * @param ips IP address for retry.
     */
    public HttpIPRetryPolicy(String[] ips) {
        mIPAddresses = ips;
    }

    @Override
    public HttpRequest createNewRequest(HttpRequest request) {
        if (!HttpManager.METHOD_GET.equals(request.getMethod())) {
            if (HttpManager.USE_SYSTEM_OUT_PRINT) {
                System.out.println("Only get request can be retried");
            } else {
                Logger.getLogger(HttpManager.SDK_HTTP_LOG_TAG, TAG).d("Only get request can be retried");
            }
            return null;
        }
        if (mIPAddresses == null || mIPAddresses.length <= 0) {
            throw new RuntimeException("No ip address");
        }
        if (mCurrentIndex >= mIPAddresses.length) {
            if (HttpManager.USE_SYSTEM_OUT_PRINT) {
                System.out.println("All ip address has been retried");
            } else {
                Logger.getLogger(HttpManager.SDK_HTTP_LOG_TAG, TAG).d("All ip address has been retried");
            }
            return null;
        }
        if (HttpManager.USE_SYSTEM_OUT_PRINT) {
            System.out.println("try ip address index: " + mCurrentIndex + ", ip: " + mIPAddresses[mCurrentIndex]);
        } else {
            Logger.getLogger(HttpManager.SDK_HTTP_LOG_TAG, TAG).d("try ip address index: " + mCurrentIndex + ", ip: " + mIPAddresses[mCurrentIndex]);
        }
        String link = request.getUrl();
        HttpURL url = HttpURL.parse(link);

        HttpURL.Builder urlBuilder = new HttpURL.Builder();
        urlBuilder.scheme(url.getScheme());
        urlBuilder.host(mIPAddresses[mCurrentIndex++]);
        urlBuilder.pathFragments(url.getPathSegments());
        urlBuilder.queryParameters(url.getQueryPairs());


        HttpRequest.Builder requestBuilder = new HttpRequest.Builder();
        requestBuilder.tag(request.getTag());
        requestBuilder.httpURL(urlBuilder.build());
        requestBuilder.method(HttpManager.METHOD_GET);
        requestBuilder.header(request.getHeaders());
        requestBuilder.retryPolicy(this);
        return requestBuilder.build();
    }

}
