
package com.xstv.desktop.app.presenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.xstv.library.base.LetvLog;
import com.xstv.library.base.async.Job;
import com.xstv.library.base.async.JobType;
import com.xstv.library.base.async.ThreadPool;
import com.xstv.desktop.app.AppPluginActivator;
import com.xstv.desktop.app.bean.ContentBean;
import com.xstv.desktop.app.bean.FolderInfo;
import com.xstv.desktop.app.bean.PosterInfo;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.interfaces.DataChangeObserver;
import com.xstv.desktop.app.interfaces.PosterDataModelCallback;
import com.xstv.desktop.app.listener.DataChangeObservable;
import com.xstv.desktop.app.listener.OnDataChangeListener;
import com.xstv.desktop.app.model.AppDataModel;
import com.xstv.desktop.app.model.DataModelList;
import com.xstv.desktop.app.model.PosterDataModel;
import com.xstv.desktop.app.util.IconFilterUtil;
import com.xstv.desktop.app.util.Utilities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FragmentPresenter extends BasePresenter<OnDataChangeListener> implements AppDataModel.Callbacks, PosterDataModelCallback {
    private static final String TAG = FragmentPresenter.class.getSimpleName();

    /**
     * 每天下午4点更新
     */
    private static final int UPDATE_TIME = 16;

    private Handler handler = new Handler(Looper.getMainLooper());

    private DataChangeObservable mObservable = new DataChangeObservable();

    private boolean isLocalDataLoaded;
    private boolean isServerDataLoaded;

    private boolean isNotifyUI;
    private boolean isFetchedDataAfter16;

    private boolean isInitData;

    private long endTimeMillis = 0;

    public FragmentPresenter() {
        PosterDataModel.getInstance().setCallback(this);
        AppDataModel.getInstance().setCallbacks(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        AppPluginActivator.getContext().registerReceiver(mNetStateReceiver, filter);
    }

    public void loadData() {
        LetvLog.i(TAG, "loadData");
        if (isInitData) {
            Log.w(TAG, "loadData data is alerday init.");
            return;
        }
        if (isViewAttached()) {
            getView().onShowLoading();
        }
        if (isCacheInitData()) {
            //有缓存数据
            loadDataFromCache();
        } else {
            initData();
        }
        isInitData = true;
    }

    private void initData() {
        //加载本地数据
        AppDataModel.getInstance().startLoader(true);
        //获取海报数据
        PosterDataModel.getInstance().fetchPosterData(false);
    }

    private void loadDataFromCache() {
        LetvLog.d(TAG, "notifyDataFromCache");
        isLocalDataLoaded = true;
        isServerDataLoaded = true;
        if (Utilities.verifySupportSdk(Utilities.support_sdk_version_100)) {
            ThreadPool threadpool = ThreadPool.getInstance();
            Job job = new Job(AppPluginActivator.getContext().getPackageName(), JobType.JOB_DATA_INIT) {
                @Override
                public void run() {
                    createBlur(DataModelList.getInstance().allAppList);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyUI();
                        }
                    });
                }
            };
            threadpool.submit(job);
        } else {
            new Thread() {
                @Override
                public void run() {
                    createBlur(DataModelList.getInstance().allAppList);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyUI();
                        }
                    });
                }
            }.start();
        }
    }

    private boolean isCacheInitData() {
        return DataModelList.getInstance().allAppList.size() > 0 && DataModelList.getInstance().contentBeanList.size() > 0;
    }

    public void updateData() {
        if (!isNotifyUI || isFetchedDataAfter16 || (isNotifyUI && !isServerDataLoaded)) {
            LetvLog.i(TAG, "fetchData no need update Data");
            return;
        }

        if (isCurrentTimeAfter16()) {
            LetvLog.i(TAG, "fetchData start update Data.");
            PosterDataModel.getInstance().fetchPosterData(true);
        }
    }

    private boolean isCurrentTimeAfter16() {
        Calendar c = Calendar.getInstance();
        // 24小时制
        int hour = c.get(Calendar.HOUR_OF_DAY);
        //LetvLog.i(TAG, "isCurrentTimeAfter16 hour = " + hour);
        return hour >= UPDATE_TIME;
    }

    @Override
    public void onServerData(final List<ContentBean> contentBeanList, final boolean isUpdate) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                List<ContentBean> list = DataModelList.getInstance().contentBeanList;
                list.clear();
                list.addAll(contentBeanList);
                isServerDataLoaded = true;
                if (isUpdate) {
                    if (isViewAttached()) {
                        isFetchedDataAfter16 = isCurrentTimeAfter16();
                        getView().onNotifyUI(contentBeanList, true, null);
                    }
                } else {
                    notifyUI();
                }
            }
        };
        handler.post(r);
    }

    @Override
    public void bindApps(ArrayList<ItemInfo> itemInfos) {
        final List<ItemInfo> list = DataModelList.getInstance().allAppList;
        list.clear();
        list.addAll(itemInfos);
        //生成高斯模糊图片
        createBlur(itemInfos);
        //通知界面更新
        Runnable r = new Runnable() {
            @Override
            public void run() {
                isLocalDataLoaded = true;
                notifyUI();
            }
        };
        handler.post(r);
    }

    private void createBlur(List<ItemInfo> itemList) {
        if (!Utilities.isNeedBlur()) {
            return;
        }
        for (ItemInfo itemInfo : itemList) {
            if (itemInfo instanceof FolderInfo) {
                IconFilterUtil.createFolderItemIconBitmap((FolderInfo) itemInfo);
            } else {
                if (IconFilterUtil.isUsedTheme()) {
                    IconFilterUtil.createIconBitmap(itemInfo);
                } else {
                    IconFilterUtil.createIconAndBgBitmap(itemInfo);
                }
            }
        }
    }


    private void notifyUI() {
        LetvLog.i(TAG, "notifyUI isLocalDataLoaded = " + isLocalDataLoaded + " isServerDataLoaded = " + isServerDataLoaded);
        if (!isLocalDataLoaded) {
            return;
        }
        List<ContentBean> contentbeanList = null;
        if (!isServerDataLoaded) {
            contentbeanList = createNullPosterList();
        } else {
            contentbeanList = DataModelList.getInstance().contentBeanList;
        }
        isNotifyUI = true;
        isFetchedDataAfter16 = isCurrentTimeAfter16();
        if (isViewAttached()) {
            getView().onNotifyUI(contentbeanList, isServerDataLoaded, DataModelList.getInstance().allAppList);
        }
    }

    private List<ContentBean> createNullPosterList() {
        List<ContentBean> contentBeanList = new ArrayList<ContentBean>(2);
        for (int i = 0; i < 2; i++) {
            ContentBean contentBean = new ContentBean();
            contentBean.setTitle("title");
            contentBean.setPosid("posId");
            contentBean.setSubTitle("subTitle");

            List<ItemInfo> itemInfos = new ArrayList<>();
            PosterInfo post1 = new PosterInfo();
            post1.setFirstTitle("测试数据1");
            post1.setIconUrl("https://wx4.sinaimg.cn/mw690/006v3HKJgy1frdb1ff66cj318g270e81.jpg");
            post1.setLogoUrl("");
            itemInfos.add(post1);

            PosterInfo post2 = new PosterInfo();
            post2.setFirstTitle("测试数据2");
            post2.setIconUrl("https://wx4.sinaimg.cn/mw690/006v3HKJgy1frdb1eohg7j30jg163adt.jpg");
            post2.setLogoUrl("");
            itemInfos.add(post2);

            PosterInfo post3 = new PosterInfo();
            post3.setFirstTitle("测试数据3");
            post3.setIconUrl("https://wx3.sinaimg.cn/mw690/006v3HKJgy1frdb1epd5gj30jg0yl77v.jpg");
            post3.setLogoUrl("");
            itemInfos.add(post3);

            PosterInfo post4 = new PosterInfo();
            post4.setFirstTitle("测试数据4");
            post4.setIconUrl("https://wx3.sinaimg.cn/mw690/006v3HKJgy1frdb1ewudhj30jg0yldjr.jpg");
            post4.setLogoUrl("");
            itemInfos.add(post4);

            PosterInfo post5 = new PosterInfo();
            post5.setFirstTitle("测试数据5");
            post5.setIconUrl("https://wx1.sinaimg.cn/mw690/006v3HKJgy1frdb1ewnfij31kw2dcn8j.jpg");
            post5.setLogoUrl("");
            itemInfos.add(post5);

            contentBean.setContentItemList(itemInfos);
            contentBeanList.add(contentBean);
            if (i == 0) {
                break;
            }
        }
        return contentBeanList;
    }

    @Override
    public void bindAppAdded(ArrayList<ItemInfo> adds) {
        LetvLog.d(TAG, "bindShortcutAdded");
        if (mObservable != null) {
            mObservable.notifyAppAdded(adds);
        }
    }

    @Override
    public void bindAppRemoved(ArrayList<ItemInfo> removes, ArrayList<ItemInfo> removeContainFolder) {
        LetvLog.d(TAG, "bindShortcutRemoved removes = " + removes);
        if (mObservable != null) {
            mObservable.notifyAppRemoved(removes, removeContainFolder);
        }
    }

    @Override
    public void bindAppUpdated(ArrayList<ItemInfo> updates, ArrayList<ItemInfo> updateContainFolder) {
        LetvLog.d(TAG, "bindShortcutUpdated: ");
        if (mObservable != null) {
            mObservable.notifyAppUpdated(updates, updateContainFolder);
        }
    }

    @Override
    public boolean setLoadOnResume() {
        return false;
    }

    @Override
    public void notifySuperscript(ItemInfo itemInfo, FolderInfo folderInfo) {
        LetvLog.d(TAG, "notifySuperscript itemInfo = " + itemInfo + " folderInfo = " + folderInfo);
        if (mObservable != null) {
            mObservable.notifySuperscriptChange(itemInfo, folderInfo);
        }
    }

    @Override
    public void notifyStateChange(ItemInfo itemInfo) {
        if (itemInfo == null) {
            LetvLog.e(TAG, "notifyStateChange itemInfo is null.");
            return;
        }

        /*DownloadStatusBean downloadStatusBean = itemInfo.getDownloadStatusBean();
        if (downloadStatusBean == null) {
            LetvLog.e(TAG, "notifyStateChange downloadStatusBean is null, this is error.");
            return;
        }

        String packageName = itemInfo.getPackageName();*/

        /*List<ItemInfo> posterItemList = DownloadAppPresenter.getInstance().getExistPreload(packageName);
        ItemInfo cibnItemInfo = DownloadAppPresenter.getInstance().getExistCibnPreload(packageName);
        if((posterItemList == null || posterItemList.size() == 0) && cibnItemInfo == null){
            Log.w(TAG, "notifyStateChange not update.");
            return;
        }

        String downState = downloadStatusBean.getDownloadStatus();
        boolean isStop = updateSweepAngle(itemInfo);
        float sweepAngle = downloadStatusBean.getSweepAngle();
        LetvLog.i(TAG, "notifyStateChange packageName = " + packageName + " loadingTitle = "
                + downloadStatusBean.getLoadingTitle()  + " sweepAngle = " + sweepAngle
                + " ( " + downloadStatusBean.getCurrentBytes() + " / " + downloadStatusBean.getTotalBytes() + " )");

        int delay = 800;
        if (downloadStatusBean.getTotalBytes() > 50 * 1024 * 1024) {
            delay = 1000;
        }

        if (System.currentTimeMillis() - endTimeMillis > delay || DownloadAppPresenter.STATE_RESET == downState
                || DownloadAppPresenter.STATE_INSTALLED == downState || DownloadAppPresenter.STATE_INSTALLING == downState
                || (DownloadAppPresenter.STATE_DOWNLOADING == downState && sweepAngle == 0)) {
            if(posterItemList != null && posterItemList.size() > 0){
                //说明有海报位要显示下载进度
                for (ItemInfo info : posterItemList) {
                    updateDownloadStatus(info, downloadStatusBean);
                }
            }

            if(cibnItemInfo != null){
                updateDownloadStatus(cibnItemInfo, downloadStatusBean);
            }

            FolderInfo cibnInFolderInfo = null;
            List<ItemInfo> allAppList = DataModelList.getInstance().allAppList;
            if (allAppList != null && allAppList.size() > 0) {
                for (ItemInfo cached : allAppList) {
                    boolean isFlag = false;
                    if (cached instanceof FolderInfo) {
                        FolderInfo folderInfo = (FolderInfo) cached;
                        ArrayList<ItemInfo> contents = folderInfo.getContents();
                        for (ItemInfo content : contents) {
                            if (content != null && packageName.equals(content.getPackageName())) {
                                updateDownloadStatus(folderInfo, downloadStatusBean);
                                cibnInFolderInfo = folderInfo;
                                isFlag = true;
                                break;
                            }
                        }
                    }
                    if (isFlag) {
                        break;
                    }
                }
            }

            if (mObservable != null) {
                mObservable.notifyStateChange(posterItemList, cibnItemInfo, cibnInFolderInfo);
            }

            endTimeMillis = System.currentTimeMillis();
        }*/
    }

    /*private void updateDownloadStatus(ItemInfo itemInfo, DownloadStatusBean statusBean){
        DownloadStatusBean updateDownloadStatusBean = itemInfo.getDownloadStatusBean();
        if(updateDownloadStatusBean == null){
            updateDownloadStatusBean = new DownloadStatusBean();
            itemInfo.setDownloadStatusBean(updateDownloadStatusBean);
        }
        updateDownloadStatusBean.setDownloadStatus(statusBean.getDownloadStatus());
        updateDownloadStatusBean.setSweepAngle(statusBean.getSweepAngle());
        updateDownloadStatusBean.setLoadingTitle(statusBean.getLoadingTitle());
        updateDownloadStatusBean.setDownloadId(statusBean.getDownloadId());
    }*/

    /*private boolean updateSweepAngle(ItemInfo itemInfo) {
        boolean isStop = false;
        DownloadStatusBean downloadStatusBean = itemInfo.getDownloadStatusBean();
        String downState = downloadStatusBean.getDownloadStatus();
        long totalBytes = downloadStatusBean.getTotalBytes();
        long currentBytes = downloadStatusBean.getCurrentBytes();
        if (DownloadAppPresenter.STATE_DOWNLOADING.equals(downState)) {
            float percent = (currentBytes * 1.0f) / totalBytes;
            if (percent <= 0) {
                percent = 0;
            } else if (percent > 1) {
                percent = 1;
            }
            float sweepAngle = 180 * percent;
            downloadStatusBean.setSweepAngle(sweepAngle);
        } else if (DownloadAppPresenter.STATE_INSTALLING.equals(downState)) {

        } else if (DownloadAppPresenter.STATE_INSTALLED.equals(downState)) {
            downloadStatusBean.setSweepAngle(360);
            isStop = true;
        } else if (DownloadAppPresenter.STATE_RESET.equals(downState)) {
            downloadStatusBean.setSweepAngle(0);
            isStop = true;
        }

        return isStop;
    }*/

    public final boolean hasObservers() {
        return mObservable != null && mObservable.hasObservers();
    }

    public void registerDataObserver(WeakReference<DataChangeObserver> observer) {
        if (mObservable != null) {
            mObservable.registerDataObserver(observer);
        }
    }

    public synchronized void unregisterDataObserver(WeakReference<DataChangeObserver> observer) {
        if (mObservable != null) {
            mObservable.unregisterDataObserver(observer);
        }
    }

    public void release() {
        detachView();
        AppDataModel.getInstance().setCallbacks(null);
        PosterDataModel.getInstance().setCallback(null);
        handler.removeCallbacksAndMessages(null);
        if (mObservable != null) {
            mObservable.unregisterAll();
        }
        mObservable = null;
        AppPluginActivator.getContext().unregisterReceiver(mNetStateReceiver);
    }

    public void crush() {
        AppDataModel.getInstance().setCallbacks(null);
        AppDataModel.getInstance().crush();
        PosterDataModel.getInstance().setCallback(null);
        DataModelList.getInstance().crush();
        PosterDataModel.getInstance().crush();
    }

    private BroadcastReceiver mNetStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LetvLog.i(TAG, "onReceive intent = " + intent + " action = " + action + " isNotifyUI = " + isNotifyUI);
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action) && isNotifyUI && Utilities.isNetworkConnected(context)) {
                updateData();
            }
        }
    };
}
