
package com.xstv.desktop.app.listener;

import com.xstv.desktop.app.bean.ContentBean;
import com.xstv.desktop.app.db.ItemInfo;

import java.util.List;

public interface OnDataChangeListener {

    void onShowLoading();

    void onHideLoading();

    void onNotifyUI(List<ContentBean> contentBeanList, boolean isHasServerData, List<ItemInfo> itemInfoList);
}
