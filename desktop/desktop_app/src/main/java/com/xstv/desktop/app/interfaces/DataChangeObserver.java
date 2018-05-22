
package com.xstv.desktop.app.interfaces;

import com.xstv.desktop.app.bean.FolderInfo;
import com.xstv.desktop.app.db.ItemInfo;

import java.util.ArrayList;
import java.util.List;

public interface DataChangeObserver {

    void onAppAdded(ArrayList<ItemInfo> adds);

    /**
     *
     * @param removes 不包含folder
     * @param removeList 可能包含文件夹
     */
    void onAppRemoved(ArrayList<ItemInfo> removes, ArrayList<ItemInfo> removeList);

    void onAppUpdated(ArrayList<ItemInfo> updates, ArrayList<ItemInfo> updateList);

    /**
     * 如果itemInfo在文件夹中,则inFolderInfo为所在的文件夹.
     * @param itemInfo
     * @param inFolderInfo
     */
    void onSuperscriptChange(ItemInfo itemInfo, FolderInfo inFolderInfo);

    /**
     *
     */
    void onStateChange(List<ItemInfo> posterList, ItemInfo itemInfo, FolderInfo folderInfo);
}
