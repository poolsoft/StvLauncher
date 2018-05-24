package com.xstv.launcher.logic.controller;


import android.os.Looper;

import com.xstv.launcher.logic.manager.DataModel;
import com.xstv.launcher.provider.db.ScreenInfo;
import com.xstv.launcher.util.DeferredHandler;
import com.xstv.library.base.LetvLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

//import com.stv.down.listener.DownloadListener;
//import com.stv.launcher.util.ReportLogUtil;
//import com.stv.plugin.upgrade.listener.ReportListener;
//import com.stv.plugin.upgrade.listener.UpgradeListener;
//import com.stv.plugin.upgrade.model.BaseInfo;
//import com.stv.plugin.upgrade.model.Location;
//import com.stv.plugin.upgrade.model.SortInfo;
//import com.stv.plugin.upgrade.model.UpgradeRecord;

/**
 * Created by wuh on 16-3-23.
 * A object of this class is a presenter ,it can dispense some message to ui , some message like load plugin ,remove plugin ,add plugin etc.
 * So ui(View or Activity) should must implements {@link IUICallback} and call {@link DataControl#setUICallback(IUICallback)} )} .
 */
public class DataControl {
    public static final String TAG = DataControl.class.getSimpleName();
    public static int CHANGE_TYPE_UPDATE = 100;
    public static int CHANGE_TYPE_ADD = CHANGE_TYPE_UPDATE + 1;
    private WeakReference<IUICallback> callbackWeakReference;
    private DataModel mDatamodel;
    private DeferredHandler mHandler = new DeferredHandler();

    public void setUICallback(IUICallback callback) {
        callbackWeakReference = new WeakReference<IUICallback>(callback);
    }

    public void setDataModel(DataModel model) {
        mDatamodel = model;
    }

    public IUICallback getUICallback() {
        IUICallback callback = null;
        if (callbackWeakReference != null) {
            callback = callbackWeakReference.get();
        }
        return callback;
    }

    public void startLoad() {
        LetvLog.d(TAG, " startLoad ");
        if (!hasCallback()) {
            return;
        }
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                IUICallback iuiCallback = callbackWeakReference.get();
                if (null != iuiCallback) {
                    iuiCallback.startLoad();
                }
            }
        };
        runOnMainThread(r);
    }

    public void onLoad(final List<ScreenInfo> loadList) {
        LetvLog.d(TAG, " onLoad ");
        if (!hasCallback()) {
            return;
        }
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                IUICallback iuiCallback = callbackWeakReference.get();
                if (null != iuiCallback) {
                    iuiCallback.onLoad(loadList);
                }
            }
        };
        runOnMainThread(r);
    }

    public void finishLoad() {
        LetvLog.d(TAG, " finishLoad ");
        if (!hasCallback()) {
            return;
        }
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                IUICallback iuiCallback = callbackWeakReference.get();
                if (null != iuiCallback) {
                    iuiCallback.finishLoad();
                }
            }
        };
        runOnMainThread(r);
    }

    public void onAddPlugin(final List<ScreenInfo> addList) {
        LetvLog.d(TAG, " onAddPlugin addList = " + addList);
        if (!hasCallback()) {
            return;
        }
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                IUICallback iuiCallback = callbackWeakReference.get();
                if (null != iuiCallback) {
                    iuiCallback.add(addList);
                }
            }
        };
        runOnMainThread(r);
    }

    public void onUpdatePlugin(final List<ScreenInfo> updateList) {
        LetvLog.d(TAG, " onUpdatePlugin updateList = " + updateList);
        if (!hasCallback()) {
            return;
        }
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                IUICallback iuiCallback = callbackWeakReference.get();
                if (null != iuiCallback) {
                    iuiCallback.update(updateList);
                }
            }
        };
        runOnMainThread(r);
    }

    public void onRemovePlugin(final List<String> pluginIDList) {
        LetvLog.d(TAG, " onRemovePlugin pluginIDList = " + pluginIDList);
        if (!hasCallback()) {
            return;
        }
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                IUICallback iuiCallback = callbackWeakReference.get();
                if (null != iuiCallback) {
                    iuiCallback.remove(pluginIDList);
                }
            }
        };
        runOnMainThread(r);
    }

    public void onLockStateChanged(final ArrayList<ScreenInfo> changedList) {
        LetvLog.d(TAG, " onLockStateChanged changedList = " + changedList);
        if (!hasCallback()) {
            return;
        }
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                IUICallback iuiCallback = callbackWeakReference.get();
                if (null != iuiCallback) {
                    iuiCallback.changeLock(changedList);
                }
            }
        };
        runOnMainThread(r);
    }

    public void showRedDot(final String packageName) {
        LetvLog.d(TAG, " showRedDot packageName = " + packageName);
        if (!hasCallback()) {
            return;
        }
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                IUICallback iuiCallback = callbackWeakReference.get();
                if (null != iuiCallback) {
                    iuiCallback.showRedDot(packageName);
                }
            }
        };
        runOnMainThread(r);
    }

    public void getBootVideoDuration(final int second) {
        if (!hasCallback()) {
            return;
        }
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                IUICallback iuiCallback = callbackWeakReference.get();
                if (null != iuiCallback) {
                    iuiCallback.getBootVideoDuration(second);
                }
            }
        };
        runOnMainThread(r);
    }

    private void runOnMainThread(Runnable r) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            r.run();
        } else {
            mHandler.post(r);
        }
    }

    public boolean hasCallback() {
        return !(callbackWeakReference == null || callbackWeakReference.get() == null);
    }

//    @Override
//    public void setDownloadListener(DownloadListener downloadListener) {
//
//    }
//
//    @Override
//    public DownloadListener getDownloadListener() {
//        return null;
//    }
//
//    @Override
//    public void onAppendDone(List<UpgradeRecord> list) {
//        /**
//         * comment for temporary
//         *
//        LetvLog.d(TAG, " onAppendDone begin ");
//        long beginTime = SystemClock.uptimeMillis();
//        List<ScreenInfo> screenInfoList = upgradeToScreenInfo(list, CHANGE_TYPE_ADD);
//        //1 insert to db
//        insertToDB(screenInfoList);
//        //2 notify ui
//        onAddPlugin(screenInfoList);
//        LetvLog.d(TAG, " onAppendDone end use time " + (SystemClock.uptimeMillis() - beginTime) + " ms");
//         */
//    }
//
//    /**
//     * @param list package name of plugin
//     * @param i :value is {@linkplain com.stv.plugin.upgrade.model.OffStrategy#POWER_ON} or {@linkplain com.stv.plugin.upgrade.model.OffStrategy#IMMEDIATE}
//     */
//    @Override
//    public void onOfflineDone(List<String> list, int i) {
//        LetvLog.d(TAG, " onOfflineDone begin ");
//        long beginTime = SystemClock.uptimeMillis();
//        setPluginOffLine(list);
//        onRemovePlugin(list);
//        LetvLog.d(TAG, " onOfflineDone end use time " + (SystemClock.uptimeMillis() - beginTime) + " ms");
//    }
//
//    @Override
//    public void onUpdateDone(List<UpgradeRecord> list) {
//        /**
//         * comment for temporary
//         *
//        LetvLog.d(TAG, " onUpdateDone begin ");
//        long beginTime = SystemClock.uptimeMillis();
//        List<ScreenInfo> screenInfoList = updateDBInfo(upgradeToScreenInfo(list, CHANGE_TYPE_UPDATE));
//        //1 update db
//        updateDB(screenInfoList);
//        //2 notify ui
//        onUpdatePlugin(screenInfoList);
//        LetvLog.d(TAG, " onUpdateDone end use time " + (SystemClock.uptimeMillis() - beginTime) + " ms");
//         */
//    }
//
//    @Override
//    public void onSortDone(List<SortInfo> list) {
//        /** deprotected instead of onBaseInfoDone
//        LetvLog.d(TAG, " onSortDone begin ");
//        long beginTime = SystemClock.uptimeMillis();
//        if (list == null) {
//            LetvLog.e(TAG, " onSortDone error list is null ");
//            return;
//        }
//        List<ScreenInfo> fromDB = ScreenDBHelper.getInstance().getAllUsedPlugin();
//        List<ScreenInfo> updateList = new ArrayList<ScreenInfo>();
//        for (SortInfo sortInfo : list) {
//            if (sortInfo == null || TextUtils.isEmpty(sortInfo.getPid()) || TextUtils.isEmpty(sortInfo.getSortLevel())) {
//                continue;
//            }
//            for (ScreenInfo screenFromDB : fromDB) {
//                if (sortInfo.getPid().equals(screenFromDB.getPackageName())) {
//                    try {
//                        int hot = Integer.parseInt(sortInfo.getSortLevel());
//                        screenFromDB.setHot(hot);
//                        updateList.add(screenFromDB);
//                    } catch (NumberFormatException e) {
//                        LetvLog.e(TAG, " onSortDone error cause NumberFormatException," +
//                                "getSortlevel = " + sortInfo.getSortLevel());
//                    }
//                    break;
//                }
//            }
//        }
//        if (updateList.size() > 0) {
//            ScreenDBHelper.getInstance().updateInTx(updateList);
//        }
//        LetvLog.d(TAG, " onSortDone end use time " + (SystemClock.uptimeMillis() - beginTime) + " ms");
//        */
//    }
//
//        @Override
//    public void onBaseInfoDone(List<BaseInfo> list) {
//        LetvLog.d(TAG, " onBaseInfoDone begin ");
//        if (list == null) {
//            LetvLog.e(TAG, " onBaseInfoDone error list is null ");
//            return;
//        }
//        List<ScreenInfo> fromDB = ScreenDBHelper.getInstance().getAll();
//        List<ScreenInfo> updateList = new ArrayList<ScreenInfo>();
//        for (BaseInfo baseInfo : list) {
//            if (baseInfo == null || TextUtils.isEmpty(baseInfo.getPid())) {
//                continue;
//            }
//            for (ScreenInfo screenFromDB : fromDB) {
//                if (baseInfo.getPid().equals(screenFromDB.getPackageName())) {
//                    screenFromDB.setHot(baseInfo.getSortLevel());
//                    /**
//                     * From server json: description:{"description" :""}
//                     * updateComment:{"updateComment" :""} viewName:{"viewName":
//                     * [ { "lang": "zh-CN","content": "乐见" }, { "lang": "en-US",
//                     * "content": "LeView" } ]}
//                     */
//                    screenFromDB.setScreenTag(baseInfo.getViewNameJson());// json
//                    screenFromDB.setDescribe(baseInfo.getDescriptionJson());
//                    screenFromDB.setTagType(baseInfo.getDesktopCornerPic());
//                    screenFromDB.setIconUrl(baseInfo.getDesktopicon_selected());
//                    screenFromDB.setMark2(baseInfo.getDesktopicon_unselected());
//                    screenFromDB.setImageUrl(baseInfo.getDesktopEffectPic());
//                    updateList.add(screenFromDB);
//                    break;
//                }
//            }
//            if (updateList.size() > 0) {
//                ScreenDBHelper.getInstance().updateInTx(updateList);
//            }
//        }
//    }
//
//    public void onReport(UpgradeRecord record, int status) {
//        StringBuffer reportMsg = new StringBuffer();
//        reportMsg.append("action=plugDownload");
//        reportMsg.append("&frameVersion=" + AppUtil.getAppVersionName(LauncherApplication.INSTANCE));
//        reportMsg.append("&plugVersion=" + record.getPVersionCode());
//        reportMsg.append("&desktopId=" + record.getPid());
//        reportMsg.append("&status=" + status);
//        LetvLog.d(TAG, "UpgradeReport onReport plugDownload = " + reportMsg.toString());
//        ReportLogUtil.getInstance().reportMsg(ReportLogUtil.LOG_TAG_TV_ACTION, reportMsg.toString());
//    }
//
//    public void onLocationDone(List<Location> locationList) {
//        if (locationList != null) {
//            for (Location location : locationList) {
//                String pid = location.getPid();
//                String versionS = "" + location.getpVersionCode();
//                if (pid != null) {
//                    ScreenInfo s = mDatamodel.getScreenByPackageName(pid, true);
//                    LetvLog.i(TAG, " onLocationDone  db : " + s + " location = " + location);
//                    if (s != null && s.getVersionCode().equals(versionS)) {
//                        /**
//                         * 如果插件在桌面管理里,没有使用过,根据标识把插件添加到tab上
//                         * 0:不执行添加 1:执行添加
//                         */
//                        boolean forceAdd = (location.getAddTab() == Location.DO_ADD_TAB);
//                        /**
//                         * 调整已经在tab上的插件位置
//                         * 1:不执行调整 2:执行调整
//                         */
//                        boolean forceChange = (location.getAdjustTab() == Location.DO_ADJUST_TAB);
//                        mDatamodel.modifyPosition(s, forceAdd, forceChange, location.getTabPostion());
//                    }
//                }
//            }
//        }
//    }
//
//    public void updateDB(List<ScreenInfo> updateList) {
//        ScreenDBHelper.getInstance().updateInTx(updateList);
//    }
//
//    public void insertToDB(List<ScreenInfo> insertList) {
//        if (insertList == null) {
//            LetvLog.d(TAG, " insertToDB error insertList is null !");
//            return;
//        }
//        ScreenDBHelper.getInstance().inserOrReplaceInTx(insertList);
//    }
//
//    /**
//     * Only change useful info
//     * <p/>
//     * pid: plugin id
//     * name: file name
//     * strategy:升级策略
//     * pVersionCode: 插件版本
//     * pVersionName: 插件版本号
//     * description: 插件描述 release note
//     *
//     * @param list
//     * @param type
//     * @return
//     */
//    public static ArrayList<ScreenInfo> upgradeToScreenInfo(List<UpgradeRecord> list, int type) {
//        ArrayList<ScreenInfo> screenInfos = new ArrayList<ScreenInfo>();
//        for (UpgradeRecord upgradeRecord : list) {
//            ScreenInfo screenInfo = new ScreenInfo();
//            screenInfo.setPackageName(upgradeRecord.getPid());
//            screenInfo.setFileName(upgradeRecord.getName());
//            screenInfo.setUpdateType("" + upgradeRecord.getStrategy());
//            screenInfo.setVersionName(upgradeRecord.getPVersionName());
//            screenInfo.setVersionCode("" + upgradeRecord.getPVersionCode());
//            screenInfo.setPluginUrl(upgradeRecord.getLocalUrl());
//            /**
//             * From server json:
//             description:{"description" :""}
//             updateComment:{"updateComment" :""}
//             viewName:{"viewName": [ { "lang": "zh-CN","content": "乐见" }, { "lang": "en-US", "content": "LeView" } ]}
//             */
//            screenInfo.setScreenTag(upgradeRecord.getViewName());// json
//            screenInfo.setUpdateInfo(upgradeRecord.getUpdateComment());
//            screenInfo.setDescribe(upgradeRecord.getDescription());
//            screenInfo.setMd5(upgradeRecord.getMd5());
//
//            screenInfo.setIsNew(true);
//            if (type == CHANGE_TYPE_ADD) {
//                screenInfo.setShowOnTab(false);
//                screenInfo.setRemovable(true);
//                screenInfo.setSortable(true);
//                screenInfo.setPluginState(ScreenInfo.PLUGIN_STATE_AVAILABLE);
//            }
//
//            Location location = upgradeRecord.getLocation();
//            if (location != null) {
//                /**
//                 * 如果插件在桌面管理里,没有使用过,根据标识把插件添加到tab上
//                 * 0:不执行添加 1:执行添加
//                 */
//                if (location.getAddTab() == Location.DO_ADD_TAB) {
//                    screenInfo.forceAddToTable = true;
//                }
//                /**
//                 * 调整已经在tab上的插件位置
//                 * 1:不执行调整 2:执行调整
//                 */
//                if (location.getAdjustTab() == Location.DO_ADJUST_TAB) {
//                    screenInfo.forceChangeTab = true;
//                }
//                screenInfo.setPosition(location.getTabPostion());
//            }
//
//            screenInfos.add(screenInfo);
//        }
//        LetvLog.i(TAG, " upgradeToScreenInfo result = " + screenInfos);
//        return screenInfos;
//    }
//
//    private List<ScreenInfo> updateDBInfo(List<ScreenInfo> updateList) {
//        List<ScreenInfo> matchedList = new ArrayList<ScreenInfo>();
//        List<ScreenInfo> fromDB = ScreenDBHelper.getInstance().getAllUsedPlugin();
//        for (ScreenInfo newScreenInfo : updateList) {
//            for (ScreenInfo screenFromDB : fromDB) {
//                if (screenFromDB.equals(newScreenInfo)) {
//                    screenFromDB.setPackageName(newScreenInfo.getPackageName());
//                    screenFromDB.setFileName(newScreenInfo.getFileName());
//                    screenFromDB.setUpdateType(newScreenInfo.getUpdateType());
//                    screenFromDB.setVersionName(newScreenInfo.getVersionName());
//                    screenFromDB.setVersionCode(newScreenInfo.getVersionCode());
//                    screenFromDB.setPluginUrl(newScreenInfo.getPluginUrl());
//                    screenFromDB.setScreenTag(newScreenInfo.getScreenTag());
//                    screenFromDB.setUpdateInfo(newScreenInfo.getUpdateInfo());
//                    screenFromDB.setDescribe(newScreenInfo.getDescribe());
//                    screenFromDB.setIsNew(newScreenInfo.getIsNew());
//                    screenFromDB.setMd5(newScreenInfo.getMd5());
//                    screenFromDB.setTagType(newScreenInfo.getTagType());
//                    screenFromDB.setIconUrl(newScreenInfo.getIconUrl());
//                    screenFromDB.setImageUrl(newScreenInfo.getImageUrl());
////                    screenFromDB.setShowOnTab(toChecked.getShowOnTab());
//                    matchedList.add(screenFromDB);
//                    break;
//                }
//            }
//        }
//        return matchedList;
//    }

    private void setPluginOffLine(List<String> list) {
        if (list == null) {
            return;
        }
        ArrayList<ScreenInfo> updateList = new ArrayList<ScreenInfo>(list.size());
        for (String s : list) {
            ScreenInfo screen = mDatamodel.getScreenByPackageName(s, false);
            if (screen != null) {
                screen.setPluginState(ScreenInfo.PLUGIN_STATE_OFFLINE);
                screen.setOfflineShot(ScreenInfo.OFFLINE_STATE_NOCHANGE);
                updateList.add(screen);
            }
        }
        mDatamodel.setPluginOffLine(updateList);
    }
}
