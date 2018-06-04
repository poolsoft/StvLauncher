package com.xstv.desktop.emodule.presenter;

import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.VerticalGridView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.xstv.desktop.emodule.R;
import com.xstv.desktop.emodule.mode.Block;
import com.xstv.desktop.emodule.mode.DisplayItem;
import com.xstv.desktop.emodule.view.FocusVerticalGridView;


public class RegularVerticalBlockPresenter extends RegularBlockPresenter {

    @Override
    protected RegularBlockPresenter.ViewHolder createBlockViewHolder(ViewGroup parent) {
        FocusVerticalGridView view = (FocusVerticalGridView) LayoutInflater.from(parent.getContext()).inflate(R.layout.block_vertical, null, false);
        ViewHolder holder = new ViewHolder(view, view, this);
        return holder;
    }

    @Override
    protected void onBindBlockData(ViewHolder vh, Object item) {
        super.onBindBlockData(vh, item);
        Block block = (Block) item;
        if (mPresenterSelector != null) {
            synchronized (block) {
                if (block.items != null && block.items.size() > 0) {
                    vh.mAdapter = new ArrayObjectAdapter(mPresenterSelector);
                    int padding = (int) vh.view.getResources().getDimension(R.dimen.grid_block_hor_padding);
                    int itemMargin = (int) vh.view.getResources().getDimension(R.dimen.grid_item_margin);

                    int columns = block.ui.columns;
                    int rows = block.items.size() / columns;
                    if (block.items.size() % columns > 0) {
                        rows += 1;
                    }

                    int itemWidth = ((mParentWidth - padding * 2 - itemMargin * (columns - 1)) / columns);
                    int itemHeight = (int) (itemWidth / block.ui.ratio);
                    for (int i = 0; i < block.items.size(); ++i) {
                        DisplayItem displayItem = (DisplayItem) block.items.get(i);
                        displayItem.ui.width = itemWidth;
                        displayItem.ui.height = itemHeight;
                        vh.mAdapter.add(displayItem);
                    }

                    VerticalGridView gridView = (VerticalGridView) vh.mGridView;

                    vh.mItemBridgeAdapter.setAdapter(vh.mAdapter);
                    gridView.setAdapter(vh.mItemBridgeAdapter);
                    gridView.setNumColumns(columns);
                    gridView.setItemSpacing(itemMargin);
                    ViewGroup.LayoutParams lp = gridView.getLayoutParams();
                    lp.height = itemHeight * rows + gridView.getVerticalSpacing() * (rows - 1) + gridView.getPaddingTop() + gridView.getPaddingBottom();
                    gridView.setLayoutParams(lp);
                    vh.mItemBridgeAdapter.setItem(item);
                }
            }
        }
    }
}
