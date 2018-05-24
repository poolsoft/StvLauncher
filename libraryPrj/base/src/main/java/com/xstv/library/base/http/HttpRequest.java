package com.xstv.library.base.http;


import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * HTTP request.
 */
public class HttpRequest {

    private String mUrl;
    private String mMethod;
    private String mTag;
    private Headers mOkHeaders;
    private RequestBody mOkBody;

    private HttpURL mHttpURL;
    private HttpHeaders mHeaders;
    private HttpFailureRetryPolicy mRetryPolicy;

    private Request mRequest;

    private HttpRequest() {
    }

    HttpRequest(Request request) {
        mUrl = request.url().toString();
        mMethod = request.method();
        mTag = (String) request.tag();
    }

    HttpRequest(Builder builder) {
        mUrl = builder.mmUrl;
        mMethod = builder.mmMethod;
        if (mMethod == null) {
            mMethod = HttpManager.METHOD_GET;
        }
        mTag = builder.mmTag;
        mHttpURL = builder.mmHttpURL;
        mOkHeaders = builder.mmOkHeaders.build();
        mOkBody = builder.mmOkBody;
        mRetryPolicy = builder.mmRetryPolicy;
        mHeaders = builder.mmHeaders;
        if (mHttpURL != null) {
            mUrl = mHttpURL.getUrl();
        }
    }

    Request toOkRequest() {
        Request.Builder builder = new Request.Builder();
        builder.tag(UUID.randomUUID().toString());
        if (mUrl != null) {
            builder.url(mUrl);
            //mHttpURL = HttpURL.parse(mUrl);
        } else {
            if (mHttpURL != null) {
                builder.url(mHttpURL.getUrl());
            } else {
                throw new RuntimeException("HTTP URL cannot be null");
            }
        }
        if (HttpManager.METHOD_POST.equalsIgnoreCase(mMethod)) {
            builder.post(mOkBody);
        } else {
            builder.get();
        }
        builder.headers(mOkHeaders);
        mRequest = builder.build();
        return mRequest;
    }

    /**
     * Get HTTP url
     *
     * @return Url
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * Get Http method
     *
     * @return Method
     */
    public String getMethod() {
        return mMethod;
    }

    /**
     * Get tag
     *
     * @return Tag
     */
    public String getTag() {
        return mTag;
    }

    Headers getOkHeaders() {
        return mOkHeaders;
    }

    /**
     * Get Headers
     *
     * @return
     */
    public HttpHeaders getHeaders() {
        return mHeaders;
    }

    /**
     *
     * @return
     */
    public HttpFailureRetryPolicy getRetryPolicy() {
        return mRetryPolicy;
    }

    Request getRequest() {
        return mRequest;
    }

    /**
     * Class to build {@link HttpRequest}
     */
    public static class Builder {
        private String mmUrl;
        private String mmTag;
        private String mmMethod;
        private Headers.Builder mmOkHeaders;
        private RequestBody mmOkBody;

        private HttpURL mmHttpURL;
        private HttpHeaders mmHeaders;

        private HttpFailureRetryPolicy mmRetryPolicy;

        /**
         * Constructor
         */
        public Builder() {
            mmOkHeaders = new Headers.Builder();
            mmHeaders = new HttpHeaders();
        }

        /**
         * Set Url
         *
         * @param url
         * @return
         */
        public Builder url(String url) {
            mmUrl = url;
            return this;
        }

        /**
         * Set tag
         *
         * @param tag
         * @return
         */
        public Builder tag(String tag) {
            mmTag = tag;
            return this;
        }

        /**
         * Set method
         *
         * @param method
         * @return
         */
        public Builder method(String method) {
            mmMethod = method;
            return this;
        }

        public Builder post(HttpRequestBody body) {
            mmMethod = HttpManager.METHOD_POST;
            mmOkBody = body.getOkBody();
            return this;
        }

        /**
         * Set headers.
         *
         * @param headers
         * @return
         */
        public Builder header(HttpHeaders headers) {
            Set<String> names = headers.names();
            for (Iterator<String> it = names.iterator(); it.hasNext(); ) {
                String name = it.next();
                String value = headers.get(name);
                addHeader(name, value);
                mmHeaders.addHeader(name, value);
            }
            return this;
        }


        /**
         * Set header, Note this will replace the {@key } headers added before.
         *
         * @param key
         * @param value
         * @return
         */
        public Builder header(String key, String value) {
            mmOkHeaders.set(key, value);
            mmHeaders.addHeader(key, value);
            return this;
        }

        /**
         * Set header, Note this will replace the {@pair.getName() } headers added before.
         *
         * @param pair
         * @return
         */
        public Builder header(HttpNameValuePair pair) {
            mmOkHeaders.set(pair.getName(), pair.getValue());
            mmHeaders.addHeader(pair.getName(), pair.getValue());
            return this;
        }

        /**
         * Add header
         *
         * @param key
         * @param value
         * @return
         */
        public Builder addHeader(String key, String value) {
            mmOkHeaders.add(key, value);
            mmHeaders.addHeader(key, value);
            return this;
        }

        /**
         * Add Header
         *
         * @param pair
         * @return
         */
        public Builder addHeader(HttpNameValuePair pair) {
            mmOkHeaders.add(pair.getName(), pair.getValue());
            mmHeaders.addHeader(pair.getName(), pair.getValue());
            return this;
        }

        /**
         * Using {@HttpURL} to make up a url string.
         *
         * @param httpURL
         * @return
         */
        public Builder httpURL(HttpURL httpURL) {
            mmHttpURL = httpURL;
            return this;
        }

        public Builder retryPolicy(HttpFailureRetryPolicy policy) {
            mmRetryPolicy = policy;
            return this;
        }

        /**
         * Create {@link HttpRequest} instance
         *
         * @return
         */
        public HttpRequest build() {
            return new HttpRequest(this);
        }

        String getUrl() {
            return mmUrl;
        }


        String getTag() {
            return mmTag;
        }


        String getMethod() {
            return mmMethod;
        }

        HttpURL getHttpURL() {
            return mmHttpURL;
        }

    }


}
