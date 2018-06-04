package com.xstv.desktop.emodule.presenter;

import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.Presenter;
import android.view.View;

import com.xstv.desktop.emodule.R;

public abstract class ItemBasePresenter extends Presenter {
    public static int VIEW_HOLD_ID = R.id.view_hold;

    protected static Drawable mDefaultBgDrawable;

    public abstract class ItemViewHolder extends ViewHolder {
        public boolean mIsBind = false;
        private Object mItem;

        public ItemViewHolder(View view) {
            super(view);
        }

        public Object getDataItem() {
            return mItem;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        if (viewHolder instanceof ItemViewHolder) {
            ((ItemViewHolder) viewHolder).mIsBind = true;
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        if (viewHolder instanceof ItemViewHolder) {
            ((ItemViewHolder) viewHolder).mIsBind = false;
        }
    }

    protected void supportReleasePicWhenPaused(ItemViewHolder holder, Object item) {
        holder.mItem = item;
    }

    protected void notSupportReleasePicWhenPaused(ItemViewHolder holder) {
        holder.mItem = null;
    }

    public boolean isSupportReleasePicWhenPaused(ItemViewHolder holder) {
        return holder.mItem != null;
    }

    public void onBindViewImageOnly(ViewHolder viewHolder, Object item) {

    }

    public boolean isDisplayItemDefaultPresenter() {
        return false;
    }

}
