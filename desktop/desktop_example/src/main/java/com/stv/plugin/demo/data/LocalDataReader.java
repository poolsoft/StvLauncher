package com.stv.plugin.demo.data;

import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.stv.plugin.demo.DemoApplication;
import com.stv.plugin.demo.data.common.PosterHolder;
import com.xstv.base.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;

class LocalDataReader {

    private Logger mLogger = Logger.getLogger(DemoApplication.PLUGINTAG, "LocalDataReader");

    private static final String FILENAME = "data.json";
    private static final String LOCAL_DIRECTORY_NAME = "DemoPluginJson";
    private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("yyyyMMddHHmm");
    /**
     * Assets数据有修改，版本号需要增加
     */
    private static final long ASSETS_FILE_VERSION = 201608011226L;
    private Gson mGson = new Gson();


    PosterHolder verifyDefaultData() {
        PosterHolder defaultHolder;

        File sdcardFile = new File(getPath() + "/" + FILENAME);
        if (!sdcardFile.exists() || sdcardFile.lastModified() < ASSETS_FILE_VERSION) {
            defaultHolder = loadAssetsDefault();
            updateLocalDataCache(defaultHolder);
        } else {
            defaultHolder = loadSDCardCache(sdcardFile);
        }
        return defaultHolder;
    }

    /**
     * 从Assets中加载默认数据
     */
    private PosterHolder loadAssetsDefault() {
        AssetManager am = DemoApplication.sWidgetApplicationContext.getResources().getAssets();
        BufferedReader reader = null;
        String jsonStr = "";
        try {
            InputStream is = am.open("data_" + ASSETS_FILE_VERSION + ".json");
            InputStreamReader inputStreamReader = new InputStreamReader(is, "UTF-8");
            reader = new BufferedReader(inputStreamReader);
            String segment;
            while ((segment = reader.readLine()) != null) {
                jsonStr += segment;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        PosterHolder holder = null;
        if (!TextUtils.isEmpty(jsonStr)) {
            holder = new PosterHolder();
            try {
                holder = mGson.fromJson(jsonStr, PosterHolder.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mLogger.d("loadAssetsDefault " + holder);
        return holder;
    }

    /**
     * 从SD卡中加载缓存数据
     */
    private PosterHolder loadSDCardCache(File file) {
        BufferedReader reader = null;
        String jsonStr = "";
        try {
            InputStream is = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(is, "UTF-8");
            reader = new BufferedReader(inputStreamReader);
            String segment;
            while ((segment = reader.readLine()) != null) {
                jsonStr += segment;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        PosterHolder holder = null;
        if (!TextUtils.isEmpty(jsonStr)) {
            holder = new PosterHolder();
            try {
                holder = mGson.fromJson(jsonStr, PosterHolder.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mLogger.d("loadSDCardCache " + holder);
        return holder;
    }

    void updateLocalDataCache(PosterHolder posterHolder) {
        File file_xml = new File(getPath());
        if (file_xml.exists()) {
            File[] files = file_xml.listFiles();
            for (File file : files) {
                file.delete();
            }
        }
        /**
         * 刷新数据时间戳
         */
        posterHolder.timestamp = Long.valueOf(TIME_FORMATTER.format(System.currentTimeMillis()));
        String jsonString = mGson.toJson(posterHolder);
        mLogger.d("updateLocalDataCache, timestamp=" + posterHolder.timestamp);
        saveJsonToSDCard(jsonString);
    }

    private void saveJsonToSDCard(String jsonString) {
        BufferedWriter writer = null;
        try {
            File file = detectedBlockDirectory();
            String path = file.getAbsolutePath() + "/" + FILENAME;
            mLogger.d("saveJsonToSDCard file=" + path);
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(path, true), "UTF-8");
            writer = new BufferedWriter(out);
            writer.write(jsonString);
            writer.flush();
            writer.close();
            writer = null;
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                    writer = null;
                }
            } catch (Exception e) {
                mLogger.e("saveLogToSDCard: ", e);
            }
        }
    }

    private File detectedBlockDirectory() {
        File directory = new File(getPath());
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    private String getPath() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) && Environment.getExternalStorageDirectory().canWrite()) {
            // return Environment.getExternalStorageDirectory().getPath() + "/" + LOCAL_DIRECTORY_NAME;
            return "/sdcard/" + LOCAL_DIRECTORY_NAME + "_" + DemoApplication.sWidgetApplicationContext.getPackageName();
        }
        return "/data/data/" + DemoApplication.sWidgetApplicationContext.getPackageName() + "/files/" + LOCAL_DIRECTORY_NAME;
    }
}
