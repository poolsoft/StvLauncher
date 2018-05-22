package com.xstv.launcher.util;

import android.content.Context;

import java.net.URLEncoder;

/**
 * Created by panfeng on 16-8-4.
 */
public class StringUtils {
//    protected static DecimalFormat dFormat = new DecimalFormat("#0.0");

    public StringUtils() {
    }

    public static String encoding(String str) {
        return isEmpty(str)?"": URLEncoder.encode(str);
    }

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str) || "NULL".equals(str.toUpperCase());
    }

    public static final long toLong(Object _obj, long _defaultValue) {
        if(isEmpty(String.valueOf(_obj))) {
            return _defaultValue;
        } else {
            try {
                return Long.parseLong(String.valueOf(_obj));
            } catch (Exception var4) {
                return _defaultValue;
            }
        }
    }

    public static String removeBlankAndN(String str) {
        if(!isEmpty(str)) {
            str = str.replace("\n", "").replace("\t", "").trim();
        }

        return str;
    }

    public static String getStringById(Context context, int id){
        return context.getResources().getString(id);
    }

    /**
     * Check a string is empty or not.
     *
     * @param str
     * @return boolean
     */
    public static boolean isStringEmpty(String str) {
        return str == null || str.length() == 0 || str.trim().length() == 0;
    }

    /**
     * Check a string is empty or not.
     *
     * @param str
     * @return boolean
     */
    public static boolean isStringNotEmpty(String str) {
        return !isStringEmpty(str);
    }
}
