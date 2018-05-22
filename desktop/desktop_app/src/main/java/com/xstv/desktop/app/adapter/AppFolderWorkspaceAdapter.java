package com.xstv.desktop.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.xstv.base.LetvLog;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.holder.AppHolder;
import com.xstv.desktop.app.widget.AppCellView;

public class AppFolderWorkspaceAdapter extends BaseSpaceAdapter<ItemInfo> {

    private static final String TAG = AppFolderWorkspaceAdapter.class.getSimpleName();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LetvLog.i(TAG, "onCreateViewHolder");
        AppCellView appCellView = new AppCellView(viewGroup.getContext());
        appCellView.setAppFragment(fragmentRef);
        AppHolder appHolder = new AppHolder(appCellView);
        return appHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ItemInfo info = getDataSet().get(position);
        LetvLog.i(TAG, "onBindViewHolder info = " + info);
        if (info != null) {
            ((AppHolder) viewHolder).bindData(info, position);
        }
    }
}
