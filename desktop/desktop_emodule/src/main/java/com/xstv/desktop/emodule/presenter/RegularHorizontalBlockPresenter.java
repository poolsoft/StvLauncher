package com.xstv.desktop.emodule.presenter;

import android.annotation.SuppressLint;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.xstv.desktop.emodule.R;
import com.xstv.desktop.emodule.mode.Block;
import com.xstv.desktop.emodule.mode.DisplayItem;

public class RegularHorizontalBlockPresenter extends RegularBlockPresenter {

    @SuppressLint("RestrictedApi")
    @Override
    protected RegularBlockPresenter.ViewHolder createBlockViewHolder(ViewGroup parent) {
        HorizontalGridView view = (HorizontalGridView) LayoutInflater.from(parent.getContext()).inflate(R.layout.block_hor, null, false);
        view.setFocusScrollStrategy(HorizontalGridView.FOCUS_SCROLL_ALIGNED);
        view.setScrollEnabled(true);
        view.setExtraLayoutSpace(100);
        ViewHolder holder = new ViewHolder(view, view, this);
        return holder;
    }

    @Override
    protected void onBindBlockData(ViewHolder vh, Object item) {
        super.onBindBlockData(vh, item);
        final Block block = (Block) item;
        if (mPresenterSelector != null) {
            synchronized (block) {
                if (block.items != null && block.items.size() > 0) {
                    vh.mAdapter = new ArrayObjectAdapter(mPresenterSelector);
                    int columns = block.ui.columns;
                    int padding = (int) vh.view.getResources().getDimension(R.dimen.grid_block_hor_padding);
                    int itemSpacing = (int) vh.view.getResources().getDimension(R.dimen.grid_item_margin);
                    int itemWidth = ((mParentWidth - padding * 2 - itemSpacing * (columns - 1)) / columns);
                    int itemHeight = (int) (itemWidth / block.ui.ratio);
                    for (int i = 0; i < block.items.size(); ++i) {
                        DisplayItem displayItem = (DisplayItem) block.items.get(i);
                        displayItem.ui.width = itemWidth;
                        displayItem.ui.height = itemHeight;
                        vh.mAdapter.add(displayItem);
                    }
                    vh.mItemBridgeAdapter.setAdapter(vh.mAdapter);
                    vh.mGridView.setAdapter(vh.mItemBridgeAdapter);
                    vh.mGridView.setItemSpacing(itemSpacing);
                    vh.mItemBridgeAdapter.setItem(item);
                }
            }
        }
    }
}