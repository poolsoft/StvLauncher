
package com.xstv.desktop.app.util;

import java.lang.reflect.Type;

/**
 * Interceptor on parse object from json. Parsing by {@link com.google.gson.Gson#fromJson(java.io.Reader, Type)}, Type can be create by this method {@link #beforeParsing()}. If some amending are
 * needed for parsed object, use method {@link #afterParsing(Object)}
 * 
 * @author huihuangui
 * @param <T>
 */
public interface JsonParsingInterceptor<T> {

    /**
     * get a Type or Class for parsing
     * 
     * @return Type which type's instance to be parsed
     */
    Type beforeParsing();

    /**
     * amend to the parsed object
     * 
     * @param t parsed object from json
     * @return parsed object from json, null means data is error
     */
    T afterParsing(T t);

}
