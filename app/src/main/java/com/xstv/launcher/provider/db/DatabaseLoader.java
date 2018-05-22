
package com.xstv.launcher.provider.db;

import android.database.sqlite.SQLiteDatabase;

import com.xstv.launcher.ui.LauncherApplication;


public class DatabaseLoader {

    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    private SQLiteDatabase mSqLiteDatabase;
    public static final String DATABASES_NAME = "launcher_new.db";

    private static DatabaseLoader sInstance;

    private DatabaseLoader() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(LauncherApplication.INSTANCE, DATABASES_NAME, null);
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
