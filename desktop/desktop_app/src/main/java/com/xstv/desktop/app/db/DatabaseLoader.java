
package com.xstv.desktop.app.db;

import android.database.sqlite.SQLiteDatabase;

import com.xstv.desktop.app.AppPluginActivator;

public class DatabaseLoader {

    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    private SQLiteDatabase mSqLiteDatabase;

    private static DatabaseLoader sInstance;

    private DatabaseLoader() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(AppPluginActivator.getContext(), "plugin_app.db", null);
        mSqLiteDatabase = helper.getWritableDatabase();
        mDaoMaster = new DaoMaster(mSqLiteDatabase);
        mDaoSession = mDaoMaster.newSession();
    }

    public static DatabaseLoader getInstance() {
        synchronized (DatabaseLoader.class) {
            if (sInstance == null) {
                synchronized (DatabaseLoader.class) {
                    sInstance = new DatabaseLoader();
                }
            }
        }
        return sInstance;
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }
}
