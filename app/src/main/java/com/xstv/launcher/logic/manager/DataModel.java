package com.xstv.launcher.logic.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.xstv.launcher.R;
import com.xstv.launcher.logic.controller.DataControl;
import com.xstv.launcher.logic.controller.IUICallback;
import com.xstv.launcher.provider.db.ScreenDBHelper;
import com.xstv.launcher.provider.db.ScreenInfo;
import com.xstv.launcher.ui.LauncherApplication;
import com.xstv.launcher.util.PreferencesUtils;
import com.xstv.launcher.util.StringUtils;
import com.xstv.library.base.LetvLog;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DataModel extends BroadcastReceiver {

    public static final String TAG = "DataModel";
    public static final String DESKTOP_LOCK_URI = "content://com.stv.helper.DesktopLockProvider/desktoplock";
    public static final String DEBUG_DERCRIBE = "debug_describe";
    public static final String DEBUG_PLUGIN_PREFIX = "com.stv.plugin.test";
    private static final String ACTION_CHECK_AD_UPDATE = "com.stv.launcher.model.ACTION_CHECK_AD_UPDATE";
    private static final String ACTION_CHECK_PLUGIN_UPDATE = "com.stv.launcher.model.ACTION_CHECK_PLUGIN_UPDATE";
    private static final String ACTION_APP_BADGE_MESSAGE_UPDATE = "android.intent.action.LETV_BADGE_MESSAGE.UPDATE";
    private static final String ACTION_APP_DESKTOP_MSG = "android.intent.action.LETV_APP_DESKTOP_MSG";

    // 60 minutes
    private static final long AD_UPDATE_INTERVAL = 1000 * 60 * 60;
    // 3 hours
    private static final long PLUGIN_UPDATE_INTERVAL = 1000 * 60 * 60 * 3;

//    private final HandlerThread sWorkerThread;
//    private final Handler sWorker;

    private static DataModel INSTANCE = new DataModel();

    protected int mPreviousConfigMcc;
    //    private DeferredHandler mHandler = new DeferredHandler();
    private DataControl mDataControl;
    private Context mContext;
    private ScreenDBHelper mScreenDBHelper;
    private ArrayList<ScreenInfo> mLoadList = new ArrayList<ScreenInfo>();
    private ArrayList<ScreenInfo> mXMLPluginList = new ArrayList<ScreenInfo>();

    private DataModel() {
        mContext = LauncherApplication.INSTANCE;
        if (mContext == null) {
            throw new IllegalStateException(" Application has not create ! ");
        }
        if (INSTANCE != null) {
            throw new IllegalStateException(" Has already instantiated !");
        }
//        sWorkerThread = new HandlerThread("launcher-loader" + LauncherConfig.THREAD_NAME);
//        sWorkerThread.start();
//        sWorker = new Handler(sWorkerThread.getLooper());

        /** register intent receivers */
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addAction(ACTION_APP_BADGE_MESSAGE_UPDATE);
        filter.addAction(ACTION_APP_DESKTOP_MSG);
        mContext.registerReceiver(this, filter);

        /** add upgrade listener */
        mDataControl = new DataControl();
        mDataControl.setDataModel(this);

        final Resources res = mContext.getResources();
        Configuration config = res.getConfiguration();
        mPreviousConfigMcc = config.mcc;

        mScreenDBHelper = ScreenDBHelper.getInstance();

        sendUpdateBroadcastRepeat();

    }

    public static DataModel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DataModel();
        }
        return INSTANCE;
    }

    public static ArrayList<ScreenInfo> loadScreenInfoFromXML(Context context, int xmlID) {
        ArrayList<ScreenInfo> fromConfigXml = null;
        XmlResourceParser parser = null;
        try {
            parser = context.getResources().getXml(xmlID);
            if (parser == null) {
                return null;
            }
            fromConfigXml = new ArrayList<ScreenInfo>();

            /** begin parser */
            int eventType = parser.getEventType();
            ScreenInfo screenInfo = null;
            String blank = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        String tag = parser.getName();
                        if (tag.equals("screen")) {
                            screenInfo = new ScreenInfo();
                        } else if (tag.equals("sortable")) {
                            String sortable = parser.nextText();
                            if (screenInfo != null) {
                                boolean sort = true;
                                if (!TextUtils.isEmpty(sortable) && sortable.equals("0")) {
                                    sort = false;
                                }
                                screenInfo.setSortable(sort);
                            }
                        } else if (tag.equals("removable")) {
                            String removable = parser.nextText();
                            if (screenInfo != null) {
                                boolean remove = true;
                                if (!TextUtils.isEmpty(removable) && removable.equals("0")) {
                                    remove = false;
                                }
                                screenInfo.setRemovable(remove);
                            }
                        } else if (tag.equals("pluginId")) {
                            String id = parser.nextText();
                            if (!TextUtils.isEmpty(id)) {
                                int pluginId = Integer.valueOf(id);
                                if (screenInfo != null) {
                                    screenInfo.setPluginId(pluginId);
                                }
                            }
                        } else if (tag.equals("tag")) {
                            String screenTag = parser.nextText();
                            if (screenInfo != null) {
                                screenInfo.setTag(screenTag);
                            }
                            screenInfo.setPackageName(screenTag);
                        } else if (tag.equals("fileName")) {
                            String fileName = parser.nextText();
                            if (screenInfo != null) {
                                screenInfo.setFileName(fileName + blank);
                            }
                        } else if (tag.equals("versionCode")) {
                            String versionCode = parser.nextText();
                            if (screenInfo != null) {
                                screenInfo.setVersionCode(versionCode + blank);
                            }
                        } else if (tag.equals("versionName")) {
                            String versionName = parser.nextText();
                            if (screenInfo != null) {
                                screenInfo.setVersionName(versionName + blank);
                            }
                        } else if (tag.equals("packageName")) {
                            String packageName = parser.nextText();
                            if (screenInfo != null) {
                                screenInfo.setPackageName(packageName + blank);
                            }
                        } else if (tag.equals("notSupport")) {
                            String notSupport = parser.nextText();
                            if (screenInfo != null) {
                                screenInfo.setNotSupport(notSupport);
                            }
                        } else if (tag.equals("pluginType")) {
                            String pluginType = parser.nextText();
                            if (screenInfo != null) {
                                screenInfo.setPluginType(pluginType + blank);
                            }
                        } else if (tag.equals("order")) {
                            String or = parser.nextText();
                            if (!TextUtils.isEmpty(or)) {
                                int order = Integer.valueOf(or);
                                if (screenInfo != null) {
                                    screenInfo.setScreenOrder(order);
                                }
                            }
                        } else if (tag.equals("showOnTab")) {
                            String showOnTab = parser.nextText();
                            if (screenInfo != null) {
                                boolean show = false;
                                if (!TextUtils.isEmpty(showOnTab) && showOnTab.equals("1")) {
                                    show = true;
                                }
                                screenInfo.setShowOnTab(show);
                            }
                        } else if (tag.equals("local")) {
                            String local = parser.nextText();
                            if (screenInfo != null) {
                                boolean localed = false;
                                if (!TextUtils.isEmpty(local) && local.equals("1")) {
                                    localed = true;
                                }
                                screenInfo.setLocal(localed);
                            }
                        } else if (tag.equals("forceUpdate")) {
                            String forceUpdate = parser.nextText();
                            if (screenInfo != null) {
                                boolean force = false;
                                if (!TextUtils.isEmpty(forceUpdate) && forceUpdate.equals("1")) {
                                    force = true;
                                }
                                screenInfo.setForceUpdateFromXml(force);
                            }
                        } else if (tag.equals("md5")) {
                            String md5 = parser.nextText();
                            if (screenInfo != null) {
                                screenInfo.setMd5(md5);
                            }
                        } else if (tag.equals("name")) {
                            String name = parser.nextText();
                            if (screenInfo != null) {
                                screenInfo.setName(name);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (screenInfo != null) {
                            String endTag = parser.getName();
                            if (endTag.equals("screen")) {
                                screenInfo.setPluginState(ScreenInfo.PLUGIN_STATE_AVAILABLE);
                                fromConfigXml.add(screenInfo);
                            }
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            LetvLog.d(TAG, " catch error, loadScreenInfoFromXML" + e);
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
        return fromConfigXml;
    }

    public void initPluginFramework() {
        startLoader(true);
    }

    public void setCallback(IUICallback callbacks) {
        if (callbacks instanceof IUICallback) {
            if (mDataControl != null) {
                LetvLog.d(TAG, " setCallback ");
                mDataControl.setUICallback(callbacks);
                notifyUICallback();
            }
        }
    }

    private void startLoader(boolean isLaunching) {
        LetvLog.d(TAG, "startLoader isLaunching=" + isLaunching);
        /* check crash desk info */
        loadWorkspace();
    }

    /**
     * Runs the specified runnable immediately if called from the main thread, otherwise it is posted on the main thread handler.
     */
//    private void runOnMainThread(Runnable r, int type) {
//        if (sWorkerThread.getThreadId() == Process.myTid()) {
//            mHandler.post(r);
//        } else {
//            r.run();
//        }
//    }

    /**
     * Only update single info#isNew
     *
     * @param isNew
     */
    public void updateScreenInfoIsNew(String pkgName, boolean isNew) {
        LetvLog.d(TAG, " updateScreenInfoIsNew pkgName " + pkgName);
        ArrayList<ScreenInfo> screenInfos = mScreenDBHelper.getAllUsedPlugin();
        for (ScreenInfo screen : screenInfos) {
            if (screen.getPackageName().equals(pkgName)) {
                screen.setIsNew(isNew);
                mScreenDBHelper.update(screen);
                break;
            }
        }
    }

    /**
     * update Database and cache
     *
     * @param updateList full list of ScreenInfo except "Signal" :contain both on tab and off tab info
     */
    public void updateScreenInfo(ArrayList<ScreenInfo> updateList) {
        if (updateList == null) {
            return;
        }
        ArrayList<ScreenInfo> beforeUpdate = getShowOnTabList();
        for (ScreenInfo screenInfo : updateList) {
            //if screen show on table ,it must using.
            if (screenInfo.getShowOnTab()) {
                screenInfo.setHasUsed(true);
            }
        }
        mScreenDBHelper.updateInTx(updateList);
        LetvLog.d(TAG, " updateScreenInfo updateList ");
        printArray(updateList);

        //如果tab 列表发生变化，已下线插件上线的时候要指定上线位置
        ArrayList<ScreenInfo> offLines = mScreenDBHelper.getOffLineStateScreenList();
        if (offLines.size() > 0) {
            boolean tableHasChange = false;
            ArrayList<ScreenInfo> afterUpdate = getShowOnTabList();
            int size1 = beforeUpdate.size();
            int size2 = afterUpdate.size();
            if (size1 == size2) {
                for (int i = 0; i < size1; i++) {
                    ScreenInfo before = beforeUpdate.get(i);
                    ScreenInfo after = afterUpdate.get(i);
                    if (before.getPackageName().equals(after.getPackageName())) {
                        continue;
                    } else {
                        LetvLog.i(TAG, before + " tab has changed !!!" + after);
                        tableHasChange = true;
                        break;
                    }
                }
            } else {
                tableHasChange = true;
            }

            if (tableHasChange) {
                LetvLog.i(TAG, " tab has changed !!!");
                for (ScreenInfo screenInfo : offLines) {
                    screenInfo.setOfflineShot(ScreenInfo.OFFLINE_STATE_HASCHANGE);
                }
                LetvLog.d(TAG, " updateScreenInfo offLines");
                printArray(offLines);
                mScreenDBHelper.updateInTx(offLines);
            }
        }
    }

    public void setPluginOffLine(ArrayList<ScreenInfo> offLineList) {
        if (offLineList == null) {
            return;
        }
        mScreenDBHelper.updateInTx(offLineList);
        LetvLog.d(TAG, " setPluginOffLine offLineList ");
        printArray(offLineList);
    }

    private void printArray(List<ScreenInfo> list) {
        LetvLog.i(TAG, "---------printArray begin--------");
        for (ScreenInfo screen : list) {
            LetvLog.i(TAG, "" + screen);
        }
        LetvLog.i(TAG, "---------printArray end--------");
    }

    public ArrayList<ScreenInfo> getShowOnTabList() {
        ArrayList<ScreenInfo> showList = mScreenDBHelper.getShowOnTabList();
        LetvLog.i(TAG, "-------getShowOnTabList --------");
        printArray(showList);
        return showList;
    }

    public void sendUpdateBroadcastRepeat() {
        LetvLog.i(TAG, " sendUpdateBroadcastRepeat ");
        Context context = mContext;
        if (context == null) {
            LetvLog.w(TAG, " sendUpdateBroadcastRepeat error context is null ");
            return;
        }
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // ad
        IntentFilter adFilter = new IntentFilter();
        adFilter.addAction(ACTION_CHECK_AD_UPDATE);
        context.registerReceiver(this, adFilter);
        Intent adIntent = new Intent();
        adIntent.setAction(ACTION_CHECK_AD_UPDATE);
        PendingIntent adPendingIntent = PendingIntent.getBroadcast(context, 0, adIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + AD_UPDATE_INTERVAL, AD_UPDATE_INTERVAL, adPendingIntent);

        // plugin
        IntentFilter pluginFilter = new IntentFilter();
        pluginFilter.addAction(ACTION_CHECK_PLUGIN_UPDATE);
        context.registerReceiver(this, pluginFilter);
        Intent pluginIntent = new Intent();
        pluginIntent.setAction(ACTION_CHECK_PLUGIN_UPDATE);
        PendingIntent pluginPendingIntent = PendingIntent.getBroadcast(context, 0, pluginIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + PLUGIN_UPDATE_INTERVAL, PLUGIN_UPDATE_INTERVAL, pluginPendingIntent);
    }

    public void cancelUpdateBroadcast() {
        LetvLog.i(TAG, " cancelUpdateBroadcast ");
        Context context = mContext;
        if (context == null) {
            LetvLog.w(TAG, " cancelUpdateBroadcast error! context is null ");
            return;
        }
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent adIntent = new Intent();
        adIntent.setAction(ACTION_CHECK_AD_UPDATE);
        PendingIntent adPendingIntent = PendingIntent.getBroadcast(context, 0, adIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(adPendingIntent);

        Intent pluginIntent = new Intent();
        pluginIntent.setAction(ACTION_CHECK_PLUGIN_UPDATE);
        PendingIntent pluginPendingIntent = PendingIntent.getBroadcast(context, 0, pluginIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(pluginPendingIntent);

        context.unregisterReceiver(this);
    }

    /**
     * Get show on tab screen（Used by user now） package name.
     *
     * @return
     */
    public ArrayList<String> getShowOnTabPackageName() {
        return mScreenDBHelper.getShowOnTabPackageName();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        LetvLog.i(TAG, " onReceive action = " + action);
        if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
//            forceReload();
        } else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
            Configuration currentConfig = context.getResources().getConfiguration();
            if (mPreviousConfigMcc != currentConfig.mcc) {
//                forceReload();
            }
            mPreviousConfigMcc = currentConfig.mcc;
        } else if (ACTION_CHECK_AD_UPDATE.equals(action)) {
//            getAdvertiseData(false);
        } else if (ACTION_CHECK_PLUGIN_UPDATE.equals(action)) {
        }
    }

    /**
     * Update ScreenInfo state to db
     *
     * @param packageNameList
     * @param pluginState     {@link ScreenInfo#PLUGIN_STATE_CRASH ScreenInfo#PLUGIN_STATE_AVAILABLE
     *                        ScreenInfo#PLUGIN_STATE_UNAVAILABLE .etc..}
     */
    public void updatePluginState(List<String> packageNameList, String pluginState) {
        if (packageNameList == null || pluginState == null) {
            LetvLog.d(TAG, " updatePluginState packageNameList =  " + packageNameList + ", pluginState = " + pluginState);
            return;
        }
        mScreenDBHelper.updatePluginState(packageNameList, pluginState);
    }

    /**
     * Update ScreenInfo state to db
     *
     * @param packageName
     * @param pluginState {@link ScreenInfo#PLUGIN_STATE_CRASH ScreenInfo#PLUGIN_STATE_AVAILABLE
     *                    ScreenInfo#PLUGIN_STATE_UNAVAILABLE .etc..}
     */
    public void updatePluginState(String packageName, String pluginState) {
        if (packageName == null || pluginState == null) {
            LetvLog.d(TAG, " updatePluginState packageName =  " + packageName + ", pluginState = " + pluginState);
            return;
        }
        mScreenDBHelper.updatePluginState(packageName, pluginState);
    }

    /**
     * 按照插件包名查找插件信息，可以指定包含已经下线的插件。
     *
     * @param packageName
     * @param includeOffline true: 查找包含下线插件。 false: 只在可用插件中查找
     * @return
     */
    public ScreenInfo getScreenByPackageName(String packageName, boolean includeOffline) {
        ScreenInfo s = null;
        if (packageName != null) {
            ArrayList<ScreenInfo> allUsedPlugin = mScreenDBHelper.getAllUsedPlugin();
            for (ScreenInfo usedScreen : allUsedPlugin) {
                if (packageName.equals(usedScreen.getPackageName())) {
                    s = usedScreen;
                }
            }
            if (s == null && includeOffline) {
                ArrayList<ScreenInfo> screenInfos = mScreenDBHelper.getScreenByPackageName(packageName);
                if (screenInfos != null && screenInfos.size() > 0) {
                    s = screenInfos.get(0);
                }
            }
        }
        return s;
    }

    /**
     * @return on Tab ScreenInfo数据
     */
    public ArrayList<ScreenInfo> getShowScreens() {
        return mScreenDBHelper.getShowOnTabList();
    }

    /**
     * @return 全量ScreenInfo数据
     */
    public ArrayList<ScreenInfo> getAllScreens() {
        return mScreenDBHelper.getAllUsedPlugin();
    }

    private void notifyUICallback() {
        final long t = SystemClock.uptimeMillis();
        if (mDataControl == null) {
            LetvLog.w(TAG, " notifyUICallback no data control ");
            return;
        }
        mDataControl.startLoad();
        ArrayList<ScreenInfo> screenInfos = getShowOnTabList();
        if (screenInfos.size() > 0) {
            mDataControl.onLoad(screenInfos);
        }
        mDataControl.finishLoad();
        LetvLog.i(TAG, "notifyUICallback use " + (SystemClock.uptimeMillis() - t) + "ms");
    }

    private void loadWorkspace() {
        final long t = SystemClock.uptimeMillis();

        verifyPlugin();

        LetvLog.i(TAG, "loadWorkspace verifyPlugin in " + (SystemClock.uptimeMillis() - t) + "ms");
        LetvLog.i(TAG, " loadWorkspace mLoadList ");
        printArray(mLoadList);

        final Map<String, String> otaMap = new ArrayMap<String, String>(mXMLPluginList.size());
        for (int i = 0; i < mXMLPluginList.size(); i++) {
            ScreenInfo s = mXMLPluginList.get(i);
            if (s != null) {
                otaMap.put(s.getPackageName(), s.getFileName());
            }
        }

        String mainPage = "com.stv.module.app";
        Collections.sort(mLoadList, new ScreenComparator(mainPage));

        mLoadList.clear();
    }

    /**
     * 1.remove invalid plugin in db.<br>
     * 2.get update plugin.<br>
     * 3.get load list.<br>
     */
    private void verifyPlugin() {
        LetvLog.i(TAG, "--verifyPlugin begin --------");
        // check rom version,if version downgraded ,clear plugin from db.
        String version = "0";//LetvManagerUtil.getLetvReleaseVersion();
        String oldVersion = PreferencesUtils.getString(mContext, "RELEASE_VERSION", version);
        LetvLog.i(TAG, " verifyPlugin oldVersion = " + oldVersion + " version = " + version);
        if (null != version && version.compareTo(oldVersion) < 0) {
            LetvLog.i(TAG, version + " < " + oldVersion + ", so clear plugin from db");
            mScreenDBHelper.deleteAll();
        }
        PreferencesUtils.putString(mContext, "RELEASE_VERSION", version);

        //1.load from db
        ArrayList<ScreenInfo> fromDB = mScreenDBHelper.getAllUsedPlugin();
        LetvLog.i(TAG, "-------fromDB used plugin list --------");
        printArray(fromDB);

        boolean versionHasChange = true;//(null != version && version.equals(oldVersion));
        if (versionHasChange) {
            //2. load from xml
            ArrayList<ScreenInfo> fromConfigXML = loadScreenInfoFromXML(mContext, R.xml.default_screens);
            mXMLPluginList.clear();
            mXMLPluginList.addAll(fromConfigXML);
            LetvLog.i(TAG, "-------fromConfigXML list --------");
            printArray(fromConfigXML);
            //3. if db is null
            if (fromDB == null || fromDB.size() == 0) {
                LetvLog.i(TAG, "verifyPlugin, db is null");
                // save to DB
                mScreenDBHelper.inserOrReplaceInTx(fromConfigXML);
                mLoadList.clear();
                mLoadList.addAll(mScreenDBHelper.getAllUsedPlugin());
                mScreenDBHelper.initPluginState();
                return;
            }
            compareWithXml(fromDB, fromConfigXML);
            //5.delete invalid plugin in db
            removeInvalidScreen(fromDB, fromConfigXML);
        }

        // 9.get load plugin list
        ArrayList<ScreenInfo> loadList = mScreenDBHelper.getShowOnTabList();
        if (loadList != null && loadList.size() > 0) {
            LetvLog.i(TAG, "-------getLoadList --------");
            printArray(loadList);
            mLoadList.clear();
            mLoadList.addAll(loadList);
        }
        LetvLog.i(TAG, "--verifyPlugin end --------");
    }

    private boolean containInXml(ArrayList<ScreenInfo> xmlList, ScreenInfo screenInDB) {
        for (ScreenInfo screenOfXml : xmlList) {
            if (screenOfXml != null && screenOfXml.equals(screenInDB)) {
                return true;
            }
        }
        return false;
    }

    /**
     * delete invalid plugin(local but not in xml)
     * 删除是本地标识但没有在xml文件中配置的插件
     *
     * @param fromDBList
     * @param fromXmlList
     */
    private void removeInvalidScreen(ArrayList<ScreenInfo> fromDBList, ArrayList<ScreenInfo> fromXmlList) {
        LetvLog.i(TAG, "--removeInvalidScreen begin---");
        //remove local but not in xml
        ArrayList<ScreenInfo> invalidList = new ArrayList<ScreenInfo>(5);
        for (ScreenInfo dbScreen : fromDBList) {
            //Only check local plugin when update system
            boolean isInvalid = dbScreen.getLocal() && !containInXml(fromXmlList, dbScreen);
            if (isInvalid) {
                invalidList.add(dbScreen);
                LetvLog.i(TAG, dbScreen + " isInvalid, remove from db");
            }
        }
        if (invalidList.size() > 0) {
            fromDBList.removeAll(invalidList);
            fromXmlList.removeAll(invalidList);
            mScreenDBHelper.deleteInTx(invalidList);
            LetvLog.i(TAG, "-------invalidList --------");
            printArray(invalidList);
        }
        LetvLog.i(TAG, "--removeInvalidScreen end---");
    }

    private void compareWithXml(ArrayList<ScreenInfo> fromDBList, ArrayList<ScreenInfo> fromXmlList) {
        LetvLog.i(TAG, "--compareWithXml begin --------");
        ArrayList<ScreenInfo> updateList = new ArrayList<ScreenInfo>(10);
        ArrayList<ScreenInfo> insertList = new ArrayList<ScreenInfo>(10);
        for (ScreenInfo screenOfXML : fromXmlList) {
            String pluginIdFromXML = StringUtils.removeBlankAndN(screenOfXML.getPackageName());
            boolean hasInDB = false;
            if (!StringUtils.isEmpty(pluginIdFromXML)) {
                for (ScreenInfo screenOfDB : fromDBList) {
                    // xml plugin find has in db
                    if (screenOfXML.equals(screenOfDB)) {
                        hasInDB = true;
                        String versionInXMl = "0";
                        if (screenOfXML.getVersionCode() != null) {
                            versionInXMl = StringUtils.removeBlankAndN(screenOfXML.getVersionCode());
                        }
                        String versionInDB = "0";
                        if (screenOfDB.getVersionCode() != null) {
                            versionInDB = StringUtils.removeBlankAndN(screenOfDB.getVersionCode());
                        }
                        if (versionInXMl.compareTo(versionInDB) > 0) {
                            LetvLog.i(TAG, screenOfXML.getPackageName() + " compareWithXml versionInXMl =" + versionInXMl + " versionInDB = "
                                    + versionInDB);
                            screenOfDB.updateInfoFromXml(screenOfXML);
                            screenOfDB.setIsNew(true);
                            screenOfDB.setPluginState(ScreenInfo.PLUGIN_STATE_AVAILABLE);
                            screenOfDB.setScreenTag(null);
                            updateList.add(screenOfDB);
                        }
                        break;
                    }
                }
                //if not find in db ,insert to db
                if (!hasInDB) {
                    ArrayList<ScreenInfo> OffLineList = ScreenDBHelper.getInstance().getOffLineStateScreenList();
                    int index = OffLineList.indexOf(screenOfXML);
                    if (index == -1) {
                        //read from xml plugin is local default
                        screenOfXML.setIsNew(true);
                        screenOfXML.setPluginState(ScreenInfo.PLUGIN_STATE_AVAILABLE);
                        insertList.add(screenOfXML);
                    }
                }
            }
        }

        LetvLog.i(TAG, "---- updateList--------");
        printArray(updateList);
        if (updateList.size() > 0) {
            mScreenDBHelper.updateInTx(updateList);
        }

        LetvLog.i(TAG, "---- insertList --------");
        printArray(insertList);
        if (insertList.size() > 0) {
            mScreenDBHelper.inserOrReplaceInTx(insertList);
        }
        LetvLog.i(TAG, "--compareWithXml end --------");
    }

    static class ScreenComparator implements Comparator {
        private String mainScreen;

        ScreenComparator(String targetScreen) {
            mainScreen = targetScreen;
        }

        public int compare(Object o1, Object o2) {
            ScreenInfo screenInfo = (ScreenInfo) o1;
            if (screenInfo.getPackageName().equalsIgnoreCase(mainScreen)) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
