
package com.xstv.desktop.app.holder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class BaseHolder<k, v extends View> extends RecyclerView.ViewHolder {
    private static final String TAG = BaseHolder.class.getSimpleName();
    protected Context mContext;
    public v mItemVeiw;

    public BaseHolder(v itemView) {
        super(itemView);
        init(itemView);
    }

    public void init(v itemView) {
        mContext = itemView.getContext();
        mItemVeiw = itemView;
    }

    public void bindData(k itemInfo) {
    }

    public void bindData(k itemInfo, int pos) {
        mItemVeiw.setTag(pos + "");
    }

    public void release(){

    }
}
