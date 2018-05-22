
package com.xstv.base;

import android.text.TextUtils;
import android.util.Log;

/**
 * API for sending log output.
 * <p>
 * Logger is generally used like this:
 *
 * <pre>
 * String final MODULE = "XXModule"; // such as Logger.MODULE_SDK
 * String final TAG = "MyActivity";
 * Logger logger = Logger.getLogger(MODULE, TAG);
 * logger.d("say something");
 * </pre>
 *
 * Output is like this:
 * <pre>
 * W/Launcher(16528): [XXModule][MyActivity]:say something
 * </pre>
 */
public final class Logger {

    private static final boolean DEBUG = true;
    private static boolean LOGV_ON = true && DEBUG;
    private static boolean LOGD_ON = true && DEBUG;
    private static boolean LOGI_ON = true && DEBUG;
    private static boolean LOGW_ON = true && DEBUG;
    private static boolean LOGWTF_ON = true && DEBUG;
    private static boolean LOGE_ON = true && DEBUG;

    private static final String APP_TAG = "Launcher";

    /**
     * Module name of SDK
     */
    public static final String MODULE_SDK = "SDK";

    private String mModuleTAG;

    private Logger() {

    }

    private Logger(String module, String tag) {
        mModuleTAG = "[" + module + "][" + tag + "]:";
    }

    /**
     * Get a Logger instance
     *
     * @param module plugin desktop name
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     *            activity where the log call occurs.
     * @return Logger instance
     */
    public static Logger getLogger(String module, String tag) {
        if (TextUtils.isEmpty(module) || TextUtils.isEmpty(tag)) {
            throw new IllegalArgumentException();
        }
        return new Logger(module, tag);
    }

    /**
     * Send a VERBOSE log message.
     *
     * @param msg The message you would like logged.
     */
    public void v(String msg) {
        if (LOGV_ON) {
            Log.v(APP_TAG, mModuleTAG + msg);
        }
    }

    /**
     * Send a VERBOSE log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public void v(String msg, Throwable tr) {
        if (LOGV_ON) {
            Log.v(APP_TAG, mModuleTAG + msg, tr);
        }
    }

    /**
     * Send a DEBUG log message.
     *
     * @param msg The message you would like logged.
     */
    public void d(String msg) {
        if (LOGD_ON) {
            Log.d(APP_TAG, mModuleTAG + msg);
        }
    }

    /**
     * Send a DEBUG log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public void d(String msg, Throwable tr) {
        if (LOGD_ON) {
            Log.d(APP_TAG, mModuleTAG + msg, tr);
        }
    }

    /**
     * Send an INFO log message.
     *
     * @param msg The message you would like logged.
     */
    public void i(String msg) {
        if (LOGI_ON) {
            Log.i(APP_TAG, mModuleTAG + msg);
        }
    }

    /**
     * Send a INFO log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public void i(String msg, Throwable tr) {
        if (LOGI_ON) {
            Log.i(APP_TAG, mModuleTAG + msg, tr);
        }
    }

    /**
     * Send a WARN log message.
     *
     * @param msg The message you would like logged.
     */
    public void w(String msg) {
        if (LOGW_ON) {
            Log.w(APP_TAG, mModuleTAG + msg);
        }
    }

    /**
     * Send a WARN log message and log the exception.
     *
     * @param tr An exception to log
     */
    public void w(Throwable tr) {
        if (LOGW_ON) {
            Log.w(APP_TAG, mModuleTAG, tr);
        }
    }

    /**
     * Send a WARN log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public void w(String msg, Throwable tr) {
        if (LOGW_ON) {
            Log.w(APP_TAG, mModuleTAG + msg, tr);
        }
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen. The error will always
     * be logged at level ASSERT with the call stack. Depending on system configuration, a report
     * may be added to the DropBoxManager and/or the process may be terminated immediately with an
     * error dialog.
     *
     * @param msg The message you would like logged.
     */
    public void wtf(String msg) {
        if (LOGWTF_ON) {
            Log.wtf(APP_TAG, mModuleTAG + msg);
        }
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen. Similar to
     * wtf(Throwable), with a message as well.
     *
     * @param tr An exception to log
     */
    public void wtf(Throwable tr) {
        if (LOGWTF_ON) {
            Log.wtf(APP_TAG, mModuleTAG, tr);
        }
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen. Similar to
     * wtf(Throwable), with a message as well.
     *
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public void wtf(String msg, Throwable tr) {
        if (LOGWTF_ON) {
            Log.wtf(APP_TAG, mModuleTAG + msg, tr);
        }
    }

    /**
     * Send an ERROR log message.
     *
     * @param msg The message you would like logged.
     */
    public void e(String msg) {
        if (LOGE_ON) {
            Log.e(APP_TAG, mModuleTAG + msg);
        }
    }

    /**
     * Send a ERROR log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public void e(String msg, Throwable tr) {
        if (LOGE_ON) {
            Log.e(APP_TAG, mModuleTAG + msg, tr);
        }
    }
}
