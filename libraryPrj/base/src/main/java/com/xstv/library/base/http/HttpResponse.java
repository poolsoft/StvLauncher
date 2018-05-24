package com.xstv.library.base.http;

import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

/**
 * HTTP response
 */
public class HttpResponse {

    private final Response mResponse;

    HttpResponse(Response response) {
        mResponse = response;
    }

    /**
     * Get response body/
     *
     * @return {@link HttpResponseBody}
     */
    public HttpResponseBody body() {
        return new HttpResponseBody(mResponse.body());
    }

    /**
     * HTTP status code
     * @return Code
     */
    public int code() {
        return mResponse.code();
    }

    /**
     * Returns true if the code is in [200..300)
     * @return True if the code is in [200..300), false otherwise
     */
    public boolean isSuccessful() {
        return mResponse.isSuccessful();
    }

    /**
     * Get header value of name
     * @param name
     * @return Value of name parameter
     */
    public String header(String name) {
        return mResponse.header(name);
    }

    /**
     * Get header list of name
     * @param name
     * @return Header list
     */
    public List<String> headers(String name) {
        return mResponse.headers(name);
    }

    /**
     * Get headers as map
     * @return
     */
    public Map<String, List<String>> headersMap() {
        return mResponse.headers().toMultimap();
    }

    public HttpHeaders headers() {
        return new HttpHeaders(mResponse.headers());
    }

    public HttpRequest request() {
        Request request = mResponse.request();
        return new HttpRequest(request);
    }

    @Override
    public String toString() {
            return super.toString() + ": " + mResponse.toString() + mResponse.getClass().getName() + '@' + Integer.toHexString(mResponse.hashCode());
    }
}
