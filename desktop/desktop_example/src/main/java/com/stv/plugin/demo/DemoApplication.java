package com.stv.plugin.demo;

import android.app.Application;
import android.content.Context;

import com.stv.plugin.PreferencesUtils;

public class DemoApplication extends Application {

    public static Context sWidgetApplicationContext;
    public static String PLUGINTAG = "DemoPlugin";

    @Override
    public void onCreate() {
        super.onCreate();
        sWidgetApplicationContext = this;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        sWidgetApplicationContext = null;
    }
}
