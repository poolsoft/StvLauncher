package com.xstv.base.http;



import java.io.IOException;

import okhttp3.RequestBody;

/**
 * Request body.
 */
public class HttpRequestBody {

    private RequestBody mOkBody;

    HttpRequestBody(RequestBody mOkBody) {
        this.mOkBody = mOkBody;
    }

    RequestBody getOkBody() {
        return mOkBody;
    }

    public HttpMediaType contentType() {
        return new HttpMediaType(mOkBody.contentType().type());
    }

    public long contentLength() {
        try {
            return mOkBody.contentLength();
        } catch (IOException e) {
            return -1;
        }
    }
}
