
package com.xstv.desktop.app.listener;

import com.xstv.base.LetvLog;
import com.xstv.desktop.app.bean.FolderInfo;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.interfaces.DataChangeObserver;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DataChangeObservable {

    private static final String TAG = DataChangeObservable.class.getSimpleName();

    public List<WeakReference<DataChangeObserver>> mObservers = new LinkedList<WeakReference<DataChangeObserver>>();

    public boolean hasObservers() {
        return !mObservers.isEmpty();
    }

    public void notifyAppAdded(ArrayList<ItemInfo> adds) {
        for (int i = mObservers.size() - 1; i >= 0; --i) {
            WeakReference<DataChangeObserver> ref = mObservers.get(i);
            if (ref != null && ref.get() != null) {
                ref.get().onAppAdded(adds);
            }
        }
    }

    public void notifyAppRemoved(ArrayList<ItemInfo> removes, ArrayList<ItemInfo> removeWidthFolderList) {
        for (int i = mObservers.size() - 1; i >= 0; --i) {
            WeakReference<DataChangeObserver> ref = mObservers.get(i);
            if (ref != null && ref.get() != null) {
                ref.get().onAppRemoved(removes, removeWidthFolderList);
            }
        }

    }

    public void notifyAppUpdated(ArrayList<ItemInfo> updates, ArrayList<ItemInfo> updateWidthFolderList) {
        for (int i = mObservers.size() - 1; i >= 0; --i) {
            WeakReference<DataChangeObserver> ref = mObservers.get(i);
            if (ref != null && ref.get() != null) {
                ref.get().onAppUpdated(updates, updateWidthFolderList);
            }
        }
    }

    public void notifyStateChange(List<ItemInfo> posterList, ItemInfo itemInfo, FolderInfo folderInfo) {
        for (int i = mObservers.size() - 1; i >= 0; --i) {
            WeakReference<DataChangeObserver> ref = mObservers.get(i);
            if (ref != null && ref.get() != null) {
                ref.get().onStateChange(posterList, itemInfo, folderInfo);
            }
        }
    }

    public void notifySuperscriptChange(ItemInfo itemInfo, FolderInfo inFolderInfo) {
        for (int i = mObservers.size() - 1; i >= 0; --i) {
            WeakReference<DataChangeObserver> ref = mObservers.get(i);
            if (ref != null && ref.get() != null) {
                ref.get().onSuperscriptChange(itemInfo, inFolderInfo);
            }
        }
    }

    /**
     * 添加观察者
     */
    public void registerDataObserver(WeakReference<DataChangeObserver> observer) {
        if (observer == null) {
            // throw new NullPointerException("observer == null");
            LetvLog.i(TAG, "registerDataObserver observer == null");
            return;
        }
        synchronized (this) {
            if (!mObservers.contains(observer))
                mObservers.add(observer);
        }
    }

    /**
     * 删除观察者
     */
    public synchronized void unregisterDataObserver(WeakReference<DataChangeObserver> observer) {
        mObservers.remove(observer);
    }

    public void unregisterAll() {
        synchronized (this) {
            this.mObservers.clear();
        }
    }
}
