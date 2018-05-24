/*
 * Copyright (C) 2014 The Android Open Source Project Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.xstv.desktop.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;

import com.xstv.library.base.LetvLog;
import com.xstv.desktop.app.bean.ContentBean;
import com.xstv.desktop.app.bean.FolderInfo;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.holder.AppFolderHolder;
import com.xstv.desktop.app.holder.AppHolder;
import com.xstv.desktop.app.holder.ContentHolder;
import com.xstv.desktop.app.listener.OnFolderVoiceListener;
import com.xstv.desktop.app.model.AppDataModel;
import com.xstv.desktop.app.model.DataModelList;
import com.xstv.desktop.app.widget.AppCellView;
import com.xstv.desktop.app.widget.AppFolderCellView;
import com.xstv.desktop.app.widget.MainContent;
import com.xstv.desktop.app.widget.VipContent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provide views to RecyclerView with data from mDataSet.
 */

public class AppWorkspaceAdapter extends BaseSpaceAdapter<ItemInfo> {
    private static final String TAG = AppWorkspaceAdapter.class.getSimpleName();

    private static final int ITEM_TYPE_HEADER1 = 10001;
    private static final int ITEM_TYPE_HEADER2 = 10002;

    private OnFolderVoiceListener mFolderVoiceListener;

    private List<ContentBean> mHeaderList = new ArrayList<ContentBean>(2);

    public void setOnFolderVoiceListener(OnFolderVoiceListener folderVoiceListener) {
        this.mFolderVoiceListener = folderVoiceListener;
    }

    public void setHeaderData(List<ContentBean> list) {
        LetvLog.d(TAG, "setHeaderData " + list.size());
        mHeaderList.clear();
        mHeaderList.addAll(list);
    }

    public void updateHeader(List<ContentBean> list) {
        setHeaderData(list);
        notifyItemRangeChanged(0, getHeaderSize());
    }

    public int getHeaderSize() {
        return mHeaderList.size();
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + getHeaderSize();
    }

    @Override
    public int getItemViewType(int position) {
        if (getHeaderSize() > 0) {
            if (position == 0) {
                return ITEM_TYPE_HEADER1;
            }/* else if (position == 1) {
                return ITEM_TYPE_HEADER1;//ITEM_TYPE_HEADER2;
            } */else {
                position = position - getHeaderSize();
            }
        }
        ItemInfo itemInfo = getDataSet().get(position);
        return itemInfo.getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LetvLog.d(TAG, "onCreateViewHolder viewType = " + viewType);
        switch (viewType) {
            case ITEM_TYPE_HEADER1:
                VipContent vipContent = new VipContent(viewGroup.getContext());
                vipContent.setAppFragment(fragmentRef);
                return new ContentHolder(vipContent);
            case ITEM_TYPE_HEADER2:
                MainContent mainContent = new MainContent(viewGroup.getContext());
                mainContent.setAppFragment(fragmentRef);
                return new ContentHolder(mainContent);
            case AppDataModel.ITEM_TYPE_APPLICATION:
            case AppDataModel.ITEM_TYPE_PRELOADED:
            case AppDataModel.ITEM_TYPE_SHORTCUT:
                AppCellView appCellView = new AppCellView(viewGroup.getContext());
                appCellView.setAppFragment(fragmentRef);
                AppHolder appHolder = new AppHolder(appCellView);
                return appHolder;
            case AppDataModel.ITEM_TYPE_FOLDER:
                AppFolderCellView folderCellView = new AppFolderCellView(viewGroup.getContext());
                folderCellView.setAppFragment(fragmentRef);
                AppFolderHolder appFolderHolder = new AppFolderHolder(folderCellView);
                return appFolderHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewtype = getItemViewType(position);
        LetvLog.d(TAG, "onBindViewHolder viewType = " + viewtype + " position = " + position);
        switch (viewtype) {
            case ITEM_TYPE_HEADER1:
            case ITEM_TYPE_HEADER2:
                ((ContentHolder) viewHolder).bindData(mHeaderList.get(position), position);
                break;
            case AppDataModel.ITEM_TYPE_APPLICATION:
            case AppDataModel.ITEM_TYPE_PRELOADED:
            case AppDataModel.ITEM_TYPE_SHORTCUT:
                ((AppHolder) viewHolder).bindData(getDataSet().get(position - getHeaderSize()), position);
                break;
            case AppDataModel.ITEM_TYPE_FOLDER:
                ItemInfo itemInfo = getDataSet().get(position - getHeaderSize());
                if (itemInfo instanceof FolderInfo) {
                    AppFolderHolder folderHolder = (AppFolderHolder) viewHolder;
                    (folderHolder).bindData((FolderInfo) itemInfo, position);
                    if (mFolderVoiceListener != null) {
                        mFolderVoiceListener.addFolderVoice(itemInfo, folderHolder.mItemVeiw);
                    }
                }
                break;
        }
    }

    @Override
    public ItemInfo getItemInfoByPosition(int position) {
        int pos = position - getHeaderSize();

        if (getDataSet() == null || pos < 0 || pos >= getDataSet().size()) {
            return null;
        }
        return getDataSet().get(pos);
    }

    @Override
    public int getPositionByBean(ItemInfo fromBean) {
        if (getDataSet() == null) {
            return -1;
        }
        int index = getDataSet().indexOf(fromBean);
        if (index != -1) {
            index += getHeaderSize();
        }
        return index;
    }

    @Override
    public void addItem(ItemInfo itemInfo) {
        getDataSet().add(itemInfo);
        notifyItemInserted(getDataSet().size() + getHeaderSize());
    }

    @Override
    public void addItem(ItemInfo itemInfo, int position) {
        int pos = position - getHeaderSize();
        if (getDataSet() == null || itemInfo == null || pos < 0 || pos >= getDataSet().size()) {
            LetvLog.e(TAG, "addItem arguments Illegal exception");
            return;
        }

        getDataSet().add(pos, itemInfo);
        notifyItemInserted(position);
    }

    @Override
    public void removeItem(int position) {
        int pos = position - getHeaderSize();
        if (getDataSet() == null || pos < 0 || pos >= getDataSet().size()) {
            LetvLog.e(TAG, "removeItem arguments Illegal exception");
            return;
        }
        ItemInfo itemInfo = getDataSet().remove(pos);
        LetvLog.d(TAG, " removeItem itemInfo = " + itemInfo);
        if (itemInfo != null) {
            notifyItemRemoved(position);
        }
    }

    @Override
    public int removeItem(ItemInfo itemInfo) {
        if (itemInfo == null) {
            LetvLog.e(TAG, "removeItem is null.");
            return -1;
        }

        if (itemInfo instanceof FolderInfo) {
            FolderInfo folderInfo = (FolderInfo) itemInfo;
            if (folderInfo.getLength() != 0) {
                updateItem(itemInfo);
                return -1;
            }
        }

        int removeIndex = getDataSet().indexOf(itemInfo);
        if (removeIndex != -1) {
            getDataSet().remove(itemInfo);
            removeIndex += getHeaderSize();
            notifyItemRemoved(removeIndex);
        }

        return removeIndex;
    }

    public void moveItem(int fromPosition, int toPosition) {
        LetvLog.d(TAG, " moveItem fromPosition = " + fromPosition + " toPosition = " + toPosition);
        int fromPos = fromPosition - getHeaderSize();
        int toPos = toPosition - getHeaderSize();

        if (toPos < 0) {
            toPosition = getHeaderSize();
        }
        if (toPos >= getDataSet().size()) {
            toPosition = getDataSet().size() + getHeaderSize() - 1;
        }
        ItemInfo from = getDataSet().remove(fromPos);
        getDataSet().add(toPos, from);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void updateItem(ItemInfo updateInfo) {
        if (updateInfo == null || getDataSet() == null) {
            LetvLog.e(TAG, "updateItem is null.");
            return;
        }
        int updateIndex = getDataSet().indexOf(updateInfo);
        if (updateIndex == -1) {
            Log.w(TAG, "updateItem updateIndex is -1.");
            return;
        }
        getDataSet().set(updateIndex, updateInfo);
        LetvLog.d(TAG, "updateItem updateIndex = " + updateIndex);
        notifyItemChanged(updateIndex + getHeaderSize());
    }


    public int removeNUllFolder() {
        if (getDataSet() == null) {
            return -1;
        }
        Iterator<ItemInfo> infoIterator = getDataSet().iterator();
        while (infoIterator.hasNext()) {
            ItemInfo bean = infoIterator.next();
            if (bean instanceof FolderInfo) {
                if (((FolderInfo) bean).getLength() == 0) {
                    int nullIndex = getDataSet().indexOf(bean);
                    if (nullIndex != -1) {
                        LetvLog.d(TAG, " removeNUllFolder bean = " + bean);
                        getDataSet().remove(bean);
                        List<ItemInfo> allAppList = DataModelList.getInstance().allAppList;
                        if (allAppList != null) {
                            allAppList.remove(bean);
                        }
                        nullIndex += getHeaderSize();
                        notifyItemRemoved(nullIndex);
                        if (mFolderVoiceListener != null) {
                            mFolderVoiceListener.removeFolderVoice((FolderInfo) bean, nullIndex);
                        }
                        return nullIndex;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Get new folder
     *
     * @return
     */
    public FolderInfo getAddingStateFolder() {
        FolderInfo folderInfo = null;
        for (ItemInfo bean : getDataSet()) {
            if (bean != null && bean instanceof FolderInfo) {
                folderInfo = (FolderInfo) bean;
                if (folderInfo.isAdding || folderInfo.getLength() == 0) {
                    LetvLog.d(TAG, " getAddingStateFolder folderInfo = " + folderInfo);
                    break;
                }
            }
        }
        return folderInfo;
    }
}
