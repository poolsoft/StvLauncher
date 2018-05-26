package com.stv.plugin.demo.presenter;

import com.xstv.library.base.presenter.BasePresenter;
import com.xstv.library.base.presenter.IContract;

import java.util.ArrayList;
import java.util.List;

public class ExamplePresenter extends BasePresenter implements IContract.IPrenenter {

    IContract.IView mView;

    public ExamplePresenter(IContract.IView view) {
        mView = view;
    }

    /**
     * 初始化桌面数据
     *
     * @param pID
     */
    @Override
    public void init(String pID) {
        mView.init();
    }

    /**
     * 更新数据
     *
     * @param pID
     */
    @Override
    public void updateData(String pID) {
        List list = new ArrayList();
        mView.showData(pID, list);
    }
}
