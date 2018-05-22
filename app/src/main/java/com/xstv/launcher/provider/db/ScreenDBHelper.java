
package com.xstv.launcher.provider.db;

import android.text.TextUtils;

import com.xstv.base.LetvLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

public class ScreenDBHelper implements DaoHelperInterface<ScreenInfo> {
    private static String TAG = ScreenDBHelper.class.getSimpleName();
    private static ScreenDBHelper sInstance;
    private ScreenInfoDao mScreenInfoDao;

    private ScreenDBHelper() {
        mScreenInfoDao = DatabaseLoader.getInstance().getDaoSession().getScreenInfoDao();
    }

    public static ScreenDBHelper getInstance() {
        if (sInstance == null) {
            synchronized (ScreenDBHelper.class) {
                if (sInstance == null) {
                    sInstance = new ScreenDBHelper();
                }
            }
        }
        return sInstance;
    }

    @Override
    public long insert(ScreenInfo insert) {
        if (insert != null) {
            LetvLog.i(TAG, "insert :" + insert.toString());
            return mScreenInfoDao.insert(insert);
        }
        return -1;
    }

    @Override
    public long insertOrReplace(ScreenInfo insert) {
        if (insert != null) {
            LetvLog.i(TAG, "insertOrReplace :" + insert.toString());
            return mScreenInfoDao.insertOrReplace(insert);
        }
        return -1;
    }

    /**
     * Inserts the given entities in the database using a transaction.
     *
     * @param entities
     */
    public void insertInTx(List<ScreenInfo> entities) {
        if (entities != null) {
            LetvLog.i(TAG, "insertInTx");
            printArray(entities);
            mScreenInfoDao.insertInTx(entities);
        }
    }

    public void inserOrReplaceInTx(List<ScreenInfo> entities) {
        if (entities != null) {
            LetvLog.i(TAG, "inserOrReplaceInTx");
            printArray(entities);
            mScreenInfoDao.insertOrReplaceInTx(entities);
        }
    }

    @Override
    public boolean delete(Long id) {
        LetvLog.i(TAG, "delete id :" + id);
        mScreenInfoDao.deleteByKey(id);
        return true;
    }

    public void deleteInTx(List<ScreenInfo> entities) {
        if (entities != null) {
            LetvLog.i(TAG, "deleteInTx");
            printArray(entities);
            mScreenInfoDao.deleteInTx(entities);
        }
    }

    @Override
    public ScreenInfo getById(Long id) {
        return mScreenInfoDao.load(id);
    }

    /**
     * If you only want get used plugin please call {@link #getAllUsedPlugin()}
     *
     * @return
     */
    public ArrayList<ScreenInfo> getAll() {
        ArrayList<ScreenInfo> all;
        QueryBuilder<ScreenInfo> qb = mScreenInfoDao.queryBuilder();
        qb.orderAsc(ScreenInfoDao.Properties.ScreenOrder);
        all = (ArrayList<ScreenInfo>) qb.list();
        return all;
    }

    /**
     * Get all used plugin ,if plugin state is off_line except it.
     *
     * @return
     */
    public ArrayList<ScreenInfo> getAllUsedPlugin() {
        ArrayList<ScreenInfo> all;
        QueryBuilder<ScreenInfo> qb = mScreenInfoDao.queryBuilder()
                .where(ScreenInfoDao.Properties.PluginState.notEq(ScreenInfo.PLUGIN_STATE_OFFLINE))
                .orderAsc(ScreenInfoDao.Properties.ScreenOrder);
        all = (ArrayList<ScreenInfo>) qb.list();
        return all;
    }

    @Override
    public boolean hasKey(Long id) {
        return false;
    }

    @Override
    public long getTotalCount() {
        QueryBuilder<ScreenInfo> qb = mScreenInfoDao.queryBuilder();
        return qb.buildCount().count();
    }

    @Override
    public boolean deleteAll() {
        mScreenInfoDao.deleteAll();
        return true;
    }

    @Override
    public boolean update(ScreenInfo update) {
        LetvLog.i(TAG, "update :" + update.toString());
        mScreenInfoDao.update(update);
        return true;
    }

    public void updateInTx(List<ScreenInfo> entities) {
        if (entities != null) {
            mScreenInfoDao.updateInTx(entities);
        }
    }

    /**
     * Get show on tab screen
     *
     * @return
     */
    public ArrayList<ScreenInfo> getShowOnTabList() {
        ArrayList<ScreenInfo> loadList;
        QueryBuilder<ScreenInfo> qb = mScreenInfoDao.queryBuilder()
                .where(ScreenInfoDao.Properties.PluginState.notEq(ScreenInfo.PLUGIN_STATE_OFFLINE), ScreenInfoDao.Properties.ShowOnTab.eq(true))
                .orderAsc(ScreenInfoDao.Properties.ScreenOrder);
        loadList = (ArrayList<ScreenInfo>) qb.list();
        return loadList;
    }

    /**
     * Get all tab to show in manager（桌面管理） ui.<br/>
     * Show on tab order by {@link ScreenInfo#getScreenOrder()}.<br/>
     * Not show tab order by {@link ScreenInfo#getHot()}
     *
     * @return
     */
    public ArrayList<ScreenInfo> getAllTabList() {
        ArrayList<ScreenInfo> loadList = new ArrayList<ScreenInfo>();
        loadList.addAll(getShowOnTabList());
        loadList.addAll(getNotShownOnTabList());
        return loadList;
    }

    /**
     * Get NOT show on tab screen
     *
     * @return
     */
    public ArrayList<ScreenInfo> getNotShownOnTabList() {
        ArrayList<ScreenInfo> loadList = new ArrayList<ScreenInfo>();
        QueryBuilder<ScreenInfo> qb = mScreenInfoDao.queryBuilder()
                .where(ScreenInfoDao.Properties.PluginState.notEq(ScreenInfo.PLUGIN_STATE_OFFLINE), ScreenInfoDao.Properties.ShowOnTab.eq(false))
                .orderDesc(ScreenInfoDao.Properties.Hot);
        if (qb.list() != null) {
            loadList.addAll(qb.list());
        }
        return loadList;
    }

    /**
     * Get show on tab screen（Used by user now） package name.
     *
     * @return
     */
    public ArrayList<String> getShowOnTabPackageName() {
        ArrayList<String> packageNameList = new ArrayList<String>();
        ArrayList<ScreenInfo> showList = getShowOnTabList();
        for (ScreenInfo screenInfo : showList) {
            packageNameList.add(screenInfo.getPackageName());
        }
        return packageNameList;
    }

    public void deleteTestData(String describe) {
        LetvLog.i(TAG, " delete Test Data by describe :" + describe);
        QueryBuilder<ScreenInfo> qb = mScreenInfoDao.queryBuilder()
                .where(ScreenInfoDao.Properties.Describe.eq(describe));
        List<ScreenInfo> deleteList = qb.list();
        mScreenInfoDao.deleteInTx(deleteList);
    }

    public ArrayList<ScreenInfo> getLockedList() {
        LetvLog.i(TAG, " getLockedList");
        ArrayList<ScreenInfo> loadList = new ArrayList<ScreenInfo>();
        QueryBuilder<ScreenInfo> qb = mScreenInfoDao.queryBuilder()
                .where(ScreenInfoDao.Properties.Locked.eq(true));
        if (qb.list() != null) {
            loadList.addAll(qb.list());
        }
        return loadList;
    }

    public void updatePluginState(List<String> packageNameList, String pluginState) {
        LetvLog.i(TAG, " updatePluginState list =  " + packageNameList + ", pluginState = " + pluginState);
        QueryBuilder<ScreenInfo> qb = mScreenInfoDao.queryBuilder()
                .orderAsc(ScreenInfoDao.Properties.ScreenOrder);
        ArrayList<ScreenInfo> all = (ArrayList<ScreenInfo>) qb.list();
        for (ScreenInfo screenInfo : all) {
            for (String packageName : packageNameList) {
                if (packageName != null && packageName.equals(screenInfo.getPackageName())) {
                    screenInfo.setPluginState(pluginState);
                    break;
                }
            }
        }
        updateInTx(all);
    }

    public void updatePluginState(String packageName, String pluginState) {
        LetvLog.i(TAG, " updatePluginState " + packageName + " pluginState = " + pluginState);
        ArrayList<ScreenInfo> screenInfos = getScreenByPackageName(packageName);
        if (screenInfos != null) {
            for (ScreenInfo screenInfo : screenInfos) {
                screenInfo.setPluginState(pluginState);
            }
            updateInTx(screenInfos);
        }
        LetvLog.i(TAG, " updatePluginState screenInfo = " + screenInfos);
    }

    public ArrayList<ScreenInfo> getScreenByPackageName(String packageName) {
        LetvLog.i(TAG, " getScreenByPackageName " + packageName);
        if (packageName == null) {
            LetvLog.e(TAG, " getScreenByPackageName Error! ");
            return null;
        }

        QueryBuilder<ScreenInfo> qb = mScreenInfoDao.queryBuilder()
                .where(ScreenInfoDao.Properties.PackageName.eq(packageName));
        ArrayList<ScreenInfo> all = (ArrayList<ScreenInfo>) qb.list();
        LetvLog.i(TAG, " getScreenByPackageName get from db = " + all);
        return all;
    }

    public ArrayList<ScreenInfo> getCrashScreenList() {
        LetvLog.i(TAG, "getCrashScreenList");
        ArrayList<ScreenInfo> loadList = new ArrayList<ScreenInfo>();
        QueryBuilder<ScreenInfo> qb = mScreenInfoDao.queryBuilder()
                .where(ScreenInfoDao.Properties.PluginState.eq(ScreenInfo.PLUGIN_STATE_CRASH));
        if (qb.list() != null) {
            loadList.addAll(qb.list());
        }
        return loadList;
    }

    public void initPluginState() {
        ArrayList<ScreenInfo> all = getAll();
        ArrayList<ScreenInfo> changedList = new ArrayList<ScreenInfo>(9);
        if (all != null && all.size() > 0) {
            Iterator<ScreenInfo> iterator = all.iterator();
            while (iterator.hasNext()) {
                ScreenInfo screen = iterator.next();
                if (TextUtils.isEmpty(screen.getPluginState())) {
                    screen.setPluginState(ScreenInfo.PLUGIN_STATE_AVAILABLE);
                    changedList.add(screen);
                }
            }
        }
        if (changedList.size() > 0) {
            mScreenInfoDao.updateInTx(changedList);
        }
    }

    public ArrayList<ScreenInfo> getOffLineStateScreenList() {
        LetvLog.i(TAG, " getOffLineStateScreenList");
        ArrayList<ScreenInfo> loadList = new ArrayList<ScreenInfo>(5);
        QueryBuilder<ScreenInfo> qb = mScreenInfoDao.queryBuilder()
                .where(ScreenInfoDao.Properties.PluginState.eq(ScreenInfo.PLUGIN_STATE_OFFLINE));
        if (qb.list() != null) {
            loadList.addAll(qb.list());
        }
        printArray(loadList);
        return loadList;
    }

    private void printArray(List<ScreenInfo> list) {
        LetvLog.i(TAG, "---------printArray begin--------");
        for (ScreenInfo screen : list) {
            LetvLog.i(TAG, "---> " + screen);
        }
        LetvLog.i(TAG, "---------printArray end--------");
    }

    /**
     * getNotUesdList : hasUsed false and showOnTab false
     *
     * @return
     */
    public ArrayList<ScreenInfo> getNotUesdList() {
        LetvLog.i(TAG, " getNotUesdList hasUsed false and showOnTab false");
        ArrayList<ScreenInfo> notUsedList = new ArrayList<ScreenInfo>(5);
        QueryBuilder<ScreenInfo> qb = mScreenInfoDao.queryBuilder()
                .where(ScreenInfoDao.Properties.HasUsed.eq(false), ScreenInfoDao.Properties.ShowOnTab.eq(false));
        if (qb.list() != null) {
            notUsedList.addAll(qb.list());
        }
        printArray(notUsedList);
        return notUsedList;
    }

    /**
     * Get user has used but not show on table.
     *
     * @return
     */
    public ArrayList<ScreenInfo> getUserNotCareList() {
        LetvLog.i(TAG, " getUserNotCareList ");
        ArrayList<ScreenInfo> notCareList = new ArrayList<ScreenInfo>(5);
        QueryBuilder<ScreenInfo> qb = mScreenInfoDao.queryBuilder()
                .where(ScreenInfoDao.Properties.HasUsed.eq(true), ScreenInfoDao.Properties.ShowOnTab.eq(false));
        if (qb.list() != null) {
            notCareList.addAll(qb.list());
        }
        printArray(notCareList);
        return notCareList;
    }

    /**
     * 把某个插件放在指定的位置
     *
     * @param screenInfo
     * @param order
     */
    public void changePosition(ScreenInfo screenInfo, int order) {
        LetvLog.i(TAG, " changePosition toPosition = " + order + " " + screenInfo);
        if (screenInfo == null) {
            return;
        }
        QueryBuilder<ScreenInfo> qb = mScreenInfoDao.queryBuilder()
                .where(ScreenInfoDao.Properties.ShowOnTab.eq(true), ScreenInfoDao.Properties.PackageName.notEq(screenInfo.getPackageName())).orderAsc(ScreenInfoDao.Properties.ScreenOrder);
        List<ScreenInfo> ss = qb.list();
        LetvLog.i(TAG, " changePosition, before change position ");
        printArray(ss);
        if (ss != null) {
            ss.add(order, screenInfo);
            int i = 0;
            for (ScreenInfo s : ss) {
                s.setScreenOrder(i);
                i++;
            }
            LetvLog.i(TAG, " changePosition, after change position ");
            printArray(ss);
            updateInTx(ss);
        }
    }
}
