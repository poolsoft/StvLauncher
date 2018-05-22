
package com.xstv.desktop.app.model;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import com.xstv.base.LetvLog;
import com.xstv.base.async.Job;
import com.xstv.base.async.JobType;
import com.xstv.base.async.ThreadPool;
import com.xstv.desktop.app.AppPluginActivator;
import com.xstv.desktop.app.R;
import com.xstv.desktop.app.bean.FolderInfo;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.db.ItemInfoDBHelper;
import com.xstv.desktop.app.receiver.InstallShortcutReceiver;
import com.xstv.desktop.app.util.AppLoaderConfig;
import com.xstv.desktop.app.util.LauncherState;
import com.xstv.desktop.app.util.PreferencesUtils;
import com.xstv.desktop.app.util.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppDataModel extends BroadcastReceiver {

    /**
     * 普通应用
     */
    public static final int ITEM_TYPE_APPLICATION = 0;
    /**
     * 文件夹
     */
    public static final int ITEM_TYPE_FOLDER = 1;
    /**
     * 预置应用
     */
    public static final int ITEM_TYPE_PRELOADED = 2;
    /**
     * 快捷方式
     */
    public static final int ITEM_TYPE_SHORTCUT = 3;

    public static final String TAG = "AppDataModel";
    public static final String RECOMMEND_APP_ACTION = "android.intent.action.RecommendApp";
    public static final String RECOMMEND_APP_CATEGORY = "android.intent.category.RecommendApp";
    public static final boolean isRecommendAppOn = false;

    public static boolean DEBUG = true;
    private static AppDataModel INSTANCE = new AppDataModel();

    private Context mContext;
    private final Object mLock = new Object();
    protected int mPreviousConfigMcc;
    private LoaderTask mLoaderTask;
    private AllItemList mAllItemList;
    private WeakReference<Callbacks> mCallbacks;
    private Handler mHandler = new Handler();

    private boolean mAllItemsLoaded;
    private boolean mIsLoaderTaskRunning;

    public static final String DEFAULT_FOLDER_SORTID = "com.stv.tools.folder";
    private String mToolsFolderTitle = "tools";
    private final String mNewFolderDefaultTitle;
    private String mNewFolderTitle = "New folder";
    private static final String TITLE_KEY_PREFIX = "title.";

    private List<String> mCibnPreloadedPkgList = new ArrayList<String>(3);
    private Map<String, Integer> mPreloadedIconMap = new ArrayMap<String, Integer>(3);
    private Map<String, String> mPreloadedTitleMap = new ArrayMap<String, String>(3);
    private ArrayMap<String, ItemInfo> mCibnItemInfoMap = new ArrayMap<String, ItemInfo>(3);
    public static final String CIBN_FOLDER_SORTID = "com.stv.app.folder.cibn_preloaded";
    private String mCibnFolderTitle;
    public static final String APP_INSTALL_FAILED = "com.android.packageinstaller.action.APP_INSTALL_FAILED";
    public static final String APP_INSTALL_SUCCESS = "com.android.packageinstaller.action.APP_INSTALL_SUCCESS";

    public static final String ACTION_APP_BADGE_MESSAGE_UPDATE = "android.intent.action.LETV_BADGE_MESSAGE.UPDATE";
    public static final String ACTION_APP_DESKTOP_MSG = "android.intent.action.LETV_APP_DESKTOP_MSG";

    private boolean isUseThreadPool;
    private HandlerThread sWorkerThread = new HandlerThread("app-loader");
    private Handler sWorker;

    public static AppDataModel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AppDataModel();
        }
        return INSTANCE;
    }

    private AppDataModel() {
        mContext = AppPluginActivator.getContext();
        if (mContext == null) {
            throw new IllegalStateException(" Application has not create ! ");
        }
        if (INSTANCE != null) {
            throw new IllegalStateException(" Has already instantiated !");
        }
        /** register intent receivers */
        /** register intent receivers */
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        mContext.registerReceiver(this, filter);
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);

        filter.addAction(APP_INSTALL_FAILED);
        filter.addAction(APP_INSTALL_SUCCESS);
        // filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        // filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(ACTION_APP_BADGE_MESSAGE_UPDATE);
        filter.addAction(ACTION_APP_DESKTOP_MSG);
        mContext.registerReceiver(this, filter);

        //for shorcut
        IntentFilter shortcutFilter = new IntentFilter();
        shortcutFilter.addAction(InstallShortcutReceiver.ACTION_INSTALL_SHORTCUT);
        InstallShortcutReceiver shortcutReceiver = new InstallShortcutReceiver();
        mContext.registerReceiver(shortcutReceiver, shortcutFilter);

        final Resources res = mContext.getResources();
        Configuration config = res.getConfiguration();
        mPreviousConfigMcc = config.mcc;

        mAllItemList = new AllItemList();

        mToolsFolderTitle = res.getString(R.string.default_folder_name_tool);

        mNewFolderDefaultTitle = res.getString(R.string.default_new_folder_name);
        mCibnFolderTitle = res.getString(R.string.preloaded_app_cibn_folder);

        // cibn
        mCibnPreloadedPkgList.add("cn.cibntv.ott");// cibn
        mCibnPreloadedPkgList.add("com.tvm.suntv.news.client.activity");// 环球视讯
        mCibnPreloadedPkgList.add("com.tea.tv.room");// 游戏茶餐厅

        // CIBN
        mPreloadedIconMap.put("cn.cibntv.ott", Integer.valueOf(R.drawable.icon_preloaded_cibn_tv));
        mPreloadedIconMap.put("com.tvm.suntv.news.client.activity", Integer.valueOf(R.drawable.icon_preloaded_cibn_news));
        mPreloadedIconMap.put("com.tea.tv.room", Integer.valueOf(R.drawable.icon_preloaded_cibn_tearoom));

        // CIBN
        mPreloadedTitleMap.put("cn.cibntv.ott", res.getString(R.string.preloaded_app_cibn_hdmv));
        mPreloadedTitleMap.put("com.tvm.suntv.news.client.activity", res.getString(R.string.preloaded_app_cibn_news));
        mPreloadedTitleMap.put("com.tea.tv.room", res.getString(R.string.preloaded_app_cibn_tearoom));

        // add for StvThemeManager.
        /*String uiType = LetvManagerUtil.getLetvUiType();
        LetvLog.i(TAG, " uiType = " + uiType);
        if ("hk".equals(uiType) || "us".equals(uiType) || "full_us".equals(uiType)) {
            LetvLog.i(TAG, "  change prop.. persist.sys.letvtheme = framework-res.apk . persist.sys.letvtheme.enable = 0");
            int prop = Integer.parseInt(android.os.SystemProperties.get("persist.sys.letvtheme.enable", "0"));
            if (0 != prop) {
                android.os.SystemProperties.set("persist.sys.letvtheme", "framework-res.apk");
                android.os.SystemProperties.set("persist.sys.letvtheme.enable", "0");
            }
        }*/
        isUseThreadPool = Utilities.verifySupportSdk(Utilities.support_sdk_version_100);
        if (!isUseThreadPool) {
            sWorkerThread.start();
            sWorker = new Handler(sWorkerThread.getLooper());
        }
    }

    /**
     * Set this as the current app fragment object for the loader.
     */
    public void register(Callbacks callbacks) {
        synchronized (mLock) {
            mCallbacks = new WeakReference<Callbacks>(callbacks);
        }
    }

    public void unRegister() {
        synchronized (mLock) {
            mCallbacks = null;
        }
    }

    public Callbacks getCallback() {
        return mCallbacks != null ? mCallbacks.get() : null;
    }

    public void startLoader(boolean isLaunching) {
        synchronized (mLock) {
            if (mCallbacks != null && mCallbacks.get() != null) {
                isLaunching = isLaunching || stopLoaderLocked();
                mLoaderTask = new LoaderTask(mContext, isLaunching);
                if (isUseThreadPool) {
                    Job job = new Job(AppPluginActivator.getContext().getPackageName(), JobType.JOB_DATA_INIT) {
                        @Override
                        public void run() {
                            mLoaderTask.run();
                        }
                    };
                    ThreadPool.getInstance().submit(job);
                } else {
                    sWorkerThread.setPriority(Thread.NORM_PRIORITY);
                    sWorker.post(mLoaderTask);
                }

            }
        }
    }

    /**
     * When the launcher is in the background, it'mFragmentContentView possible for it to miss paired configuration changes. So whenever we trigger the loader from the background tell the launcher
     * that it needs to re-run the loader when it comes back instead of doing it now.
     */
    public void startLoaderFromBackground() {
        boolean runLoader = false;
        if (mCallbacks != null) {
            // Only actually run the loader if they're not paused.
            Callbacks callbacks = mCallbacks.get();
            if (!callbacks.setLoadOnResume()) {
                runLoader = true;
            }
        }
        if (runLoader) {
            startLoader(false);
        }
    }

    private void forceReload() {
        resetLoadedState(true);
        // Do this here because if the launcher activity is running it will be restarted.
        // If it'mFragmentContentView not running startLoaderFromBackground will merely tell it that it needs
        // to reload.
        startLoaderFromBackground();
    }

    public void resetLoadedState(boolean resetAllAppsLoaded) {
        synchronized (mLock) {
            // Stop any existing loaders first, so they don't set mAllAppsLoaded to true later
            stopLoaderLocked();
            if (resetAllAppsLoaded) {
                mAllItemsLoaded = false;
            }
        }
    }

    private boolean stopLoaderLocked() {
        boolean isLaunching = false;
        LoaderTask oldTask = mLoaderTask;
        if (oldTask != null) {
            if (oldTask.isLaunching()) {
                isLaunching = true;
            }
            oldTask.stopLocked();
        }
        return isLaunching;
    }

    void enqueuePackageUpdated(final PackageUpdatedTask task) {
        if (isUseThreadPool) {
            Job job = new Job(AppPluginActivator.getContext().getPackageName(), JobType.JOB_DATA_INIT) {
                @Override
                public void run() {
                    task.run();
                }
            };
            ThreadPool.getInstance().submit(job);
        } else {
            sWorker.post(task);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        LetvLog.i(TAG, " onReceive intent = " + intent);
        if (Intent.ACTION_PACKAGE_CHANGED.equals(action) || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            final String packageName = intent.getData().getSchemeSpecificPart();
            final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            LetvLog.i(TAG, " onReceive packageName = " + packageName + " replacing = " + replacing);
            int op = PackageUpdatedTask.OP_NONE;
            if (packageName == null || packageName.length() == 0) {
                LetvLog.i(TAG, " they sent us a bad intent packageName = " + packageName);
                return;
            }
            if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                op = PackageUpdatedTask.OP_UPDATE;
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                if (!replacing) {
                    op = PackageUpdatedTask.OP_REMOVE;
                }
                // else, we are replacing the package, so a PACKAGE_ADDED will be sent
                // later, we will update the package at this time
            } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                if (!replacing) {
                    op = PackageUpdatedTask.OP_ADD;
                } else {
                    op = PackageUpdatedTask.OP_UPDATE;
                }
            }
            if (op != PackageUpdatedTask.OP_NONE) {
                enqueuePackageUpdated(new PackageUpdatedTask(op, new String[]{
                        packageName
                }));
            }
        } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
            /** First, schedule to add these apps back in. */
            String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            enqueuePackageUpdated(new PackageUpdatedTask(PackageUpdatedTask.OP_ADD, packages));
            /** Then, rebind everything. */
            // startLoaderFromBackground();
        } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
            String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            enqueuePackageUpdated(new PackageUpdatedTask(PackageUpdatedTask.OP_UNAVAILABLE, packages));
        } else if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            /**
             * If we have changed locale we need to clear out the labels in all apps/workspace.
             */
            forceReload();
        } else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
            /**
             * Check if configuration change was an mcc/mnc change which would affect app resources and we would need to clear out the labels in all apps/workspace. Same handling as above for
             * ACTION_LOCALE_CHANGED
             */
            Configuration currentConfig = context.getResources().getConfiguration();
            if (mPreviousConfigMcc != currentConfig.mcc) {
                LetvLog.d(TAG, "Reload apps on config change. curr_mcc:" + currentConfig.mcc + " prevmcc:" + mPreviousConfigMcc);
                forceReload();
            }
            /** Update previousConfig */
            mPreviousConfigMcc = currentConfig.mcc;
        } else if (APP_INSTALL_FAILED.equals(action)) {
            String reason = intent.getStringExtra("storage_error_reason");
            String packageName = intent.getStringExtra("com.android.packageinstaller.action.package");
            LetvLog.i(TAG, "onReceive packageName = " + packageName + " reason = " + reason);
            //DownloadAppPresenter.getInstance().resetStatusIfInstallFail(packageName);
        } else if (APP_INSTALL_SUCCESS.equals(action)) {
            // store has install success prompt
            String packageName = intent.getStringExtra("com.android.packageinstaller.action.package");
            String appName = intent.getStringExtra("com.android.packageinstaller.action.appname");
            LetvLog.i(TAG, " packageName = " + packageName + " appName = " + appName);

            //DownloadAppPresenter.getInstance().installSuccess(packageName);
        } else if (ACTION_APP_BADGE_MESSAGE_UPDATE.equals(action) || ACTION_APP_DESKTOP_MSG.equals(action)) {
            dealMsgBroadcast(intent);
        }
    }

    /**
     * Set icon index,id,backgroundID,folder index.
     *
     * @param arrayList
     */
    private void setIndex(ArrayList<ItemInfo> arrayList) {
        int j = 0;
        for (ItemInfo itemInfo : arrayList) {
            if (itemInfo != null) {
                // set id and index
                itemInfo.setId((long) j);
                itemInfo.setIndex(j);
                itemInfo.setInFolderIndex(0);
                j++;
            }
            LetvLog.d(TAG, "setIndex  when init db print all app " + itemInfo);
        }
    }

    public interface Callbacks {
        public void bindApps(ArrayList<ItemInfo> itemInfos);

        public void bindAppAdded(ArrayList<ItemInfo> adds);

        public void bindAppRemoved(ArrayList<ItemInfo> removes, ArrayList<ItemInfo> removeContainFolder);

        public void bindAppUpdated(ArrayList<ItemInfo> updates, ArrayList<ItemInfo> updateContainFolder);

        public boolean setLoadOnResume();

        void notifySuperscript(ItemInfo itemInfo, FolderInfo folderInfo);

        void notifyStateChange(ItemInfo itemInfo);
    }

    private class LoaderTask implements Runnable {

        private boolean mStopped;
        private Context mContext;
        private boolean mIsLaunching;

        public LoaderTask(Context context, boolean isLaunching) {
            mContext = context;
            mIsLaunching = isLaunching;
        }

        boolean isLaunching() {
            return mIsLaunching;
        }

        void stopLocked() {
            synchronized (LoaderTask.this) {
                mStopped = true;
                this.notify();
            }
        }

        Callbacks tryGetCallbacks(Callbacks oldCallbacks) {
            synchronized (mLock) {
                if (mStopped) {
                    return null;
                }
                if (mCallbacks == null) {
                    return null;
                }
                final Callbacks callbacks = mCallbacks.get();
                if (callbacks != oldCallbacks) {
                    return null;
                }
                if (callbacks == null) {
                    LetvLog.w(TAG, "no mCallbacks");
                    return null;
                }
                return callbacks;
            }
        }

        @Override
        public void run() {
            synchronized (mLock) {
                mIsLoaderTaskRunning = true;
            }
            keep_running:
            {
                synchronized (mLock) {
                    android.os.Process.setThreadPriority(mIsLaunching
                            ? Process.THREAD_PRIORITY_DEFAULT
                            : Process.THREAD_PRIORITY_BACKGROUND);
                }

                loadAndBindAllItems();

                if (mStopped) {
                    break keep_running;
                }
                synchronized (mLock) {
                    if (mIsLaunching) {
                        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    }
                }
                synchronized (mLock) {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                }
            }
            mContext = null;
            synchronized (mLock) {
                // If we are still the last one to be scheduled, remove ourselves.
                if (mLoaderTask == this) {
                    mLoaderTask = null;
                }
                mIsLoaderTaskRunning = false;
            }
        }

        private void loadAndBindAllItems() {
            final long loadTime = SystemClock.uptimeMillis();
            LetvLog.d(TAG, " loadAndBindAllItems mAllItemsLoaded = " + mAllItemsLoaded);
            if (!mAllItemsLoaded) {
                loadAllItems();
                synchronized (LoaderTask.this) {
                    if (mStopped) {
                        return;
                    }
                    mAllItemsLoaded = true;
                }
            } else {
                onlyBindAllItems();
            }
            LetvLog.i(TAG, " loadAndBindAllItems use time " + (SystemClock.uptimeMillis() - loadTime) + " ms");
        }

        private void loadAllItems() {
            long loadTime = SystemClock.uptimeMillis();
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                LetvLog.w(TAG, "LoaderTask running with no launcher (loadAllItems)");
                return;
            }
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            final Intent recommendAppIntent = new Intent(RECOMMEND_APP_ACTION, null);
            recommendAppIntent.addCategory(RECOMMEND_APP_CATEGORY);

            final PackageManager packageManager = mContext.getPackageManager();
            List<ResolveInfo> apps = null;
            List<ResolveInfo> recommendApps = null;

            int N = Integer.MAX_VALUE;
            int i = 0;
            int batchSize = -1;
            // 遍历系统中的应用
            while (i < N && !mStopped) {
                if (i == 0) {
                    apps = packageManager.queryIntentActivities(mainIntent, 0);
                    if (isRecommendAppOn) {
                        recommendApps = packageManager.queryIntentActivities(recommendAppIntent, 0);
                    }
                    if (apps == null) {
                        return;
                    }
                    if (recommendApps != null) {
                        apps.addAll(recommendApps);
                    }
                    N = apps.size();
                    if (N == 0) {
                        return;
                    }
                    batchSize = N;
                }
                synchronized (mAllItemList) {
                    mAllItemList.clear();
                    for (int j = 0; i < N && j < batchSize; j++) {
                        ItemInfo itemInfo = new ItemInfo(packageManager, apps.get(i));
                        itemInfo.setType(ITEM_TYPE_APPLICATION);
                        LetvLog.i(TAG, "loadAllItems every itemInfo = " + itemInfo);
                        if (mCibnPreloadedPkgList.contains(itemInfo.getPackageName())) {
                            mCibnItemInfoMap.put(itemInfo.getPackageName(), itemInfo);
                        }
                        mAllItemList.add(itemInfo);
                        i++;
                    }
                }
            }
            LetvLog.i(TAG, " loadAllItems query use  " + (SystemClock.uptimeMillis() - loadTime) + "ms");
            loadTime = SystemClock.uptimeMillis();
            // 筛选
            final ArrayList<ItemInfo> added = filteringItems(mAllItemList.data);

            LetvLog.i(TAG, " loadAllItems filteringItems use  " + (SystemClock.uptimeMillis() - loadTime) + "ms");

            // Post callback on main thread
            final Runnable r = new Runnable() {
                public void run() {
                    final long bindTime = SystemClock.uptimeMillis();
                    final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        callbacks.bindApps(added);
                        LetvLog.i(TAG, "loadAllItems bound " + added.size() + " apps in " + (SystemClock.uptimeMillis() - bindTime) + "ms");
                    } else {
                        LetvLog.w(TAG, "loadAllItems not binding shortcuts: no Launcher activity");
                    }
                }
            };
            //runOnMainThread(r);
            r.run();
        }

        private void onlyBindAllItems() {
            final Callbacks oldCallbacks = mCallbacks.get();
            if (oldCallbacks == null) {
                LetvLog.w(TAG, " onlyBindAllItems LoaderTask running with no launcher (onlyBindAllItems)");
                return;
            }
            final Runnable r = new Runnable() {
                public void run() {
                    final long t = SystemClock.uptimeMillis();
                    final Callbacks callbacks = tryGetCallbacks(oldCallbacks);
                    if (callbacks != null) {
                        // TODO: should not use getAll
                        ArrayList<ItemInfo> arrayList = (ArrayList<ItemInfo>) ItemInfoDBHelper.getInstance().getAllItemAndFolder(true);
                        callbacks.bindApps(arrayList);
                        LetvLog.i(TAG, " onlyBindAllItems bound all shortcuts from cache or db use "
                                + (SystemClock.uptimeMillis() - t)
                                + "ms");
                    } else {
                        LetvLog.w(TAG, "not binding shortcuts from cache: no Launcher activity");
                    }
                }
            };
            //runOnMainThread(r);
            r.run();
        }

        private ArrayList<ItemInfo> filteringItems(ArrayList<ItemInfo> systemLoad) {
            if (systemLoad == null) {
                return null;
            }
            LetvLog.i(TAG, " filteringItems begin ");
            List<ItemInfo> fromDB = ItemInfoDBHelper.getInstance().getAllItem();
            ArrayList<ItemInfo> returnList = new ArrayList<ItemInfo>(systemLoad);
            // Some case db size > 0 but db not init ,because some apk is update or install when os starting.
            if (fromDB == null || fromDB.size() < 3) {// system app must more than 3
                ItemInfoDBHelper.getInstance().deleteAll();
                LetvLog.i(TAG, " filteringItems init db ");
                // new cibn folder
                FolderInfo cibnFolder = null;
                if (Utilities.isCIBN()) {
                    cibnFolder = newPreLoadedFolder(0, mCibnPreloadedPkgList, mCibnFolderTitle, CIBN_FOLDER_SORTID, returnList);
                    if (cibnFolder != null) {
                        returnList.add(cibnFolder);
                    }
                }
                // 2.sort
                AppLoaderConfig.sort(returnList, true);
                // 3 .set index
                setIndex(returnList);
                // 5. init cibn folder
                if (cibnFolder != null) {
                    PreferencesUtils.putLong(mContext, cibnFolder.mSortID, cibnFolder.getId());
                    cibnFolder.setItemContainer();
                    LetvLog.i(TAG, " filteringItems cibn folder = " + cibnFolder.getContents());
                    ArrayList<ItemInfo> notInstalled = new ArrayList<ItemInfo>(4);
                    for (ItemInfo inFolder : cibnFolder.getContents()) {
                        // preloaded apk not install id is null
                        if (inFolder.getId() == null) {
                            notInstalled.add(inFolder);
                        }
                    }
                    returnList.addAll(notInstalled);
                }
                // 6.insert to db
                ItemInfoDBHelper.getInstance().insertInTx(returnList);
                if (cibnFolder != null) {
                    // remove has in cibn folder
                    returnList.removeAll(cibnFolder.getContents());
                }
                AppLoaderConfig.release();
            } else {
                // add cibn folder
                if (Utilities.isCIBN()) {
                    FolderInfo folderInfo = newCibnPreloadedFolder();
                    if (folderInfo != null) {
                        fromDB = ItemInfoDBHelper.getInstance().getAllItem();
                    }
                }
                LetvLog.i(TAG, " filteringItems load from system ");
                // 1.delete has in db
                ArrayList<ItemInfo> hasInDB = new ArrayList<ItemInfo>(systemLoad.size());
                ArrayList<ItemInfo> hasInSystem = new ArrayList<ItemInfo>(systemLoad.size());
                for (ItemInfo system : returnList) {
                    for (ItemInfo db : fromDB) {
                        if (db != null && db.equals(system)) {
                            // language or main activity maybe changed.
                            db.setTitle(system.getTitle());
                            db.setFlags(system.getFlags());
                            // If has in db ,should remove from system list.
                            hasInDB.add(system);
                            // If not install ,should remove from db;
                            hasInSystem.add(db);
                            break;
                        }
                    }
                }

                // language or main activity maybe changed,so update its.
                ItemInfoDBHelper.getInstance().updateInTx(fromDB);
                // when switch language, tool folder and cibn folder title need update
                switchFolderTitleByLocal();
                // remove has in db
                returnList.removeAll(hasInDB);
                // 2.remove not install but in db
                fromDB.removeAll(hasInSystem);
                LetvLog.i(TAG, " remove not install but in db : " + fromDB);
                ItemInfoDBHelper.getInstance().deleteInTx(fromDB);
                // 3.remove not show
                // removeNotShow(returnList);
                if (returnList.size() > 0) {
                    // 4.sort
                    AppLoaderConfig.sort(returnList, false);
                    // 5.insert news to db
                    long lastIndex = ItemInfoDBHelper.getInstance().getLastIndex();
                    for (ItemInfo itemInfo : returnList) {
                        lastIndex++;
                        itemInfo.setIndex((int) lastIndex);
                        itemInfo.setInFolderIndex(0);
                        LetvLog.i(TAG, " insert news to db : " + itemInfo);
                    }
                    ItemInfoDBHelper.getInstance().insertInTx(returnList);
                }
                addAppToToolFolder();
                changeCibnWithoutClassName();
                returnList.clear();
                returnList.addAll(ItemInfoDBHelper.getInstance().getAllItemAndFolder(true));
                // 可能有强制排序的需求
                checkForceAdjustSort(returnList);
            }
            // 判断系统升级应用view上是否需要显示new角标
            setSuperscript(returnList);
            LetvLog.i(TAG, " filteringItems end ");
            return returnList;
        }
    }

    private void setSuperscript(ArrayList<ItemInfo> returnList) {
        long startTime = System.currentTimeMillis();
        ArrayList<ItemInfo> itemInfoList = getShowSuperscriptItemInfoList();
        if (itemInfoList != null && itemInfoList.size() > 0) {
            for (ItemInfo itemInfo : itemInfoList) {
                if (itemInfo != null) {
                    int index = returnList.indexOf(itemInfo);
                    LetvLog.d(TAG, "setSuperscript index = " + index);
                    if (index != -1) {
                        ItemInfo updateInfo = returnList.get(index);
                        if (updateInfo != null) {
                            updateInfo.setSuperscriptCount(itemInfo.getSuperscriptCount());
                            updateInfo.setSuperscriptType(itemInfo.getSuperscriptType());
                        }
                    } else {// 判断是否在文件夹中
                        for (ItemInfo info : returnList) {
                            if (info instanceof FolderInfo) {
                                FolderInfo folderInfo = (FolderInfo) info;
                                int updateIndex = folderInfo.getContents().indexOf(itemInfo);
                                if (updateIndex != -1 && updateIndex < folderInfo.getLength()) {
                                    ItemInfo updateInfo = folderInfo.getContents().get(updateIndex);
                                    if (updateInfo != null) {
                                        updateInfo.setSuperscriptCount(itemInfo.getSuperscriptCount());
                                        updateInfo.setSuperscriptType(itemInfo.getSuperscriptType());
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        long endTime = System.currentTimeMillis();
        LetvLog.d(TAG, "setSuperscript total time : " + (endTime - startTime));
    }

    private ArrayList<ItemInfo> getShowSuperscriptItemInfoList() {
        ArrayList<ItemInfo> infoList = new ArrayList<ItemInfo>(3);
        ItemInfo systemupdateInfo = getShowSuperscriptItemInfo("com.stv.systemupgrade");
        if (systemupdateInfo != null) {
            infoList.add(systemupdateInfo);
        }
        ItemInfo msgInfo = getShowSuperscriptItemInfo("com.stv.message");
        if (msgInfo != null) {
            infoList.add(msgInfo);
        }
        ItemInfo feedbackInfo = getShowSuperscriptItemInfo("com.stv.feedback");
        if (feedbackInfo != null) {
            infoList.add(feedbackInfo);
        }
        return infoList;
    }

    /**
     * 当桌面启动时应用桌面还没有加载完成,此时有广播要显示角标
     * 为了解决这个问题，在launcher的DataModel中加了广播的处理。
     *
     * @param pkg
     * @return
     */
    private ItemInfo getShowSuperscriptItemInfo(String pkg) {
        String keyPrefix = "plugin.";
        SharedPreferences hostPreference = LauncherState.getInstance().getHostContext().
                getSharedPreferences("LauncherCommon", Context.MODE_PRIVATE);
        String value = hostPreference.getString(keyPrefix + pkg, null);
        if (value == null) {
            String key = null;
            if ("com.stv.message".equals(pkg)) {
                key = "just_for_app_msg";
            } else if ("com.stv.systemupgrade".equals(pkg)) {
                key = "just_for_app_systemupgrade";
            }
            if (key != null) {
                String str = hostPreference.getString(key, null);
                if (str != null) {
                    String[] superscript = str.split(";");
                    if (superscript != null && superscript.length == 4) {
                        String packageName = superscript[0];
                        String className = superscript[1];
                        String msgType = superscript[2];
                        String msgCount = superscript[3];
                        try {
                            int count = Integer.parseInt(msgCount);
                            if (count > 0) {
                                ItemInfo itemInfo = new ItemInfo();
                                itemInfo.setPackageName(packageName);
                                itemInfo.setClassName(className);
                                itemInfo.setSuperscriptCount(count);
                                itemInfo.setSuperscriptType(Integer.parseInt(msgType));
                                return itemInfo;
                            } else {
                                LetvLog.d(TAG, "getShortcutInfoSuperscript remove packageName = " + packageName);
                                SharedPreferences.Editor editor = hostPreference.edit();
                                editor.remove(key);
                                editor.commit();
                            }
                        } catch (NumberFormatException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        } else {
            try {
                JSONObject object = new JSONObject(value);
                String packageName = object.getString("packageName");
                String className = object.getString("className");
                int msgType = object.getInt("msgType");
                int msgCount = object.getInt("msgCount");
                if (msgCount > 0) {
                    ItemInfo itemInfo = new ItemInfo();
                    itemInfo.setPackageName(packageName);
                    itemInfo.setClassName(className);
                    itemInfo.setSuperscriptCount(msgCount);
                    itemInfo.setSuperscriptType(msgType);
                    return itemInfo;
                } else {
                    SharedPreferences.Editor editor = hostPreference.edit();
                    editor.remove(keyPrefix + pkg);
                    editor.commit();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void checkForceAdjustSort(ArrayList<ItemInfo> returnList) {
        long startTime = System.currentTimeMillis();
        AppLoaderConfig.setLoadType(returnList, true);
        // loadSortMapFromXml(false);
        HashMap<String, Integer> forceOrderMap = AppLoaderConfig.forceSortMap;
        if (forceOrderMap != null && forceOrderMap.size() > 0) {// 说明有需要强制更新的app
            LetvLog.d(TAG, "checkForceAdjustSort forceOrderMap.size():" + forceOrderMap.size());
            Set<String> keys = forceOrderMap.keySet();
            for (Iterator<String> it = keys.iterator(); it.hasNext(); ) {
                String componentName = it.next();
                Integer needChangedPos = forceOrderMap.get(componentName);
                LetvLog.d(TAG, "checkForceAdjustSort componentName :" + componentName);

                if (componentName != null && needChangedPos < returnList.size()) {
                    for (int i = 0; i < returnList.size(); i++) {
                        ItemInfo info = returnList.get(i);
                        String sortStr = null;
                        if (info instanceof FolderInfo) {
                            FolderInfo folderInfo = (FolderInfo) info;
                            LetvLog.d(TAG, "checkForceAdjustSort: type:" + folderInfo.getType() +
                                    ",folder_id:" + folderInfo.getFolder_id());
                            if (folderInfo.getType() == ITEM_TYPE_FOLDER) {
                                if (DEFAULT_FOLDER_SORTID.equals(folderInfo.getFolder_id())) {
                                    sortStr = DEFAULT_FOLDER_SORTID;
                                } else if (CIBN_FOLDER_SORTID.equals(folderInfo.getFolder_id())) {
                                    sortStr = CIBN_FOLDER_SORTID;
                                }
                            }
                        } else {
                            sortStr = info.getComponentNameStr();
                        }

                        // LetvLog.d(TAG, "checkForceAdjustSort sortStr" + sortStr + "needChangedPos = " + needChangedPos +
                        // " info.getIndex = " + info.getIndex() +" title: " + info.getTitle());

                        ItemInfo needChangedItemInfo = returnList.get(needChangedPos);
                        int changeIndex = needChangedItemInfo.getIndex();
                        if (componentName.equals(sortStr)) {

                            if (needChangedPos == i) {
                                break;
                            }

                            ItemInfo removeInfo = returnList.remove(i);
                            returnList.add(needChangedPos, removeInfo);
                            info.setIndex(changeIndex);

                            // 判断是否是文件夹，如果是文件夹，文件夹中的item所属的文件夹的索引也需要改变
                            if (info instanceof FolderInfo) {
                                FolderInfo folderInfo = (FolderInfo) info;
                                folderInfo.setItemContainer();
                            }

                            LetvLog.d(TAG, "checkForceAdjustSort needChangedPos = " + needChangedPos + " i = " + i);
                            if (i > needChangedPos) {// 从后往前移动
                                List<ItemInfo> itemInfoList = returnList.subList(needChangedPos + 1, i + 1);
                                sortFront2Back(itemInfoList, true);
                            } else if (i < needChangedPos) {// 从前往后移动
                                List<ItemInfo> itemInfoList = returnList.subList(i, needChangedPos);
                                sortFront2Back(itemInfoList, false);
                            }
                            break;
                        } else {
                            ItemInfo itemInfo = findItemInfoInFolder(info, componentName, changeIndex);
                            if (itemInfo != null) {
                                if (info instanceof FolderInfo) {
                                    FolderInfo folderInfo = (FolderInfo) info;
                                    if (folderInfo.getLength() == 0) {
                                        LetvLog.d(TAG, "checkForceAdjustSort folderInfo ： " + folderInfo);
                                        returnList.remove(folderInfo);
                                        ItemInfoDBHelper.getInstance().delete(folderInfo);
                                    }
                                }
                                // 插入到指定位置
                                returnList.add(needChangedPos, itemInfo);
                                // 移动位置
                                List<ItemInfo> itemInfoList = returnList.subList(needChangedPos + 1, returnList.size());
                                sortFront2Back(itemInfoList, true);
                                break;
                            }
                        }
                    }
                }
            }
            // 更新数据库
            ItemInfoDBHelper.getInstance().updateInTx(returnList);
        }
        AppLoaderConfig.forceSortMap.clear();
        long endTime = System.currentTimeMillis();
        LetvLog.d(TAG, "checkForceAdjustSort total time : " + (endTime - startTime));
    }

    private ItemInfo findItemInfoInFolder(ItemInfo info, String componentName, int changeIndex) {
        if (info instanceof FolderInfo) {
            FolderInfo folderInfo = (FolderInfo) info;
            ArrayList<ItemInfo> hasInFolderList = folderInfo.getContents();
            // 判断是否在文件夹中
            if (hasInFolderList != null && hasInFolderList.size() > 0) {
                for (ItemInfo itemInfo : hasInFolderList) {
                    if (componentName.equals(itemInfo.getComponentNameStr())) {
                        LetvLog.d(TAG, "checkForceAdjustSort has in folder componentName = " + componentName);
                        // 如果在文件夹中，从文件夹中移除，（更新数据库）
                        hasInFolderList.remove(itemInfo);
                        itemInfo.setContainer(0L);
                        itemInfo.setContainerName("");
                        itemInfo.setIndex(changeIndex);
                        itemInfo.setInFolderIndex(0);
                        ItemInfoDBHelper.getInstance().update(itemInfo);
                        return itemInfo;
                    }
                }
            }
        }
        return null;
    }

    private void sortFront2Back(List<ItemInfo> itemInfoList, boolean isFront2Back) {
        if (itemInfoList != null && itemInfoList.size() > 0) {
            for (int k = 0; k < itemInfoList.size(); k++) {
                ItemInfo itemInfo = itemInfoList.get(k);
                if (isFront2Back) {
                    itemInfo.setIndex(itemInfo.getIndex() + 1);
                } else {
                    itemInfo.setIndex(itemInfo.getIndex() - 1);
                }
                if (itemInfo instanceof FolderInfo) {
                    FolderInfo folderInfo = (FolderInfo) itemInfo;
                    folderInfo.setItemContainer();
                }
            }
        }
    }

    private void addAppToToolFolder() {
        int key = PreferencesUtils.getInt(mContext, "add_to_tool_folder", 0);
        if (key == 1) {
            return;
        }
        ItemInfo toolInfo = ItemInfoDBHelper.getInstance().getFolderInfoByFolderId(DEFAULT_FOLDER_SORTID);
        if (toolInfo != null) {// 工具文件夹存在
            long id = toolInfo.getId();
            int folderIndex = toolInfo.getIndex();
            List<ItemInfo> itemList = ItemInfoDBHelper.getInstance().getFolderShortsByID(id);
            int itemCount = itemList.size();
            if (itemCount > 0) {
                ItemInfo itemInfo = itemList.get(itemCount - 1);
                int inFolderIndex = itemInfo.getInFolderIndex();
                LetvLog.d(TAG, "addAppToToolFolder last itemInfo = " + itemInfo + " inFolderIndex = " + inFolderIndex);
                ComponentName componentName = new ComponentName("com.android.browser", "com.android.browser.BrowserActivity");
                ItemInfo browerItemInfo = ItemInfoDBHelper.getInstance().getShortByComponent(componentName.flattenToString());
                LetvLog.d(TAG, "addAppToToolFolder browerItemInfo = " + browerItemInfo);
                if (browerItemInfo != null) {
                    browerItemInfo.setContainer(id);
                    browerItemInfo.setContainerName(toolInfo.getTitle());
                    browerItemInfo.setIndex(folderIndex);
                    browerItemInfo.setInFolderIndex(inFolderIndex + 1);
                    LetvLog.d(TAG, "addAppToToolFolder id = " + id + " title = " + toolInfo.getTitle() + " folderIndex = " + folderIndex);
                    ItemInfoDBHelper.getInstance().update(browerItemInfo);
                    PreferencesUtils.putInt(mContext, "add_to_tool_folder", 1);
                }
            }
        }
    }

    /**
     * 由于去掉预置应用的className,所以需要将数据库中的className设置为""
     * 等下载完成后在重新设置为实际值.
     */
    private void changeCibnWithoutClassName() {
        List<ItemInfo> cibnItemInfoList = ItemInfoDBHelper.getInstance().getAllItemByType(AppDataModel.ITEM_TYPE_PRELOADED);
        if (cibnItemInfoList != null && cibnItemInfoList.size() > 0) {
            LetvLog.d(TAG, "changeCibnWithoutClassName size = " + cibnItemInfoList.size());
            for (ItemInfo itemInfo : cibnItemInfoList) {
                if (itemInfo != null && !TextUtils.isEmpty(itemInfo.getClassName())) {
                    itemInfo.setClassName("");
                    ComponentName componentName = new ComponentName(itemInfo.getPackageName(), itemInfo.getClassName());
                    itemInfo.setComponentNameStr(componentName.flattenToString());
                    ItemInfoDBHelper.getInstance().update(itemInfo);
                }
            }
        }
    }

    /**
     * Runs the specified runnable immediately if called from the main thread, otherwise it is posted on the main thread handler.
     */
    private void runOnMainThread(Runnable r) {
        runOnMainThread(r, 0);
    }

    private void runOnMainThread(Runnable r, int type) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            r.run();
        } else {
            mHandler.post(r);
        }
    }

    private class PackageUpdatedTask implements Runnable {
        public static final int OP_NONE = 0;
        public static final int OP_ADD = 1;
        public static final int OP_UPDATE = 2;
        public static final int OP_REMOVE = 3; // uninstlled
        public static final int OP_UNAVAILABLE = 4; // external media unmounted
        int mOp;
        String[] mPackages;

        public PackageUpdatedTask(int op, String[] packages) {
            mOp = op;
            mPackages = packages;
        }

        public void run() {
            final Context context = mContext;
            final String[] packages = mPackages;
            final int N = packages.length;

            ArrayList<ItemInfo> addedList = null;
            ArrayList<ItemInfo> modified = null;
            ArrayList<ItemInfo> removedApps = null;

            synchronized (mAllItemList) {
                mAllItemList.added.clear();
                mAllItemList.modified.clear();
                mAllItemList.removed.clear();

                switch (mOp) {
                    case OP_ADD:
                        for (int i = 0; i < N; i++) {
                            LetvLog.i(TAG, "mAllItemList.addPackage " + packages[i]);
                            mAllItemList.addPackage(context, packages[i]);
                        }
                        break;
                    case OP_UPDATE:
                        for (int i = 0; i < N; i++) {
                            LetvLog.i(TAG, "mAllItemList.updatePackage " + packages[i]);
                            mAllItemList.updatePackage(context, packages[i]);
                        }
                        break;
                    case OP_REMOVE:
                    case OP_UNAVAILABLE:
                        for (int i = 0; i < N; i++) {
                            LetvLog.i(TAG, "mAllItemList.removePackage " + packages[i]);
                            mAllItemList.removePackage(packages[i]);
                        }
                        break;
                }

                if (mAllItemList.added.size() > 0) {
                    addedList = new ArrayList<ItemInfo>(mAllItemList.added);
                    mAllItemList.added.clear();
                }

                if (mAllItemList.modified.size() > 0) {
                    modified = new ArrayList<ItemInfo>(mAllItemList.modified);
                    mAllItemList.modified.clear();
                }

                if (mAllItemList.removed.size() > 0) {
                    removedApps = new ArrayList<ItemInfo>(mAllItemList.removed);
                    mAllItemList.removed.clear();
                }
            }

            LetvLog.i(TAG, " mOp = " + mOp + " addedList = " + addedList + " modified = " + modified + " removedApps = " + removedApps);

            final Callbacks callbacks = mCallbacks != null ? mCallbacks.get() : null;
            /*if (callbacks == null) {
                LetvLog.w(TAG, "Nobody to tell about the new app. Launcher probably is loading. callbacks is null, so return ");
                return;
            }*/

            if (addedList != null) {
                LetvLog.i(TAG, " addedList = " + addedList);
                final ArrayList<ItemInfo> addedFinal = new ArrayList<ItemInfo>(2);
                // removeNotShow(addedFinal);
                int index = ItemInfoDBHelper.getInstance().getLastIndex();
                for (ItemInfo add : addedList) {
                    if (add == null) {
                        continue;
                    }
                    ItemInfo hasInDB = null;
                    if (mCibnPreloadedPkgList.contains(add.getPackageName())) {
                        hasInDB = ItemInfoDBHelper.getInstance().getByPackageAndClass(add.getPackageName(), "");
                    }
                    //LetvLog.i(TAG, " hasInDB = " + hasInDB);
                    // 记录当前的时间戳,用于最近应用的排序
                    add.setOrderTimestamp(System.currentTimeMillis());
                    // if find has in db, update it.
                    LetvLog.d(TAG, "run  hasInDB = " + hasInDB);
                    if (hasInDB != null) {
                        add.setType(ITEM_TYPE_APPLICATION);
                        // install success set downState = null

                        //add.setLoadingTitle(null);
                        //add.setDownState(null);

                        add.setId(hasInDB.getId());
                        add.setIndex(hasInDB.getIndex());
                        add.setContainer(hasInDB.getContainer());
                        // add.setContainerName(hasInDB.getContainerName());
                        add.setInFolderIndex(hasInDB.getInFolderIndex());
                        // not need
                        // add.setApkUrl(hasInDB.getApkUrl());
                        // add.setDownType(hasInDB.getDownType());

                        // 说明是预置应用
                        ItemInfoDBHelper.getInstance().update(add);
                        final ItemInfo preloadApp = add;
                        List<String> cibnPkgList = getCibnPackageNameList();
                        if (cibnPkgList.contains(preloadApp.getPackageName())) {
                            // 此时说明已经安装完了
                            //DownloadAppPresenter.getInstance().installSuccess(preloadApp);
                        }

                        // Maybe main activity is changed,so removed list has it ,should remove it.
                        if (removedApps != null && removedApps.contains(hasInDB)) {
                            removedApps.remove(hasInDB);
                        }
                    } else {
                        final ItemInfo itemInfo = add;
                        add.setType(ITEM_TYPE_APPLICATION);
                        index++;
                        add.setIndex(index);
                        add.setInFolderIndex(0);
                        addedFinal.add(add);
                        /*boolean isPreload = DownloadAppPresenter.getInstance().isContain(itemInfo.getPackageName());
                        if (isPreload) {
                            DownloadAppPresenter.getInstance().installSuccess(itemInfo);
                        }*/
                    }
                }

                LetvLog.i(TAG, " addedFinal = " + addedFinal);
                if (addedFinal.size() > 0) {
                    ItemInfoDBHelper.getInstance().insertInTx(addedFinal);
                    //更新应用列表
                    List<ItemInfo> allAppList = DataModelList.getInstance().allAppList;
                    // some time addList is all shortcut of system
                    if (addedFinal.size() >= allAppList.size()) {
                        addedFinal.removeAll(allAppList);
                    }
                    LetvLog.i(TAG, " bindShortcutAdded addList = " + addedFinal);
                    if (addedFinal.size() > 0) {
                        allAppList.addAll(addedFinal);
                    }
                    final Runnable r = new Runnable() {
                        public void run() {
                            Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                            if (callbacks == cb && cb != null) {
                                // ArrayList<ItemInfo> arrayList = (ArrayList<ItemInfo>) ItemDBHelper.getInstance(
                                // context).getAllItemAndFolder(true);
                                callbacks.bindAppAdded(addedFinal);
                            }
                        }
                    };
                    if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                        r.run();
                    } else {
                        mHandler.postDelayed(r, 500);
                    }
                }
            }

            if (modified != null) {
                LetvLog.i(TAG, " modified = " + modified);
                final ArrayList<ItemInfo> modifiedFinal = modified;
                for (ItemInfo update : modifiedFinal) {
                    ItemInfoDBHelper.getInstance().updateFromSystem(update);
                }
                final ArrayList<ItemInfo> updateContainFolder = handleAppUpdate(modifiedFinal);
                final Runnable r = new Runnable() {
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            // ArrayList<ItemInfo> arrayList = (ArrayList<ItemInfo>) ItemDBHelper.getInstance(
                            // context).getAllItemAndFolder(true);
                            callbacks.bindAppUpdated(modifiedFinal, updateContainFolder);
                        }
                    }
                };
                runOnMainThread(r);
            }

            if (removedApps != null) {
                LetvLog.i(TAG, " removedApps = " + removedApps);
                final ArrayList<ItemInfo> removedAppsFinal = removedApps;
                ArrayList<ItemInfo> cloudApps = new ArrayList<ItemInfo>(1);
                for (ItemInfo rm : removedAppsFinal) {
                    if (mCibnPreloadedPkgList.contains(rm.getPackageName())) {
                        if (removePreloadedApp(rm)) {
                            cloudApps.add(rm);
                        }
                    }
                    boolean deleteResult = ItemInfoDBHelper.getInstance().delete(rm);
                    LetvLog.i(TAG, " deleteResult = " + deleteResult);
                }

                LetvLog.i(TAG, " cloudApps = " + cloudApps);
                removedAppsFinal.removeAll(cloudApps);
                LetvLog.i(TAG, " removedAppsFinal = " + removedAppsFinal);
                final ArrayList<ItemInfo> removeContainFolder = handleAppRemove(removedAppsFinal);
                final Runnable r = new Runnable() {
                    public void run() {
                        Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                        if (callbacks == cb && cb != null) {
                            callbacks.bindAppRemoved(removedAppsFinal, removeContainFolder);
                        }
                    }
                };
                runOnMainThread(r);
            }
        }
    }

    /**
     * 删除应用时,可能在非应用桌面操作, 所以放在Model中处理.
     */
    private ArrayList<ItemInfo> handleAppRemove(ArrayList<ItemInfo> removes) {
        ArrayList<ItemInfo> removeList = new ArrayList<ItemInfo>(removes.size());
        List<ItemInfo> allAppList = DataModelList.getInstance().allAppList;
        if (removes == null || allAppList == null || removes.size() == 0) {
            return removeList;
        }
        for (ItemInfo remove : removes) {
            if (remove != null) {
                Iterator<ItemInfo> iterator = allAppList.iterator();
                while (iterator.hasNext()) {
                    ItemInfo itemInfo = iterator.next();
                    if (itemInfo instanceof FolderInfo) {
                        FolderInfo folderInfo = (FolderInfo) itemInfo;
                        if (folderInfo.getContents().contains(remove)) {
                            folderInfo.remove(remove);
                            removeList.add(folderInfo);
                            if (folderInfo.getLength() == 0) {
                                iterator.remove();
                                ItemInfoDBHelper.getInstance().delete(folderInfo);
                            }
                            break;
                        }
                    } else {
                        if (remove.equals(itemInfo)) {
                            iterator.remove();
                            removeList.add(itemInfo);
                            break;
                        }
                    }
                }
            }
        }
        return removeList;
    }

    private ArrayList<ItemInfo> handleAppUpdate(ArrayList<ItemInfo> updates) {
        ArrayList<ItemInfo> updateList = new ArrayList<ItemInfo>(updates.size());
        List<ItemInfo> allAppList = DataModelList.getInstance().allAppList;
        if (updates == null || updates.size() == 0 || allAppList == null) {
            Log.w(TAG, "handleAppUpdate is null.");
            return updateList;
        }
        for (ItemInfo update : updates) {
            if (update != null) {
                for (ItemInfo cached : allAppList) {
                    if (cached instanceof FolderInfo) {
                        FolderInfo folderInfo = (FolderInfo) cached;
                        if (folderInfo.getContents().contains(update)) {
                            int index = folderInfo.getChildrenIndex(update);
                            ItemInfo info = folderInfo.getChildrenByIndex(index);
                            // update.setBackgroundResID(info.getBackgroundResID());
                            update.setInFolderIndex(info.getInFolderIndex());
                            update.setIndex(info.getIndex());
                            update.setContainer(info.getContainer());
                            update.setContainerName(info.getContainerName());
                            update.setVersionCode(0);
                            folderInfo.getContents().set(index, update);
                            updateList.add(folderInfo);
                            break;
                        }
                    } else {
                        if (update.equals(cached)) {
                            int index = allAppList.indexOf(cached);
                            if (index != -1) {
                                allAppList.set(index, update);
                            }
                            update.setVersionCode(0);
                            updateList.add(update);
                            break;
                        }
                    }
                }
            }
        }
        LetvLog.i(TAG, " onShortcutUpdated updateList = " + updateList);
        return updateList;
    }

    public void onTerminate() {
        mContext.unregisterReceiver(this);
        mHandler.removeCallbacksAndMessages(null);
    }

    public void setCallbacks(Callbacks callbacks) {
        if (callbacks == null) {
            unRegister();
        } else {
            register(callbacks);
        }
    }

    public boolean removePreloadedApp(final ItemInfo removeItem) {
        LetvLog.i(TAG, " removePreloadedApp removeItem = " + removeItem);
        if (removeItem == null) {
            return false;
        }
        // final ItemInfo itemInfo = ItemDBHelper.getInstance().getByPackageAndClass(removeItem.getPackageName(), mCIBNPreloadedApp.get(removeItem.getPackageName()));
        LetvLog.i(TAG, " removePreloadedApp from db itemInfo = " + removeItem);

        boolean deleteResult = ItemInfoDBHelper.getInstance().delete(removeItem);
        LetvLog.i(TAG, " removePreloadedApp deleteResult = " + deleteResult);
        if (deleteResult) {
            // Flag this app is remove by user.
            PreferencesUtils.putBoolean(mContext, removeItem.getPackageName(), true);
            final Runnable r = new Runnable() {
                public void run() {
                    //DownloadAppPresenter.getInstance().removePreloadByPkg(removeItem.getPackageName());
                    Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
                    if (cb != null) {
                        ArrayList removeList = new ArrayList<ItemInfo>(1);
                        removeList.add(removeItem);
                        ArrayList<ItemInfo> removeContainFolder = handleAppRemove(removeList);
                        cb.bindAppRemoved(removeList, removeContainFolder);
                    }
                }
            };
            runOnMainThread(r);
        }
        return deleteResult;
    }

    /**
     * 删除快捷方式
     *
     * @return
     */
    public void removeShortcutApp(ItemInfo removeShortcut) {
        if (removeShortcut == null) {
            return;
        }
        boolean deleteResult = ItemInfoDBHelper.getInstance().delete(removeShortcut);
        LetvLog.i(TAG, " removePreloadedApp deleteResult = " + deleteResult);
        if (deleteResult) {
            Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
            if (cb != null) {
                ArrayList<ItemInfo> removeList = new ArrayList<ItemInfo>(1);
                removeList.add(removeShortcut);
                ArrayList<ItemInfo> removeContainFolder = handleAppRemove(removeList);
                cb.bindAppRemoved(removeList, removeContainFolder);
            }
        }
    }

    public Drawable getPreLoadedAppIcon(String packageName) {
        Drawable drawable = null;
        Integer icon = mPreloadedIconMap.get(packageName);
        if (icon != null) {
            drawable = mContext.getResources().getDrawable(icon.intValue());
        }
        return drawable;
    }

    public String[] getAllFolderTitle() {
        ArrayList<FolderInfo> allFolder = ItemInfoDBHelper.getInstance().getAllFolder();
        String[] titles = new String[allFolder.size()];
        for (int i = 0; i < allFolder.size(); i++) {
            titles[i] = allFolder.get(i).getTitle();
        }
        return titles;
    }

    public String getNewFolderTitle() {
        String[] folders = getAllFolderTitle();
        String newFolder = mNewFolderDefaultTitle;
        LetvLog.d(TAG, "getNewFolderTitle mNewFolderDefaultTitle = " + mNewFolderDefaultTitle);
        newFolderTitle(newFolder, folders, 0);
        LetvLog.d(TAG, "getNewFolderTitle mNewFolderTitle = " + mNewFolderTitle);
        return mNewFolderTitle;
    }

    private void newFolderTitle(String beginTitle, String[] folders, int start) {
        int i = 0;
        for (String string : folders) {
            if (beginTitle.equals(string)) {
                start++;
                beginTitle = mNewFolderDefaultTitle + start;
                break;
            }
            i++;
        }
        if (i == folders.length) {
            mNewFolderTitle = beginTitle;
            return;
        }
        newFolderTitle(beginTitle, folders, start);
    }

    private FolderInfo newCibnPreloadedFolder() {
        FolderInfo folderInfo = null;
        int folderIndex = 24;
        folderInfo = newPreLoadedFolder(folderIndex, mCibnPreloadedPkgList, mCibnFolderTitle, CIBN_FOLDER_SORTID, null);
        LetvLog.i(TAG, " newCibnPreloadedFolder folderInfo = " + folderInfo);
        if (folderInfo != null) {
            ArrayList<ItemInfo> fromIndexList = (ArrayList<ItemInfo>) ItemInfoDBHelper.getInstance().getItemAndFolderFromIndex(folderIndex);
            int index = 0;
            for (int i = 0; i < fromIndexList.size(); i++) {
                ItemInfo s = fromIndexList.get(i);
                index = s.getIndex();
                index++;
                s.setIndex(index);
                LetvLog.i(TAG, " newCibnPreloadedFolder " + s);
                if (s instanceof FolderInfo) {
                    ((FolderInfo) s).setItemContainer();
                }
            }
            ItemInfoDBHelper.getInstance().updateInTx(fromIndexList);
            long folderId = ItemInfoDBHelper.getInstance().insert(folderInfo);
            folderInfo.setId(folderId);
            PreferencesUtils.putLong(mContext, CIBN_FOLDER_SORTID, folderId);
            folderInfo.setItemContainer();
            ItemInfoDBHelper.getInstance().insertInTx(folderInfo.getContents());
        }
        return folderInfo;
    }

    private FolderInfo newPreLoadedFolder(int position, List<String> cibnPackageName, String title, String folder_id, ArrayList<ItemInfo> installedList) {
        LetvLog.i(TAG, " newPreLoadedFolder " + title);
        if (cibnPackageName != null) {
            long folderId = PreferencesUtils.getLong(mContext, folder_id);
            if (folderId == -1) {
                int size = cibnPackageName.size();
                FolderInfo folderInfo = new FolderInfo();
                folderInfo.setIndex(position);
                folderInfo.setTitle(title);
                PreferencesUtils.putString(mContext, TITLE_KEY_PREFIX + CIBN_FOLDER_SORTID, title);
                folderInfo.mSortID = folder_id;
                folderInfo.setFolder_id(folder_id);
                for (int i = 0; i < size; i++) {
                    ItemInfo itemInfo = newPreLoadedApp(i, cibnPackageName.get(i));
                    if (itemInfo != null) {
                        ItemInfo cacheItemInfo = mCibnItemInfoMap.get(itemInfo.getPackageName());
                        if (cacheItemInfo != null) {
                            itemInfo.setClassName(cacheItemInfo.getClassName());
                        }
                        if (installedList != null) {
                            if (installedList.contains(itemInfo)) {
                                LetvLog.i(TAG, " newPreLoadedFolder find in system " + cibnPackageName.get(i));
                                int index = installedList.indexOf(itemInfo);
                                itemInfo = installedList.get(index);
                            }
                        }
                        folderInfo.add(itemInfo);
                    }
                }
                return folderInfo;
            }
        }
        return null;
    }

    private ItemInfo newPreLoadedApp(int position, String packageName) {
        LetvLog.i(TAG, " newPreLoadedApp position = " + position + " packageName = " + packageName);
        if (!TextUtils.isEmpty(packageName)) {
            // If preloaded app flag true(when user remove preloaded app ,set true, see removePreloadedApp()), user has remove it from tv.
            if (!PreferencesUtils.getBoolean(mContext, packageName, false)) {
                ItemInfo add = ItemInfoDBHelper.getInstance().getByPackageAndClass(packageName, "");
                LetvLog.i(TAG, " newPreLoadedApp find from db = " + add);
                if (add == null) {
                    add = new ItemInfo();
                    add.setType(ITEM_TYPE_PRELOADED);
                    if (position != -1) {
                        add.setIndex(position);
                    }
                    add.setPackageName(packageName);
                    add.setClassName("");
                    add.setTitle(mPreloadedTitleMap.get(packageName));
                    add.init();
                    return add;
                }
            } else {
                LetvLog.w(TAG, " newPreLoadedApp packageName has remove by user ");
            }
        }
        return null;
    }

    private void dealMsgBroadcast(Intent intent) {
        String PCKAGE_NAME_EXTRA = "packageName";
        String CLASS_NAME_EXTRA = "className";
        String MSG_TYPE_EXTRA = "msgType";
        String MSG_COUNT_EXTRA = "msgCount";

        String packageName = intent.getStringExtra(PCKAGE_NAME_EXTRA);
        String className = intent.getStringExtra(CLASS_NAME_EXTRA);
        int msgType = intent.getIntExtra(MSG_TYPE_EXTRA, -1);
        int msgCount = intent.getIntExtra(MSG_COUNT_EXTRA, -1);
        LetvLog.i(TAG, "onReceive ---> className = " + className + " msgType = " + msgType + " msgCount: = " + msgCount);
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(className)) {
            return;
        }

        if ("com.stv.systemupgrade".equals(packageName) || "com.stv.message".equals(packageName) ||
                "com.stv.feedback".equals(packageName)) {

            final ItemInfo itemInfo = new ItemInfo();
            itemInfo.setPackageName(packageName);
            itemInfo.setClassName(className);
            itemInfo.superscriptType = msgType;
            itemInfo.superscriptCount = msgCount;

            ItemInfo updateItemInfo = null;
            FolderInfo inFolderInfo = null;

            List<ItemInfo> allAppList = DataModelList.getInstance().allAppList;
            for (ItemInfo cached : allAppList) {
                if (cached != null) {
                    if (cached instanceof FolderInfo) {
                        FolderInfo folderInfo = (FolderInfo) cached;
                        if (folderInfo.getContents().contains(itemInfo)) {
                            int index = folderInfo.getChildrenIndex(itemInfo);
                            ItemInfo info = folderInfo.getChildrenByIndex(index);
                            info.superscriptType = itemInfo.superscriptType;
                            info.superscriptCount = itemInfo.superscriptCount;
                            folderInfo.getContents().set(index, info);
                            updateItemInfo = info;
                            inFolderInfo = folderInfo;
                            break;
                        }
                    } else {
                        if (itemInfo.equals(cached)) {
                            cached.superscriptType = itemInfo.superscriptType;
                            cached.superscriptCount = itemInfo.superscriptCount;
                            updateItemInfo = cached;
                            break;
                        }
                    }
                }
            }

            Callbacks cb = mCallbacks != null ? mCallbacks.get() : null;
            if (cb != null) {
                cb.notifySuperscript(updateItemInfo, inFolderInfo);
            }
        }
    }

    private void switchFolderTitleByLocal() {
        setSystemFolderTitle(TITLE_KEY_PREFIX + CIBN_FOLDER_SORTID, CIBN_FOLDER_SORTID, mCibnFolderTitle);
    }

    private void setSystemFolderTitle(String key, String folderSortId, String newFolderTitle) {
        String oldTitle = PreferencesUtils.getString(mContext, key);
        if (oldTitle == null) {
            return;
        }
        ItemInfo folderInfo = ItemInfoDBHelper.getInstance().getFolderInfoByFolderId(folderSortId);
        if (folderInfo == null) {
            return;
        }
        if (oldTitle.equals(folderInfo.getTitle())) {
            if (oldTitle.equals(newFolderTitle)) {
                return;
            }
            folderInfo.setTitle(newFolderTitle);
            PreferencesUtils.putString(mContext, key, folderInfo.getTitle());
            LetvLog.d(TAG, "setSystemFolderTitle title change");
            ItemInfoDBHelper.getInstance().update(folderInfo);
        }
    }

    public List<String> getCibnPackageNameList() {
        return mCibnPreloadedPkgList;
    }

    public void crush() {
        mAllItemsLoaded = false;
    }
}
