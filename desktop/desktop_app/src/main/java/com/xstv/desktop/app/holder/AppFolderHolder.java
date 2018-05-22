
package com.xstv.desktop.app.holder;

import com.xstv.desktop.app.bean.FolderInfo;
import com.xstv.desktop.app.widget.AppFolderCellView;

public class AppFolderHolder extends BaseHolder<FolderInfo, AppFolderCellView> {

    public AppFolderHolder(AppFolderCellView itemView) {
        super(itemView);
    }


    @Override
    public void bindData(FolderInfo itemInfo, int pos) {
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
