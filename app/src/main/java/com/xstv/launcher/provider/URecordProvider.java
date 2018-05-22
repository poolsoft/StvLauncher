package com.xstv.launcher.provider;//package com.stv.launcher.provider;
//
//import com.stv.launcher.util.LetvLog;
//import com.stv.plugin.upgrade.db.DaoMaster;
//import com.stv.plugin.upgrade.db.DaoSession;
//import com.stv.plugin.upgrade.db.UpgradeDBHelper;
//import com.stv.plugin.upgrade.db.UpgradeRecordDao;
//
//import android.content.ContentProvider;
//import android.content.ContentValues;
//import android.content.UriMatcher;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.net.Uri;
//
///**
// * 升级记录
// */
//public class URecordProvider extends ContentProvider {
//    public final String TAG = "URecordProvider";
//
//    public static final UriMatcher URI_MATCHER;
//    private DaoSession mDaoSession;
//    private SQLiteDatabase mSqLiteDatabase;
//
//    static {
//        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
//        URI_MATCHER.addURI(URecord.AUTHORITY, URecord.PATH_SINGLE, URecord.CODE_SINGLE);
//        URI_MATCHER.addURI(URecord.AUTHORITY, URecord.PATH_MULTIPLE, URecord.CODE_MULTIPLE);
//        URI_MATCHER.addURI(URecord.AUTHORITY, URecord.PATH_SINGLE_READY, URecord.CODE_SINGLE_READY_REBOOT);
//        URI_MATCHER.addURI(URecord.AUTHORITY, URecord.PATH_MULTIPLE_READY, URecord.CODE_MULTIPLE_READY_REBOOT);
//    }
//
//    private boolean setDatabases() {
//        LetvLog.d(TAG, "setDatabases");
//        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(getContext(), UpgradeDBHelper.DB_NAME, null);
//        mSqLiteDatabase = helper.getReadableDatabase();
//        DaoMaster daoMaster = new DaoMaster(mSqLiteDatabase);
//        mDaoSession = daoMaster.newSession();
//        return (null != mSqLiteDatabase && null != mDaoSession);
//    }
//
//    @Override
//    public boolean onCreate() {
//        LetvLog.d(TAG, "onCreate");
//        return setDatabases();
//    }
//
//    @Override
//    public int delete(Uri uri, String selection, String[] selectionArgs) {
//        return 0;
//    }
//
//    @Override
//    public String getType(Uri uri) {
//        switch (URI_MATCHER.match(uri)) {
//        case URecord.CODE_SINGLE:
//        case URecord.CODE_SINGLE_READY_REBOOT:
//            return URecord.CONTENT_TYPE_ITEM;
//        case URecord.CODE_MULTIPLE:
//        case URecord.CODE_MULTIPLE_READY_REBOOT:
//            return URecord.CONTENT_TYPE_DIR;
//        default:
//            throw new IllegalArgumentException("unkown uri = " + uri);
//        }
//    }
//
//    @Override
//    public Uri insert(Uri uri, ContentValues values) {
//        return null;
//    }
//
//    @Override
//    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
//        LetvLog.d(TAG, "query uri = " + uri);
//        Cursor cursor = null;
//        switch (URI_MATCHER.match(uri)) {
//        case URecord.CODE_SINGLE:
//            LetvLog.d(TAG, "query single segment = " + uri.getLastPathSegment());
//            cursor = mSqLiteDatabase.query(UpgradeRecordDao.TABLENAME, projection, UpgradeRecordDao.TABLENAME + ".PID = '" + uri.getLastPathSegment() + "'", selectionArgs, null, null, sortOrder);
//            break;
//        case URecord.CODE_SINGLE_READY_REBOOT:
//            LetvLog.d(TAG, "query single ready reboot segment = " + uri.getLastPathSegment());
//            cursor = mSqLiteDatabase.query(UpgradeRecordDao.TABLENAME, projection,
//                    UpgradeRecordDao.TABLENAME + ".PID = '" + uri.getLastPathSegment() + "' and " + UpgradeRecordDao.TABLENAME + ".STATUS = 3" // 下载完成
//                            + " and " + UpgradeRecordDao.TABLENAME + ".STRATEGY = 3" // 开机升级
//                    , selectionArgs, null, null, sortOrder);
//            break;
//        case URecord.CODE_MULTIPLE:
//            LetvLog.d(TAG, "query multiple");
//            cursor = mSqLiteDatabase.query(UpgradeRecordDao.TABLENAME, projection, selection, selectionArgs, null, null, sortOrder);
//            break;
//        case URecord.CODE_MULTIPLE_READY_REBOOT:
//            LetvLog.d(TAG, "query multiple ready reboot");
//            cursor = mSqLiteDatabase.query(UpgradeRecordDao.TABLENAME, projection, UpgradeRecordDao.TABLENAME + ".STATUS = 3" // 下载完成
//                            + " and " + UpgradeRecordDao.TABLENAME + ".STRATEGY = 3" // 开机升级
//                    , selectionArgs, null, null, sortOrder);
//            break;
//        }
//        LetvLog.d(TAG, "cursor = " + cursor);
//        return cursor;
//    }
//
//    @Override
//    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
//        return 0;
//    }
//
//    static class URecord {
//        public static final String AUTHORITY = "com.stv.launcher.provider.urecord";
//        public static final String PATH_SINGLE = "record/*";
//        public static final String PATH_MULTIPLE = "records";
//        public static final String PATH_SINGLE_READY = "ready_record/*";
//        public static final String PATH_MULTIPLE_READY = "ready_records";
//        public static final int CODE_SINGLE = 1;
//        public static final int CODE_MULTIPLE = 2;
//        public static final int CODE_SINGLE_READY_REBOOT = 3;
//        public static final int CODE_MULTIPLE_READY_REBOOT = 4;
//        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.stv.record";
//        public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.stv.records";
//    }
//}
