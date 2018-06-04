package com.xstv.desktop.emodule.view;


import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;

import java.util.ArrayList;

public class ViewPool {
    private static final String TAG = "ViewPool";

    private static final boolean DEBUG = false;

    public static final int INVALID_TYPE = -1;
//    public static abstract class ViewHolder {
//        public final View itemView;
//        int mItemViewType = INVALID_TYPE;
//        public ViewHolder(View itemView) {
//            if (itemView == null) {
//                throw new IllegalArgumentException("itemView may not be null");
//            }
//            this.itemView = itemView;
//        }
//    }


    private SparseArray<ArrayList<View>> mScrap =
            new SparseArray<>();
    private SparseIntArray mMaxScrap = new SparseIntArray();
    private int mAttachCount = 0;

    private static final int DEFAULT_MAX_SCRAP = 10;

    public void clear() {
        mScrap.clear();
    }

    public void setMaxRecycledViews(int viewType, int max) {
        mMaxScrap.put(viewType, max);
        final ArrayList<View> scrapHeap = mScrap.get(viewType);
        if (scrapHeap != null) {
            while (scrapHeap.size() > max) {
                scrapHeap.remove(scrapHeap.size() - 1);
            }
        }
    }

    public View getRecycledView(int viewType) {
        final ArrayList<View> scrapHeap = mScrap.get(viewType);
        if (scrapHeap != null && !scrapHeap.isEmpty()) {
            final int index = scrapHeap.size() - 1;
            final View scrap = scrapHeap.get(index);
            scrapHeap.remove(index);
            return scrap;
        }
        return null;
    }

    int size() {
        int count = 0;
        for (int i = 0; i < mScrap.size(); i++) {
            ArrayList<View> viewHolders = mScrap.valueAt(i);
            if (viewHolders != null) {
                count += viewHolders.size();
            }
        }
        return count;
    }

    public void putRecycledView(View scrap, int viewType) {
        final ArrayList scrapHeap = getScrapHeapForType(viewType);
        if (mMaxScrap.get(viewType) <= scrapHeap.size()) {
            return;
        }
        if (DEBUG && scrapHeap.contains(scrap)) {
            throw new IllegalArgumentException("this scrap staggered_item already exists");
        }
        scrapHeap.add(scrap);
    }

    void attach(RecyclerView.Adapter adapter) {
        mAttachCount++;
    }

    void detach() {
        mAttachCount--;
    }


    private ArrayList<View> getScrapHeapForType(int viewType) {
        ArrayList<View> scrap = mScrap.get(viewType);
        if (scrap == null) {
            scrap = new ArrayList<>();
            mScrap.put(viewType, scrap);
            if (mMaxScrap.indexOfKey(viewType) < 0) {
                mMaxScrap.put(viewType, DEFAULT_MAX_SCRAP);
            }
        }
        return scrap;
    }

}
