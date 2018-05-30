package com.xstv.library.base.model;

import android.support.annotation.NonNull;

import com.xstv.library.base.Logger;
import com.xstv.library.base.presenter.DataType;


/**
 * @author wuh
 * @date 18-5-29 下午12:01
 * @describe ExampleDataModel
 */
public class ExampleDataModel extends BaseDataModel {

    public static final String ID = "com.xstv.library.base.model.ExampleDataModel";
    private Logger logger = Logger.getLogger("Example", "ExampleDataModel");

    /**
     * 创建一个DataModel并且添加到DataManager中。
     *
     * @param id 一个Model的唯一标示，通常是包名+类名。
     */
    public ExampleDataModel(@NonNull String id) {
        super(id);
        mDataModelManager.addModel(id, this);
    }

    /**
     * 回收Presenter对应在DataModel的资源
     *
     * @param pId
     */
    @Override
    public void recyclePresenter(@NonNull String pId) {
        logger.d("recyclePresenter " + pId);
        //pid对应的清理缓存
    }

    @Override
    public void fetchData(DataType type) {
        logger.d("fetchData ");
        //.....
        //mDataModelManager
        //.....
    }

    @Override
    public void initData(DataType type) {
        logger.d("initData ");
    }
}
