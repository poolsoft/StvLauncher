package com.xstv.desktop.app.db;

import android.database.sqlite.SQLiteDatabase;

import com.xstv.library.base.LetvLog;

/**
 * Created by zhangguanhua on 17-12-19.
 */

public class ItemInfoMigrationV2 implements Migration<SQLiteDatabase> {
    @Override
    public void migrate(SQLiteDatabase db) {

        try{
            String sql = "ALTER TABLE SHORTCUT_INFO RENAME TO ITEM_INFO";
            db.execSQL(sql);
        }catch (Exception ex){
            LetvLog.d("greenDAO", "migrate error=" + ex);
        }
    }
}
