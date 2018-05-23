package com.xstv.base.http;

import com.xstv.base.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * A name / value pair parameter used as an element of HTTP messages.
 */

public class HttpNameValuePair {

    public static final String TAG = "HttpNameValuePair";


    private String mName;
    private String mValue;

    private boolean needEncode;

    /**
     * Constructor, name cannot be null.
     *
     * @param name
     * @param value
     */
    public HttpNameValuePair(String name, String value) {
        mName = name;
        mValue = value;
    }

    public HttpNameValuePair(String name, String value, boolean needEncode) {
        this(name, value);
        this.needEncode = needEncode;
    }

    /**
     * Get name
     *
     * @return Name of this name / value pair
     */
    public String getName() {
        if (needEncode) {
            try {
                String encoded = URLEncoder.encode(mName, HttpManager.CHARSET_NAME);
                return encoded;
            } catch (UnsupportedEncodingException e) {
                Logger.getLogger(HttpManager.SDK_HTTP_LOG_TAG, TAG).d("exception when encode", e);
            }
        }
        return mName;
    }

    /**
     * Get value
     *
     * @return Value of this name / value pair
     */
    public String getValue() {
        if (needEncode) {
            try {
                String encoded = URLEncoder.encode(mValue, HttpManager.CHARSET_NAME);
                return encoded;
            } catch (UnsupportedEncodingException e) {
                Logger.getLogger(HttpManager.SDK_HTTP_LOG_TAG, TAG).d("exception when encode", e);
            }
        }
        return mValue;
    }

    public boolean isNeedEncode() {
        return needEncode;
    }


    @Override
    public String toString() {
        String nameAndValue = getName() + HttpManager.NAME_VALUE_SEPARATOR + getValue();
        return nameAndValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof HttpNameValuePair) {
            HttpNameValuePair another = (HttpNameValuePair) o;
            if (!mName.equals(another.getName())) {
                return false;
            }
            if (mValue == null) {
                return another.getValue() == null;
            }
            return mValue.equals(another.getValue());
        }
        return false;
    }
}
