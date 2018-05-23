package com.stv.plugin.demo.widget;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

class RecyclerViewLayoutManager extends GridLayoutManager {

    RecyclerViewLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    @Override
    public View onFocusSearchFailed(View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return null;
    }

}
