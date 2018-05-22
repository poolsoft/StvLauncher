
package com.xstv.desktop.app.db;

import android.os.SystemClock;

import com.xstv.base.LetvLog;
import com.xstv.desktop.app.bean.FolderInfo;
import com.xstv.desktop.app.model.AppDataModel;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.Query;

public class ItemInfoDBHelper implements DaoHelperInterface<ItemInfo> {

    private static final String TAG = ItemInfoDBHelper.class.getSimpleName();
    private static ItemInfoDBHelper sInstance;
    private ItemInfoDao mItemInfoDao;

    private ItemInfoDBHelper() {
        mItemInfoDao = DatabaseLoader.getInstance().getDaoSession().getItemInfoDao();
    }

    public static ItemInfoDBHelper getInstance() {
        synchronized (ItemInfoDBHelper.class) {
            if (sInstance == null) {
                synchronized (ItemInfoDBHelper.class) {
                    sInstance = new ItemInfoDBHelper();
                }
            }
        }
        return sInstance;
    }

    @Override
    public long insert(ItemInfo insert) {
        LetvLog.d(TAG, " insert " + insert);
        if (insert != null) {
            return mItemInfoDao.insert(insert);
        }
        return -1;
    }

    @Override
    public long insertOrReplace(ItemInfo insert) {
        LetvLog.i(TAG, " insertOrReplace " + insert);
        long id = -1;
        if (insert != null) {
            Query<ItemInfo> query = mItemInfoDao.queryBuilder()
                    .where(ItemInfoDao.Properties.ComponentNameStr.eq(insert.getComponentNameStr()))
                    .build();
            List<ItemInfo> matchedList = query.list();
            if (matchedList != null && matchedList.size() > 0) {
                ItemInfo info = matchedList.get(0);
                insert.setId(info.getId());
                insert.setIndex(info.getIndex());
                mItemInfoDao.update(insert);
                LetvLog.i(TAG, " insertOrReplace update ");
            } else {
                id = mItemInfoDao.insert(insert);
                LetvLog.i(TAG, " insertOrReplace insert ");
            }
        }
        return id;
    }

    /**
     * Inserts the given entities in the database using a transaction.
     *
     * @param entities
     */
    public void insertInTx(List<ItemInfo> entities) {
        if (entities != null) {
            mItemInfoDao.insertInTx(entities);
        }
    }

    @Override
    public boolean delete(Long id) {
        LetvLog.d(TAG, " delete id = " + id);
        if (id != null && id > 0) {
            mItemInfoDao.deleteByKey(id);
            LetvLog.d(TAG, " delete success ");
            return true;
        }
        LetvLog.d(TAG, " delete fail ");
        return false;
    }

    public boolean delete(ItemInfo delete) {
        LetvLog.i(TAG, " delete " + delete);
        if (delete != null) {
            if (delete.getId() != null && delete.getId() > -1) {
                return delete(delete.getId());
            } else {
                Query<ItemInfo> query = mItemInfoDao.queryBuilder()
                        .where(ItemInfoDao.Properties.ComponentNameStr.eq(delete.getComponentNameStr()))
                        .build();
                List<ItemInfo> matchedList = query.list();
                if (matchedList != null && matchedList.size() > 0) {
                    mItemInfoDao.deleteInTx(matchedList);
                }
                return true;
            }
        }
        return false;
    }

    public void deleteInTx(List<ItemInfo> deletes) {
        if (deletes != null && deletes.size() > 0) {
            mItemInfoDao.deleteInTx(deletes);
        }
    }

    @Override
    public ItemInfo getById(Long id) {
        return mItemInfoDao.load(id);
    }

    /**
     * Get all shortcut and folder ,But not remove has in folder
     *
     * @see ItemInfoDBHelper#getAllItemAndFolder(boolean)
     */
    @Override
    public List<ItemInfo> getAll() {
        long beginTime = SystemClock.uptimeMillis();
        List<ItemInfo> itemInfos = getAllItemAndFolder(false);
        LetvLog.i(TAG, " getAll from db use " + (SystemClock.uptimeMillis() - beginTime) + " ms");
        return itemInfos;
    }

    /**
     * Get all ItemInfo and folder
     *
     * @param removeHasInFolder : If true remove has in folder shortcut
     * @return
     */
    public List<ItemInfo> getAllItemAndFolder(boolean removeHasInFolder) {
        Query<ItemInfo> query = mItemInfoDao.queryBuilder().orderAsc(ItemInfoDao.Properties.Index)
                .build();
        List<ItemInfo> all = query.list();
        List<ItemInfo> hasInFolder = null;
        if (removeHasInFolder) {
            hasInFolder = new ArrayList<ItemInfo>();
        }

        List<ItemInfo> removeList = new ArrayList<ItemInfo>(3);
        for (int i = 0; i < all.size(); i++) {
            ItemInfo s = all.get(i);
            LetvLog.d(TAG, "getAllItemAndFolder ItemInfo = " + s);
            if (s.getType() == AppDataModel.ITEM_TYPE_FOLDER) {
                FolderInfo folder = new FolderInfo();
                folder.addAll(getFolderShortsByID(s.getId()));
                folder.setTitle(s.getTitle());
                folder.setId(s.getId());
                folder.setIndex(s.getIndex());
                folder.setFolder_id(s.getFolder_id());
                if (folder.getLength() == 0) {
                    LetvLog.i(TAG, "getAllItemAndFolder folder = " + folder);
                    removeList.add(s);
                } else {
                    if (hasInFolder != null) {
                        hasInFolder.addAll(folder.getContents());
                    }
                    all.set(i, folder);
                }
            }
        }
        all.removeAll(removeList);
        deleteInTx(removeList);

        if (hasInFolder != null) {
            all.removeAll(hasInFolder);
        }
        return all;
    }

    public List<ItemInfo> getItemAndFolderFromIndex(int index) {
        Query<ItemInfo> query = mItemInfoDao.queryBuilder().where(ItemInfoDao.Properties.Index.ge(index)).orderAsc(ItemInfoDao.Properties.Index)
                .build();
        List<ItemInfo> all = query.list();
        List<ItemInfo> hasInFolder = new ArrayList<ItemInfo>();
        List<ItemInfo> removeList = new ArrayList<ItemInfo>(3);
        for (int i = 0; i < all.size(); i++) {
            ItemInfo s = all.get(i);
            if (s.getType() == AppDataModel.ITEM_TYPE_FOLDER) {
                FolderInfo folder = new FolderInfo();
                folder.addAll(getFolderShortsByID(s.getId()));
                folder.setTitle(s.getTitle());
                folder.setId(s.getId());
                folder.setIndex(s.getIndex());
                folder.setFolder_id(s.getFolder_id());
                if (folder.getLength() == 0) {
                    removeList.add(s);
                } else {
                    if (hasInFolder != null) {
                        hasInFolder.addAll(folder.getContents());
                    }
                    all.set(i, folder);
                }
            }
        }
        all.removeAll(removeList);
        deleteInTx(removeList);
        all.removeAll(hasInFolder);
        return all;
    }

    public List<ItemInfo> getAllItem() {
        Query<ItemInfo> query = mItemInfoDao.queryBuilder().orderAsc(ItemInfoDao.Properties.Index)
                .where(ItemInfoDao.Properties.Type.eq(AppDataModel.ITEM_TYPE_APPLICATION))
                .build();
        return query.list();
    }

    public List<ItemInfo> getAllItemByType(int type) {
        Query<ItemInfo> query = mItemInfoDao.queryBuilder().orderAsc(ItemInfoDao.Properties.Index)
                .where(ItemInfoDao.Properties.Type.eq(type))
                .build();
        return query.list();
    }

    public List<ItemInfo> getRecentItemInfo(int limit) {
        Query<ItemInfo> query = mItemInfoDao.queryBuilder().orderDesc(ItemInfoDao.Properties.OrderTimestamp)
                .where(ItemInfoDao.Properties.Type.notEq(AppDataModel.ITEM_TYPE_FOLDER),
                        ItemInfoDao.Properties.OrderTimestamp.isNotNull())
                .limit(limit)
                .build();
        return query.list();
    }

    public ArrayList<FolderInfo> getAllFolder() {
        ArrayList<FolderInfo> allFloder = new ArrayList<FolderInfo>();
        Query<ItemInfo> query = mItemInfoDao.queryBuilder().orderAsc(ItemInfoDao.Properties.Index)
                .where(ItemInfoDao.Properties.Type.eq(AppDataModel.ITEM_TYPE_FOLDER))
                .build();
        List<ItemInfo> back = query.list();
        if (back != null && back.size() > 0) {
            for (ItemInfo folderInfo : back) {
                FolderInfo folder = new FolderInfo();
                folder.addAll(getFolderShortsByID(folderInfo.getId()));
                folder.setTitle(folderInfo.getTitle());
                folder.setId(folderInfo.getId());
                folder.setIndex(folderInfo.getIndex());
                folder.setFolder_id(folderInfo.getFolder_id());
                allFloder.add(folder);
            }
        }
        return allFloder;
    }

    public List<ItemInfo> getFolderShortsByID(long floderID) {
        Query<ItemInfo> query = mItemInfoDao.queryBuilder().orderAsc(ItemInfoDao.Properties.InFolderIndex).where(ItemInfoDao.Properties.Container.eq(floderID))
                .build();
        return query.list();
    }

    public ItemInfo getFolderInfoByFolderId(String folder_id) {
        Query<ItemInfo> query = mItemInfoDao.queryBuilder().orderAsc(ItemInfoDao.Properties.Index)
                .where(ItemInfoDao.Properties.Folder_id.eq(folder_id))
                .build();
        return query.unique();
    }

    @Override
    public boolean hasKey(Long id) {
        return false;
    }

    @Override
    public long getTotalCount() {
        return mItemInfoDao.count();
    }

    @Override
    public boolean deleteAll() {
        mItemInfoDao.deleteAll();
        return true;
    }

    /**
     * Update data but except index,Only compare getComponentNameStr .
     *
     * @param update
     * @return
     */
    @Override
    public boolean update(ItemInfo update) {
        LetvLog.i(TAG, " update " + update);
        if (update != null) {
            if (update.getId() != null && (update.getId() > -1)) {
                mItemInfoDao.update(update);
                return true;
            } else {
                Query<ItemInfo> query = mItemInfoDao.queryBuilder()
                        .where(ItemInfoDao.Properties.ComponentNameStr.eq(update.getComponentNameStr()))
                        .build();
                List<ItemInfo> matchedList = query.list();
                if (matchedList != null && matchedList.size() > 0) {
                    ItemInfo info = matchedList.get(0);
                    update.setId(info.getId());
                    update.setIndex(info.getIndex());
                    mItemInfoDao.update(update);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Only compare package
     *
     * @param update
     */
    public void updateFromSystem(ItemInfo update) {
        LetvLog.i(TAG, " updateFromSystem update = " + update);
        if (update != null && update.getPackageName() != null) {
            Query<ItemInfo> query = mItemInfoDao.queryBuilder()
                    .where(ItemInfoDao.Properties.PackageName.eq(update.getPackageName()),
                            ItemInfoDao.Properties.ClassName.eq(update.getClassName()))
                    .build();
            List<ItemInfo> matchedList = query.list();
            if (matchedList != null && matchedList.size() > 0) {
                ItemInfo info = matchedList.get(0);
                update.setId(info.getId());
                update.setIndex(info.getIndex());
                update.setContainer(info.getContainer());
                update.setContainerName(info.getContainerName());
                update.setInFolderIndex(info.getInFolderIndex());
                mItemInfoDao.update(update);
            }
        }
    }

    public void updateInTx(List<ItemInfo> updateList) {
        long beginTime = SystemClock.uptimeMillis();
        if (updateList != null) {
            mItemInfoDao.updateInTx(updateList);
        } else {
            LetvLog.e(TAG, " updateInTx " + updateList);
        }
        LetvLog.i(TAG, " updateInTx use " + (SystemClock.uptimeMillis() - beginTime) + " ms");
    }

    public List<ItemInfo> getShortByPkg(String packageName) {
        Query<ItemInfo> query = mItemInfoDao.queryBuilder()
                .where(ItemInfoDao.Properties.PackageName.eq(packageName))
                .build();
        return query.list();
    }

    public ItemInfo getShortByComponent(String ComponentName) {
        ItemInfo itemInfo = null;
        if (ComponentName != null) {
            Query<ItemInfo> query = mItemInfoDao.queryBuilder()
                    .where(ItemInfoDao.Properties.ComponentNameStr.eq(ComponentName))
                    .build();
            List<ItemInfo> matchedList = query.list();
            if (matchedList != null && matchedList.size() > 0) {
                itemInfo = matchedList.get(0);
            }
        }
        return itemInfo;
    }

    public ItemInfo getByPackageAndClass(String pkg, String className) {
        ItemInfo itemInfo = null;
        LetvLog.d(TAG, " getByPackageAndClass pkg = " + pkg + " className = " + className);
        if (pkg != null && className != null) {
            Query<ItemInfo> query = mItemInfoDao.queryBuilder()
                    .where(ItemInfoDao.Properties.PackageName.eq(pkg), ItemInfoDao.Properties.ClassName.eq(className))
                    .build();
            List<ItemInfo> matchedList = query.list();
            if (matchedList != null && matchedList.size() > 0) {
                itemInfo = matchedList.get(0);
            }
        }
        return itemInfo;
    }

    public int getLastIndex() {
        int index = (int) mItemInfoDao.count();
        LetvLog.i(TAG, " getLastIndex get count = " + index);
        Query<ItemInfo> query = mItemInfoDao.queryBuilder().orderDesc(ItemInfoDao.Properties.Index)
                .build();
        List<ItemInfo> all = query.list();
        if (all != null && all.size() > 0) {
            index = all.get(0).getIndex();
        }
        LetvLog.i(TAG, " getLastIndex index = " + index);
        return index;
    }

    public List<ItemInfo> getShortByIntentUrl(String intentUrl) {
        Query<ItemInfo> query = mItemInfoDao.queryBuilder()
                .where(ItemInfoDao.Properties.ShortcutIntentUrl.eq(intentUrl))
                .build();
        return query.list();
    }

}
