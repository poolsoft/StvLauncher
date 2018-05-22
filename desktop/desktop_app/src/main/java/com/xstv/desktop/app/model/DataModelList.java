
package com.xstv.desktop.app.model;

import com.xstv.desktop.app.bean.ContentBean;
import com.xstv.desktop.app.db.ItemInfo;

import java.util.ArrayList;
import java.util.List;

public class DataModelList {

    /**
     * 从服务器返回的数据集合
     */
    public List<ContentBean> contentBeanList = new ArrayList<ContentBean>();

    /**
     * 从本地加载的所有应用集合
     */
    public List<ItemInfo> allAppList = new ArrayList<ItemInfo>();

    private DataModelList() {}

    public static DataModelList getInstance() {
        return DataModelList.SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        private static final DataModelList sInstance = new DataModelList();
    }

    /**
     * 释放内存
     */
    public void crush(){
        contentBeanList.clear();
        allAppList.clear();
    }
}
