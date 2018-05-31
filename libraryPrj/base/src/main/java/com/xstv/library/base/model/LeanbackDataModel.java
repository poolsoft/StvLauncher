package com.xstv.library.base.model;

import android.support.annotation.NonNull;

import com.xstv.library.base.Logger;
import com.xstv.library.base.presenter.DataType;

public class LeanbackDataModel extends BaseDataModel {
    private Logger logger = Logger.getLogger("BaseFrame", "LeanbackDataModel");

    /**
     * 在创建Presenter的时候会调用.
     *
     * @param id 一个Model的唯一标示,通常是包名+类名.
     */
    public LeanbackDataModel(@NonNull String id) {
        super(id);
    }

    /**
     * 回收Presenter对应在DataModel的资源
     *
     * @param pId
     */
    @Override
    public void recyclePresenter(@NonNull String pId) {
        logger.d("recyclePresenter " + pId);
    }

    @Override
    public void stopWork(@NonNull String pId) {
        logger.d("stopWork " + pId);
    }

    @Override
    public void fetchData(DataType type) {
        logger.d("fetchData ");
    }

    @Override
    public void initData() {
        logger.d("initData ");
    }


}
