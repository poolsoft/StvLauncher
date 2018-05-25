package com.xstv.launcher.logic.manager;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.xstv.base.LetvLog;
import com.xstv.launcher.logic.model.Weather;

public class WeatherManager {
    private static final String TAG = "WeatherManager";

    public static final String PACKAGE = "sina.mobile.tianqitongstv";
    public static final String URL = "content://com.sina.tianqitong.StvWeatherInfoProvider/stv_weather";

    public static Weather getWeatherInfo(Context context) {
        LetvLog.d(TAG, " getWeatherInfo");
        return queryWeatherInfo(context, Uri.parse(URL));
    }

    private static Weather queryWeatherInfo(Context context, Uri uri) {
        Weather weather = null;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String city = cursor.getString(cursor.getColumnIndex("city")); // 城市名
                int unit = cursor.getInt(cursor.getColumnIndex("temp_unit")); // 单位
                String temperature = cursor.getString(cursor.getColumnIndex("temperature")); // 温度值
                String weatherDes = cursor.getString(cursor.getColumnIndex("weather")); // 天气描述
                String image_icon = cursor.getString(cursor.getColumnIndex("image_icon")); // 图片对应的资源ID
                weather = new Weather(city, temperature, weatherDes, unit, Integer.parseInt(image_icon));
            }
            LetvLog.d(TAG, " getWeatherInfo = " + weather);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return weather;
    }

    public static Context getWeatherContext(Context context) {
        Context weatherContext = null;
        try {
            weatherContext = context.createPackageContext(PACKAGE, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return weatherContext;
    }
}