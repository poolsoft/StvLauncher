package com.stv.plugin.demo.presenter;

import android.support.annotation.NonNull;

import com.xstv.library.base.bean.DataBean;
import com.xstv.library.base.presenter.BasePresenter;
import com.xstv.library.base.presenter.DataType;
import com.xstv.library.base.presenter.IView;

/**
 * @author wuh
 * @date 18-5-28 下午6:29
 * @describe ExamplePresenter
 */
public class ExamplePresenter extends BasePresenter {
    /**
     * @param view    实现了IView的对象
     * @param modelID 需要关联的Model模块ID。
     */
    public ExamplePresenter(@NonNull IView view, @NonNull String modelID) {
        super(view, modelID);
    }

    /**
     * 初始化数据,必须调用{@link BasePresenter#register()}
     */
    @Override
    public void bind() {
        register();
    }

    /**
     * 停止后台工作,比如后台线程刷新,定时获取数据等一切与桌面有关的工作.
     */
    @Override
    public void stopWork() {
        mDataModel.stopWork(mPresenterID);
    }

    /**
     * 销毁桌面,需要销毁与桌面相关的资源：内存,线程.必须调用{@link BasePresenter#unRegister()}方法.
     */
    @Override
    public void unBind() {
        unRegister();
    }

    /**
     * 通知Model获取数据,然后通过{@link IView.DataCallback#onDataBack(Object)}返回数据.
     *
     * @param params 请求参数,包含请求URL,请求类型等.请求类型用来标识请求是从哪个模块触发的.
     */
    @Override
    public void getData(DataType params) {
        mDataModel.fetchData(params);
        mDataCallback.onDataChange(new DataBean(mPresenterID));
    }

    /**
     * 通知Model准备数据,一般情况下Model都会准备好初始数据,立刻会回调{@link IView.DataCallback#onDataBack(Object)}方法.
     * <p>
     * 如果没有缓存数据则触发异步获取数据,然后通过{@link IView.DataCallback#onDataBack(Object)}返回数据.
     */
    @Override
    public void initData() {
        mDataModel.initData();
        mDataCallback.onDataBack(new DataBean(mPresenterID));
    }
}
