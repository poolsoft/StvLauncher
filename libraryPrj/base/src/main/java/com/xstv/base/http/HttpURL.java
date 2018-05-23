package com.xstv.base.http;



import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;

public class HttpURL {

    private String mScheme;
    private String mHost;
    private int mPort;
    private List<HttpPathFragment> mPathSegments;

    private List<HttpNameValuePair> mQueryPairs;

    private String mUrl;


    HttpURL(Builder builder) {
        mScheme = builder.mmScheme;
        mHost = builder.mmHost;
        mPort = builder.mmPort;
        mPathSegments = builder.mmPathFragments;
        mQueryPairs = builder.mmQueryPairs;
        mUrl = builder.toString();
    }

    /**
     * Get mUrl as String.
     *
     * @return mUrl string.
     */
    public String getUrl() {
        return mUrl;
    }

    public static HttpURL parse(String link) {
        HttpUrl okUrl = HttpUrl.parse(link);
        Builder builder = new Builder();
        builder.scheme(okUrl.scheme());
        builder.host(okUrl.host());
        for (String seg : okUrl.pathSegments()) {
            builder.pathFragment(seg);
        }
        for (String name : okUrl.queryParameterNames()) {
            builder.queryParameter(new HttpNameValuePair(name, okUrl.queryParameter(name)));
        }
        return builder.build();
    }

    public String getScheme() {
        return mScheme;
    }

    public String getHost() {
        return mHost;
    }

    public int getPort() {
        return mPort;
    }

    public List<HttpPathFragment> getPathSegments() {
        return mPathSegments;
    }

    public List<HttpNameValuePair> getQueryPairs() {
        return mQueryPairs;
    }

    /**
     * HttpURL Builder.
     */
    public static class Builder {

        private String mmScheme;
        private String mmHost;
        private int mmPort;
        private List<HttpPathFragment> mmPathFragments = new ArrayList<HttpPathFragment>();
        private List<HttpNameValuePair> mmQueryPairs = new ArrayList<HttpNameValuePair>();

        /**
         * scheme of mUrl, http, https.
         *
         * @param scheme
         * @return
         */
        public Builder scheme(String scheme) {
            mmScheme = scheme;
            return this;
        }

        /**
         * Host or ip address of mUrl.
         *
         * @param host
         * @return
         */
        public Builder host(String host) {
            mmHost = host;
            return this;
        }

        /**
         * @param port
         * @return
         */
        public Builder port(int port) {
            mmPort = port;
            return this;
        }

        /**
         * @param fragment
         * @return
         */
        public Builder pathFragment(String fragment) {
            mmPathFragments.add(new HttpPathFragment(fragment));
            return this;
        }

        /**
         * Add a path fragment.
         *
         * @param fragment
         * @return
         */
        public Builder pathFragment(HttpPathFragment fragment) {
            mmPathFragments.add(fragment);
            return this;
        }

        /**
         * @param pathFragments
         * @return
         */
        public Builder pathStringFragments(List<String> pathFragments) {
            for (String path : pathFragments) {
                pathFragment(path);
            }
            return this;
        }

        /**
         * Add a list of path fragment
         *
         * @param pathFragments
         * @return
         */
        public Builder pathFragments(List<HttpPathFragment> pathFragments) {
            mmPathFragments.addAll(pathFragments);
            return this;
        }

        /**
         * Add a query parameter
         *
         * @param queryPair
         * @return
         */
        public Builder queryParameter(HttpNameValuePair queryPair) {
            mmQueryPairs.add(queryPair);
            return this;
        }

        /**
         * Add a list of query parameter
         *
         * @param queryPairs
         * @return
         */
        public Builder queryParameters(List<HttpNameValuePair> queryPairs) {
            mmQueryPairs.addAll(queryPairs);
            return this;
        }

        /**
         * Create a HttpURL.
         *
         * @return
         */
        public HttpURL build() {
            return new HttpURL(this);
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append(mmScheme);
            result.append("://");
            result.append(mmHost);
            if (mmPort > 0) {
                result.append(":").append(mmPort);
            }
            if (mmPathFragments.size() > 0) {
                for (HttpPathFragment fragment : mmPathFragments) {
                    result.append(fragment.toString());
                }
            }
            if (mmQueryPairs.size() > 0) {
                for (int i = 0; i < mmQueryPairs.size(); i++) {
                    HttpNameValuePair pair = mmQueryPairs.get(i);
                    if (i == 0) {
                        result.append("?");
                        result.append(pair.toString());
                    } else {
                        result.append(HttpManager.QUERY_PARAM_SEPARATOR);
                        result.append(pair.toString());
                    }
                }
            }
            return result.toString();
        }

    }

}
