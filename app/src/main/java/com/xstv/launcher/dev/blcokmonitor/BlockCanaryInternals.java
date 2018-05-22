
package com.xstv.launcher.dev.blcokmonitor;

import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;

public final class BlockCanaryInternals {

    private BlockCanaryInternals() {
        throw new AssertionError();
    }

    public static String getPath() {
        String state = Environment.getExternalStorageState();
        String logPath = BlockCanaryCore.getContext() == null ? "" : BlockCanaryCore.getContext().getLogPath();

        if (Environment.MEDIA_MOUNTED.equals(state) && Environment.getExternalStorageDirectory().canWrite()) {
            return Environment.getExternalStorageDirectory().getPath() + logPath;
        }
        return Environment.getDataDirectory().getAbsolutePath() + BlockCanaryCore.getContext().getLogPath();
    }

    public static File detectedBlockDirectory() {
        File directory = new File(getPath());
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    public static File[] getLogFiles() {
        File f = BlockCanaryInternals.detectedBlockDirectory();
        if (f.exists() && f.isDirectory()) {
            return f.listFiles(new BlockLogFileFilter());
        }
        return null;
    }

    static class BlockLogFileFilter implements FilenameFilter {

        private String TYPE = ".txt";

        public BlockLogFileFilter() {

        }

        @Override
        public boolean accept(File dir, String filename) {
            return filename.endsWith(TYPE);
        }
    }
}
