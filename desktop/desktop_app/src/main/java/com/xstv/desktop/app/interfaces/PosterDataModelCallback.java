package com.xstv.desktop.app.interfaces;

import com.xstv.desktop.app.bean.ContentBean;

import java.util.List;

public interface PosterDataModelCallback {

    /**
     *
     * @param contentBeanList 服务器返回的数据
     */
    void onServerData(List<ContentBean> contentBeanList, boolean isUpdate);
}
