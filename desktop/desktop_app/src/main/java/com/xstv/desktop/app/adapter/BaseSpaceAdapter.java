
package com.xstv.desktop.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.xstv.library.base.LetvLog;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.holder.BaseHolder;
import com.xstv.desktop.app.interfaces.IAppFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseSpaceAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = BaseSpaceAdapter.class.getSimpleName();

    private List<T> mDataSet = new ArrayList<T>();

    protected WeakReference<IAppFragment> fragmentRef;

    public void setAppFragment(IAppFragment fragment) {
        if(fragmentRef == null){
            this.fragmentRef = new WeakReference<IAppFragment>(fragment);
        }
    }

    public void setAdapterData(List<T> itemInfoList) {
        mDataSet.clear();
        this.mDataSet.addAll(itemInfoList);
        LetvLog.d(TAG, "setAdapterData size = " + mDataSet.size());
        notifyDataSetChanged();
    }

    public void appendAdapterData(List<T> itemInfoList){
        if(itemInfoList == null || itemInfoList.size() == 0){
            Log.w(TAG, "appendAdapterData invalid data return.");
            return;
        }
        int olderSize = getItemCount();
        mDataSet.addAll(itemInfoList);
        notifyItemRangeInserted(olderSize, itemInfoList.size());
    }

    public void updateAdapterData(List<T> updateList) {
        int olderDataSize = getItemCount();
        int newDataSize = updateList.size();
        this.mDataSet.clear();
        this.mDataSet.addAll(updateList);
        LetvLog.d(TAG, "updateAdapterData olderDataSize = " + olderDataSize + " newDataSize = " + newDataSize);
        if (olderDataSize > newDataSize) {
            notifyItemRangeRemoved(newDataSize, olderDataSize - newDataSize);
            notifyItemRangeChanged(0, newDataSize);
        } else if (olderDataSize < newDataSize) {
            notifyItemRangeChanged(0, newDataSize);
            notifyItemRangeInserted(newDataSize, newDataSize - olderDataSize);
        } else {
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    public List<T> getDataSet() {
        return mDataSet;
    }

    @Override
    public int getItemCount() {
        if (mDataSet == null) {
            return 0;
        }
        return mDataSet.size();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof BaseHolder) {
            BaseHolder appHolder = (BaseHolder) holder;
            appHolder.release();
        }
    }

    public T getItemInfoByPosition(int position) {
        if (mDataSet == null || (mDataSet != null && position >= mDataSet.size()) || position < 0) {
            return null;
        }
        return mDataSet.get(position);
    }

    public int getPositionByBean(ItemInfo fromBean) {
        if (mDataSet == null) {
            return -1;
        }
        return mDataSet.indexOf(fromBean);
    }

    public void addItem(T itemInfo) {
        if (mDataSet == null) {
            return;
        }

        mDataSet.add(itemInfo);
        notifyItemInserted(mDataSet.size());
    }

    public void addItem(T itemInfo, int position) {
        if (mDataSet == null || itemInfo == null || position < 0 || position >= mDataSet.size()) {
            LetvLog.e(TAG, "addItem arguments Illegal exception");
            return;
        }

        mDataSet.add(position, itemInfo);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        if (mDataSet == null || position < 0 || position >= mDataSet.size()) {
            LetvLog.e(TAG, "removeItem arguments Illegal exception");
            return;
        }
        T itemInfo = mDataSet.remove(position);
        LetvLog.d(TAG, " removeItem itemInfo = " + itemInfo);
        if(itemInfo != null){
            notifyItemRemoved(position);
        }
    }

    public int removeItem(ItemInfo itemInfo) {
        if (itemInfo == null || mDataSet == null) {
            LetvLog.e(TAG, "removeItem is null.");
        }
        int removeIndex = mDataSet.indexOf(itemInfo);
        LetvLog.d(TAG, " removeFolder itemInfo = " + itemInfo + " removeIndex = " + removeIndex);
        if (mDataSet.remove(itemInfo)) {
            notifyItemRemoved(removeIndex);
        }
        return removeIndex;
    }

    public void moveItem(int fromPosition, int toPosition) {
        LetvLog.d(TAG, " moveItem fromPosition = " + fromPosition + " toPosition = " + toPosition);
        if (toPosition < 0) {
            toPosition = 0;
        }
        if (toPosition >= mDataSet.size()) {
            toPosition = mDataSet.size() - 1;
        }
        T from = mDataSet.remove(fromPosition);
        mDataSet.add(toPosition, from);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void updateItem(T updateInfo) {
        if (updateInfo == null || mDataSet == null) {
            LetvLog.e(TAG, "updateItem is null.");
            return;
        }
        int updateIndex = mDataSet.indexOf(updateInfo);
        if (updateIndex == -1) {
            Log.w(TAG, "updateItem updateIndex is -1.");
            return;
        }
        mDataSet.set(updateIndex, updateInfo);
        LetvLog.d(TAG, "updateItem updateIndex = " + updateIndex);
        notifyItemChanged(updateIndex);
    }
}
