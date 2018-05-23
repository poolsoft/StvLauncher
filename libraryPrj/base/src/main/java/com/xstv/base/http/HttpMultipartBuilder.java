package com.xstv.base.http;


import okhttp3.MultipartBody;

public class HttpMultipartBuilder {

    private MultipartBody.Builder mOkBuilder;

    public HttpMultipartBuilder() {
        mOkBuilder = new MultipartBody.Builder();
    }

    public HttpMultipartBuilder(String boundary) {
        mOkBuilder = new MultipartBody.Builder(boundary);
    }

    public HttpMultipartBuilder type(HttpMediaType mediaType) {
        mOkBuilder.setType(mediaType.getOkMediaType());
        return this;
    }

    public HttpMultipartBuilder addPart(HttpRequestBody body) {
        mOkBuilder.addPart(body.getOkBody());
        return this;
    }

    public HttpMultipartBuilder addPart(HttpHeaders headers, HttpRequestBody body) {
        mOkBuilder.addPart(headers.getOkHeaders(), body.getOkBody());
        return this;
    }

    public HttpRequestBody build() {
        return new HttpRequestBody(mOkBuilder.build());
    }

    MultipartBody.Builder getOkBuilder() {
        return mOkBuilder;
    }
}
