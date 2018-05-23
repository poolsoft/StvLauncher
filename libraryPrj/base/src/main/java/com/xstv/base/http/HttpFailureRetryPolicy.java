package com.xstv.base.http;


public abstract class HttpFailureRetryPolicy {
    /**
     * Is new request enqueued
     * @param request
     * @param callback
     * @return true if a new request add to the queue
     */
    boolean retry(HttpRequest request, HttpCallback callback) {
        HttpRequest newRequest = createNewRequest(request);
        if (null == newRequest) {
            return false;
        }
        HttpManager.enqueue(newRequest, callback);
        return true;
    }

    /**
     * Make a new request to retry
     * @param request
     * @return
     */
    public abstract HttpRequest createNewRequest(HttpRequest request);

}
