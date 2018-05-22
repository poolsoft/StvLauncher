
package com.xstv.desktop.app.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class IMEUtil {

    public static void showIME(Context context, View view) {
        ((InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(view, 0);
    }

    public static void hideIME(Context context, View view) {
        InputMethodManager inputMethodManager = ((InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE));
        if (view != null && view.getWindowToken() != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * Change IME status: when IME open, close it. Otherwise open it.
     * 
     * @param context
     */
    public static void changeIME(Context context) {
        InputMethodManager m = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static String filterString(String str) throws PatternSyntaxException {
        String regEx = "[^a-zA-Z0-9\u4e00-\u9fa5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }
}
