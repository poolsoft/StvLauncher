/**
 *
 */

package com.xstv.base;


import android.util.Log;

/**
 * @author tongyonghui
 */
public final class LetvLog {
    public static boolean DBG_METHOD = true;

    private static final String APP_TAG = "Launcher";

    public static void v(String msg) {
        Log.v(APP_TAG, msg);
    }

    public static void v(String tag, String msg) {
        Log.v(APP_TAG, tag + " -- " + msg);
    }

    public static void d(String msg) {
        Log.d(APP_TAG, msg);
    }

    public static void d(String tag, String msg) {
        Log.d(APP_TAG, tag + " -- " + msg);
    }

    public static void d(String tag, String msg, Throwable tr) {
        Log.d(APP_TAG, tag + " -- " + msg, tr);
    }

    public static void d(Class<?> c, String msg) {
        Log.d(APP_TAG, c.getSimpleName() + " -- " + msg);
    }

    public static void i(String msg) {
        Log.i(APP_TAG, msg);
    }

    public static void i(String tag, String msg) {
        Log.i(APP_TAG, tag + " -- " + msg);
    }

    public static void w(String msg) {
        Log.w(APP_TAG, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(APP_TAG, tag + " -- " + msg);
    }

    public static void w(String tag, Throwable tr) {
        Log.w(APP_TAG, tr);
    }

    public static void w(String tag, String msg, Throwable tr) {
        Log.w(APP_TAG, tag + " -- " + msg, tr);
    }

    public static void w(Class<?> c, String msg, Throwable tr) {
        Log.w(APP_TAG, c.getSimpleName() + " -- " + msg, tr);
    }

    public static void w(Class<?> c, String msg) {
        Log.d(APP_TAG, c.getSimpleName() + " -- " + msg);
    }

    public static void e(String msg) {
        Log.e(APP_TAG, msg);
    }

    public static void e(String msg, Throwable tr) {
        Log.e(APP_TAG, msg, tr);
    }

    public static void e(String tag, String msg) {
        Log.e(APP_TAG, tag + " -- " + msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(APP_TAG, tag + " -- " + msg, tr);
    }

    public static long methodBegin(String tag) {
        long currentTime = 0;
        if (DBG_METHOD) {
            currentTime = System.currentTimeMillis();
            String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
            StringBuilder msg = new StringBuilder();
            msg.append(methodName).append(" begin");
            LetvLog.d(msg.toString());
            msg.delete(0, msg.length());
        }
        return currentTime;
    }

    public static void methodEnd(String tag, long begin) {
        if (DBG_METHOD) {
            String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
            StringBuilder msg = new StringBuilder();
            msg.append("wait ");
            msg.append(System.currentTimeMillis() - begin);
            msg.append(" ms ");
            msg.append("for method ");
            msg.append(methodName);
            LetvLog.d(tag, msg.toString());
            msg.delete(0, msg.length());
        }
    }

    public static void methodEnd(String tag, long begin, String obj) {
        if (DBG_METHOD) {
            String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
            StringBuilder msg = new StringBuilder();
            msg.append("wait ");
            msg.append(System.currentTimeMillis() - begin);
            msg.append(" ms ");
            msg.append("for method  ");
            msg.append(methodName);
            msg.append("  to deal ");
            msg.append(obj);
            LetvLog.d(tag, msg.toString());
            msg.delete(0, msg.length());
        }
    }
}
