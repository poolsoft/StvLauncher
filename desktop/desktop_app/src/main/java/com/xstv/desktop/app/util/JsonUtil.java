/**
 *
 */

package com.xstv.desktop.app.util;

import com.google.gson.Gson;
import com.xstv.library.base.LetvLog;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.zip.GZIPInputStream;

/**
 * @author yinxinya
 * @version 1.0
 * @title:
 * @description:
 * @company: 乐视网信息技术（北京）股份有限公司
 * @created 2012-11-9
 * @changeRecord
 */
public class JsonUtil {
    private static String TAG = "JsonUtil";
    private static final Gson gson = new Gson();

    public static <T> T parse(String str, JsonParsingInterceptor<T> interceptor) {
        Type parsingType = null;
        T t = null;
        if (interceptor == null) {
            throw new RuntimeException("beforeParsing cannot return null");
        } else {
            parsingType = interceptor.beforeParsing();
        }
        try {
            t = gson.fromJson(str, parsingType);
        } catch (Exception e) {
            LetvLog.w(TAG, " catch error ,parse", e);
            return null;
        }
        if (null != interceptor) {
            t = interceptor.afterParsing(t);
        }
        return t;
    }

    public static <T> T parse(InputStream is, final Class<T> type) {
        return parse(is, false, new JsonParsingInterceptor<T>() {

            @Override
            public Type beforeParsing() {
                return type;
            }

            @Override
            public T afterParsing(T t) {
                return t;
            }
        });
    }

    public static <T> T parse(InputStream is, JsonParsingInterceptor<T> interceptor) {
        return parse(is, false, interceptor);
    }

    public static <T> T parse(InputStream is, boolean isGzip, JsonParsingInterceptor<T> interceptor) {
        if (is != null) {
            Reader reader = null;
            if (isGzip) {
                try {
                    is = new GZIPInputStream(is);
                    reader = new InputStreamReader(is);
                } catch (IOException e) {
                }
            } else {
                reader = new InputStreamReader(is);
            }
            return parse(reader, interceptor);
        }
        return null;
    }

    /**
     * parse json to object, if error occurred when parse
     *
     * @param reader
     * @param interceptor
     * @return object parsed from json, null if cannot parse or parse error
     */
    public static <T> T parse(Reader reader, JsonParsingInterceptor<T> interceptor) {
        Type parsingType = null;
        T t = null;
        if (interceptor == null) {
            throw new RuntimeException("beforeParsing cannot return null");
        } else {
            parsingType = interceptor.beforeParsing();
        }
        try {
            t = gson.fromJson(reader, parsingType);
        } catch (Exception e) {
            LetvLog.w(TAG, " catch error ,parse", e);
            return null;
        }
        if (null != interceptor) {
            t = interceptor.afterParsing(t);
        }
        return t;
    }

    public static <T> T jsonToObject(String json, Type type) {
        T t = null;
        if (json == null || type == null) {
            return null;
        }
        try {
            t = gson.fromJson(json, type);
        } catch (Exception e) {
            LetvLog.w(TAG, " catch error ,jsonToObject", e);
        }
        return t;
    }

    /**
     * java对象转换为json string
     *
     * @throws JSONException
     */
    public static String javaToJson(Object object) {
        if (object == null) {
            return "";
        }
        String json;
        json = gson.toJson(object);
        return json;
    }
}
