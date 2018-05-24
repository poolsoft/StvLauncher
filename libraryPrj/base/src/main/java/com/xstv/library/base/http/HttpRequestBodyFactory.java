package com.xstv.library.base.http;



import java.io.File;

import okhttp3.RequestBody;


public class HttpRequestBodyFactory {

    public static HttpRequestBody create(String content) {
        return create(null, content);
    }

    public static HttpRequestBody create(HttpMediaType mediaType, String content) {
        if (null == mediaType) {
            return new HttpRequestBody(RequestBody.create(null, content));
        }
        return new HttpRequestBody(RequestBody.create(mediaType.getOkMediaType(), content));
    }

    public static HttpRequestBody create(HttpMediaType mediaType, byte[] content) {
        return create(mediaType, content, 0, content.length);
    }

    public static HttpRequestBody create(HttpMediaType mediaType,
                                         byte[] content, int offset, int count) {
        return new HttpRequestBody(
                RequestBody.create(mediaType.getOkMediaType(), content, offset, count));
    }

    public static HttpRequestBody create(HttpMediaType mediaType, File file) {
        return new HttpRequestBody(RequestBody.create(mediaType.getOkMediaType(), file));
    }
}
