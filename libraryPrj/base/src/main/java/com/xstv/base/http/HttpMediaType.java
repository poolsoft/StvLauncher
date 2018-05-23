package com.xstv.base.http;


import java.nio.charset.Charset;

import okhttp3.MediaType;

/**
 * Content type description of request or response body.
 */
public class HttpMediaType {

    private MediaType mOkMediaType;

    private String mMediaType;

    public HttpMediaType(String mediaType) {
        mMediaType = mediaType;
        mOkMediaType = MediaType.parse(mediaType);
    }

    public String toString() {
        return mMediaType;
    }

    /**
     * Content type description string.
     * @return
     */
    public String getMediaType() {
        return mMediaType;
    }

    public String getType() {
        return mOkMediaType.type();
    }

    public String getSubtype() {
        return mOkMediaType.subtype();
    }

    public Charset getCharset(Charset defaultValue) {
        return mOkMediaType.charset(defaultValue);
    }

    public Charset getCharset() {
        return mOkMediaType.charset();
    }

    public static HttpMediaType parse(String str) {
        return new HttpMediaType(str);

    }

    MediaType getOkMediaType() {
        return mOkMediaType;
    }
}
