
package com.xstv.desktop.app.listener;

import com.xstv.desktop.app.bean.FolderInfo;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.widget.AppFolderCellView;

public interface OnFolderVoiceListener {

    void addFolderVoice(ItemInfo bean, AppFolderCellView itemFolderView);

    void removeFolderVoice(FolderInfo folderInfo, int nullIndex);
}
