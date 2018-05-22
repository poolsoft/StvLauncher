
package com.xstv.launcher.dev.blcokmonitor;

import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

class UploadMonitorLog {

    private static final String TAG = "UploadMonitorLog";

    private UploadMonitorLog() {
        throw new InstantiationError("Must not instantiate this class");
    }

    private static File zipFile() {
        String timeString = Long.toString(System.currentTimeMillis());
        try {
            SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            timeString = sdFormat.format(new Date());
        } catch (Throwable e) {
            Log.e(TAG, "zipFile: ", e);
        }

        File zippedFile = LogWriter.generateTempZipFile("Monitor_looper_" + timeString);
        BlockCanaryCore.getContext().zipLogFile(BlockCanaryInternals.getLogFiles(), zippedFile);
        LogWriter.deleteLogFiles();
        return zippedFile;
    }

    public static void forceZipLogAndUpload() {
        HandlerThread.getWriteLogFileThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                final File file = zipFile();
                if (file.exists()) {
                    BlockCanaryCore.getContext().uploadLogFile(file);
                }
            }
        });
    }
}
