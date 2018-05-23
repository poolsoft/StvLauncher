package com.xstv.base.http;



import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Headers;

/**
 * Headers of request or response
 */
public class HttpHeaders {

    private Headers mOkHeaders;


    private Map<String, String> mHeadersMap;

    /*
     package accessibility
     */
    HttpHeaders(Headers headers) {
        mOkHeaders = headers;
        mHeadersMap = new HashMap<String, String>();
        Set<String> names = headers.names();
        for (Iterator<String> it = names.iterator(); it.hasNext();) {
            String name = it.next();
            String value = headers.get(name);
            mHeadersMap.put(name, value);
        }
    }

    /**
     * Create a empty header.
     */
    public HttpHeaders() {
        mHeadersMap = new HashMap<String, String>();
        mOkHeaders = Headers.of(mHeadersMap);
    }

    /**
     * Create headers with headers.
     * @param headers
     */
    public HttpHeaders(Map<String, String> headers) {
        mHeadersMap = headers;
        mOkHeaders = Headers.of(mHeadersMap);
    }

    /**
     * Create a Headers with key and value.
     * @param key
     * @param value
     */
    public HttpHeaders(String key, String value) {
        mHeadersMap = new HashMap<String, String>();
        mHeadersMap.put(key, value);
        mOkHeaders = Headers.of(mHeadersMap);
    }

    /**
     * Add a pair of string as header.
     * @param key
     * @param value
     */
    public void addHeader(String key, String value) {
        mHeadersMap.put(key, value);
    }

    /**
     * Add a pair as header.
     * @param pair
     */
    public void addHeader(HttpNameValuePair pair) {
        mHeadersMap.put(pair.getName(), pair.getValue());
    }


    /**
     * get the value of name.
     * @param name
     * @return value
     */
    public String get(String name) {
        return mOkHeaders.get(name);
    }

    /**
     * Get date value in headers.
     * @param name
     * @return
     */
    public Date getDate(String name) {
        return mOkHeaders.getDate(name);
    }

    /**
     * Get header count.
     * @return
     */
    public int size() {
        return mOkHeaders.size();
    }

    /**
     * Is header empty.
     * @return
     */
    public boolean isEmpty() {
        return size() <= 0;
    }

    /**
     * Get the name at index.
     * @param index
     * @return
     */
    public String name(int index) {
        return mOkHeaders.name(index);
    }

    /**
     * Get the value at index.
     * @param index
     * @return
     */
    public String value(int index) {
        return mOkHeaders.value(index);
    }

    /**
     * Get all names of this headers.
     * @return
     */
    public Set<String> names() {
        return mOkHeaders.names();
    }

    /**
     * Get the values as a list of the parameter name.
     * @param name
     * @return
     */
    public List<String> values(String name) {
        return mOkHeaders.values(name);
    }

    @Override
    public String toString() {
        return mOkHeaders.toString();
    }

    /**
     * Remove the header whose name equals parameter.
     * @param name
     */
    public void remove(String name) {
        mHeadersMap.remove(name);
    }

    /**
     * Clear the headers.
     */
    public void removeAll() {
        mHeadersMap.clear();

    }

    Headers getOkHeaders() {
        return mOkHeaders;
    }
}
