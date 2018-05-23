package com.xstv.base.http;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Fragment of HTTP url. For example http://api.live.letv.com/v1/playbill/current2/100?channelIds=71
 * is a url, v1, playbill, current2, 100 is fragment.
 */
public class HttpPathFragment {

    public static final String TAG = "HttpPathFragment";

    /*
     * Fragment string
     */
    private String fragment;

    /*
     * Does this fragment need to be encoded
     */
    private boolean needEncode;

    public HttpPathFragment(String fragment, boolean needEncode) {
        this.fragment = fragment;
        this.needEncode = needEncode;
    }

    public HttpPathFragment(String fragment) {
        this(fragment, false);
    }

    /**
     * Get Fragment string.
     *
     * @return Fragment string
     */
    public String getFragment() {
        if (needEncode) {
            try {
                return URLEncoder.encode(fragment, HttpManager.CHARSET_NAME);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return fragment;
    }

    /**
     * Does this fragment need to be encoded.
     *
     * @return Encode or not
     */
    public boolean isNeedEncode() {
        return needEncode;
    }

    @Override
    public String toString() {
        return "/" + getFragment();
    }
}
