
package com.xstv.desktop.app.holder;

import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.widget.AppCellView;

public class AppHolder extends BaseHolder<ItemInfo, AppCellView> {

    public AppHolder(AppCellView itemView) {
        super(itemView);
    }

    @Override
    public void bindData(ItemInfo itemInfo, int pos) {
        super.bindData(itemInfo, pos);
        mItemVeiw.bindData(itemInfo);
        mItemVeiw.setTag(pos + "");
    }


    @Override
    public void release() {
        super.release();
        if(mItemVeiw != null){
            mItemVeiw.recycle(false);
        }
    }
}
