package com.xstv.desktop.emodule.view;

import android.util.SparseArray;

import com.xstv.desktop.emodule.presenter.ItemBasePresenter;

import java.util.ArrayList;

public class GridLayoutRecycleViewPool {

    SparseArray<ArrayList<ItemBasePresenter.ItemViewHolder>> mRecycles = new SparseArray<ArrayList<ItemBasePresenter.ItemViewHolder>>();

    public ItemBasePresenter.ItemViewHolder fetchOneViewHolder(int viewType) {
        if (mRecycles.get(viewType) != null && mRecycles.get(viewType).size() > 0) {
            //Log.d("blockGridRecycle", "get one recycle:"+ " type:" + viewType);
            return mRecycles.get(viewType).remove(mRecycles.get(viewType).size() - 1);
        }
        return null;
    }

    public void addRecycledViewHolder(ItemBasePresenter.ItemViewHolder aRecycled, int type) {
        if (mRecycles.get(type) == null) {
            mRecycles.put(type, new ArrayList<ItemBasePresenter.ItemViewHolder>());
        }
        //Log.d("blockGridRecycle", "recycle one:"+aRecycled.toString() + " type:" + type);
        mRecycles.get(type).add(aRecycled);
    }

    public void clear() {
        mRecycles.clear();
    }
}
