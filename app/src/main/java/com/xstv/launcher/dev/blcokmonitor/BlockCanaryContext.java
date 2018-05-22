package com.xstv.launcher.dev.blcokmonitor;

import android.content.Context;

import java.io.File;

public class BlockCanaryContext implements IBlockCanaryContext {

    private static Context sAppContext;
    private static BlockCanaryContext sInstance = null;

    public BlockCanaryContext() {
    }

    public static void init(Context context, BlockCanaryContext blockCanaryContext) {
        sAppContext = context;
        sInstance = blockCanaryContext;
    }

    public static BlockCanaryContext get() {
        if (sInstance == null) {
            throw new RuntimeException("BlockCanaryContext not init");
        } else {
            return sInstance;
        }
    }

    public Context getContext() {
        return sAppContext;
    }

    /**
     * qualifier which can specify this installation, like version + flavor
     * 
     * @return apk qualifier
     */
    public String getQualifier() {
        return "Unspecified";
    }

    /**
     * Get user id
     * 
     * @return user id
     */
    public String getUid() {
        return "0";
    }

    /**
     * Network type
     * 
     * @return String like 2G, 3G, 4G, wifi, etc.
     */
    public String getNetworkType() {
        return "un-check";
    }

    /**
     * Config monitor duration, after this time BlockCanary will stop, use with {@link BlockCanary}'s isMonitorDurationEnd
     * 
     * @return monitor last duration (in hour)
     */
    public int getConfigDuration() {
        return 99999;
    }

    /**
     * Config block threshold (in millis), dispatch over this duration is regarded as a BLOCK. You may set it from performance of device.
     * 
     * @return threshold in mills
     */
    public int getConfigBlockThreshold() {
        return 1000;
    }

    /**
     * If need notification and block ui
     * 
     * @return true if need, else if not need.
     */
    public boolean isNeedDisplay() {
        return true;
    }

    /**
     * Path to save log, like "/blockcanary/log"
     * 
     * @return path of log files
     */
    @Override
    public String getLogPath() {
        return "/blockcanary";
    }

    /**
     * Zip log file
     * 
     * @param src files before compress
     * @param dest files compressed
     * @return true if compression is successful
     */
    @Override
    public boolean zipLogFile(File[] src, File dest) {
        return false;
    }

    /**
     * Upload log file
     * 
     * @param zippedFile zipped file
     */
    @Override
    public void uploadLogFile(File zippedFile) {
        throw new UnsupportedOperationException();
    }

    /**
     * Config string prefix to determine how to fold stack
     * 
     * @return string prefix, null if use process name.
     */
    @Override
    public String getStackFoldPrefix() {
        return null;
    }

    /**
     * Thread stack dump interval, use when block happens, BlockCanary will dump on main thread stack according to current sample cycle.
     * <p>
     * PS: Because the implementation mechanism of Looper, real dump interval would be longer than the period specified here (longer if cpu is busier)
     * </p>
     * 
     * @return dump interval(in millis)
     */
    @Override
    public int getConfigDumpIntervalMillis() {
        return getConfigBlockThreshold();
    }
}
