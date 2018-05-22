
package com.xstv.launcher.dev.blcokmonitor;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;

public class LogWriter {

    private static final String TAG = "LogWriter";

    private static final Object SAVE_DELETE_LOCK = new Object();
    private static final long OBSOLETE_DURATION = 2 * 24 * 3600 * 1000L;

    private LogWriter() {
        throw new InstantiationError("Must not instantiate this class");
    }

    /**
     * Save log to file
     *
     * @param str block log string
     * @return log file path
     */
    public static String saveLooperLog(String str) {
        String path;
        synchronized (SAVE_DELETE_LOCK) {
            path = saveLogToSDCard("block", str);
        }
        return path;
    }

    /**
     * Delete obsolete log files，see also {@code OBSOLETE_DURATION}
     */
    public static void cleanOldFiles() {
        HandlerThread.getWriteLogFileThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                File[] f = BlockCanaryInternals.getLogFiles();
                if (f != null && f.length > 0) {
                    synchronized (SAVE_DELETE_LOCK) {
                        for (File aF : f) {
                            if (now - aF.lastModified() > OBSOLETE_DURATION) {
                                aF.delete();
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * Delete all log files.
     */
    public static void deleteLogFiles() {
        synchronized (SAVE_DELETE_LOCK) {
            try {
                File[] files = BlockCanaryInternals.getLogFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        file.delete();
                    }
                }
            } catch (Throwable e) {
                Log.e(TAG, "deleteLogFiles: ", e);
            }
        }
    }

    private static String saveLogToSDCard(String logFileName, String str) {
        String path = "";
        BufferedWriter writer = null;
        FileOutputStream fileOutputStream = null;
        try {
            File file = BlockCanaryInternals.detectedBlockDirectory();
            long time = System.currentTimeMillis();
            SimpleDateFormat FILE_NAME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS");
            SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            path = file.getAbsolutePath() + "/" + logFileName + "-" + FILE_NAME_FORMATTER.format(time) + ".txt";
            fileOutputStream = new FileOutputStream(path, true);
            OutputStreamWriter out = new OutputStreamWriter(fileOutputStream, "UTF-8");

            writer = new BufferedWriter(out);
            writer.write("\r\n**********************\r\n");
            writer.write(TIME_FORMATTER.format(time) + "(write log time)");
            writer.write("\r\n");
            writer.write("\r\n");
            writer.write(str);
            writer.write("\r\n");
            writer.flush();
            writer.close();
            writer = null;
        } catch (Throwable t) {
            Log.e(TAG, "saveLogToSDCard: ", t);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                    writer = null;
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "saveLogToSDCard: ", e);
            }
        }
        return path;
    }

    public static File generateTempZipFile(String filename) {
        return new File(BlockCanaryInternals.getPath() + "/" + filename + ".log.zip");
    }
}
