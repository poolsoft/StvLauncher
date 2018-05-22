package com.xstv.desktop.app.db;

import android.database.sqlite.SQLiteDatabase;

import com.xstv.base.LetvLog;

/**
 * Created by zhangguanhua on 17-9-26.
 *
 * v1 -> v2
 *
 *  1.去掉一些冗余字段
 * spanX,spanY,row,column,width,height,description,iconUrl,backgroundResID,apkUrl,downType,downState
 * superscriptType,superscriptCount,multi_language;
 * 2.为添加快捷方式增加一些字段
 * orderTimestamp 常用应用位排序需要的时间戳
 * shortcutIntentUrl 快捷方式intent转换成url
 * shortcutIcon      快捷方式图标
 * shortcutResourseName 资源名字
 */

public class ItemInfoMigrationV1 implements Migration<SQLiteDatabase> {

    @Override
    public void migrate(SQLiteDatabase db) {
        try{
            String createSql = "CREATE TABLE IF NOT EXISTS " + " SHORTCUT_INFO (" + //
                    "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                    "\"INDEX\" INTEGER," + // 1: index
                    "\"TYPE\" INTEGER," + // 2: type
                    "\"TITLE\" TEXT," + // 3: title
                    "\"PACKAGE_NAME\" TEXT," + // 4: packageName
                    "\"CLASS_NAME\" TEXT," + // 5: className
                    "\"FLAGS\" INTEGER," + // 6: flags
                    "\"CONTAINER\" INTEGER," + // 7: container
                    "\"CONTAINER_NAME\" TEXT," + // 8: containerName
                    "\"INSTALL_TIME\" INTEGER," + // 9: installTime
                    "\"COMPONENT_NAME_STR\" TEXT," + // 10: componentNameStr
                    "\"IN_FOLDER_INDEX\" INTEGER," + // 11: inFolderIndex
                    "\"FOLDER_ID\" TEXT," + // 12: folder_id
                    "\"ORDER_TIMESTAMP\" INTEGER," + // 13: orderTimestamp
                    "\"SHORTCUT_INTENT_URL\" TEXT," + // 14: shortcutIntentUrl
                    "\"SHORTCUT_ICON\" TEXT," + // 15: shortcutIcon
                    "\"SHORTCUT_RESOURSE_NAME\" TEXT," + // 16: shortcutResourseName
                    "\"RESERVE1\" TEXT," + // 17: reserve1
                    "\"RESERVE2\" TEXT," + // 18: reserve2
                    "\"RESERVE3\" TEXT);"; // 19: reserve3
            String rename_sql = "ALTER TABLE SHORTCUT_INFO RENAME TO temp";
            String insert_sql = "INSERT INTO SHORTCUT_INFO " +
                    "(_id, [INDEX], TYPE, TITLE, PACKAGE_NAME, CLASS_NAME, FLAGS, CONTAINER, CONTAINER_NAME, INSTALL_TIME, COMPONENT_NAME_STR, " +
                    "IN_FOLDER_INDEX, FOLDER_ID, ORDER_TIMESTAMP) " +
                    " SELECT _id, [INDEX], TYPE, TITLE, PACKAGE_NAME, CLASS_NAME, FLAGS, CONTAINER, CONTAINER_NAME, INSTALL_TIME, COMPONENT_NAME_STR, IN_FOLDER_INDEX, FOLDER_ID, RESERVE1 FROM temp ";
            String drop_sql = "DROP TABLE temp";

            db.execSQL(rename_sql);
            db.execSQL(createSql);
            db.execSQL(insert_sql);
            db.execSQL(drop_sql);
        }catch (Exception ex){
            LetvLog.d("greenDAO", "migrate error=" + ex);
        }
    }
}
