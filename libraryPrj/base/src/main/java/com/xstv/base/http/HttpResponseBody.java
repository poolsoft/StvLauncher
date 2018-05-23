package com.xstv.base.http;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.ResponseBody;

/**
 * HTTP response body
 */
public final class HttpResponseBody {

    private ResponseBody mResponseBody;

    HttpResponseBody(ResponseBody body) {
        mResponseBody = body;
    }

    /**
     * Get {@link InputStream} of the response body.
     * @return
     * @throws IOException
     */
    public final InputStream byteStream() throws IOException {
        return mResponseBody.byteStream();
    }

    /**
     * Return the response as string.
     * @return
     * @throws IOException
     */
    public final String string() throws IOException {
        return new String(mResponseBody.bytes(), charset().name());
    }

    MediaType contentType() {
        return mResponseBody.contentType();
    }

    private Charset charset() {
        MediaType contentType = mResponseBody.contentType();
        return contentType != null ? contentType.charset(okhttp3.internal.Util.UTF_8) : okhttp3.internal.Util.UTF_8;
    }
}
