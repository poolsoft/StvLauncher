package com.stv.plugin.demo.fragment;

import android.support.annotation.NonNull;

import com.xstv.library.base.bean.DataBean;
import com.xstv.library.base.presenter.BasePresenter;
import com.xstv.library.base.presenter.DataType;
import com.xstv.library.base.presenter.IView;

public class LeanbackPresenter extends BasePresenter {
    /**
     * @param view    实现了IView的对象
     * @param pid     Presenter的ID,不能为NULL,或者空字符串,最好是Presenter的包名+类名.
     * @param modelID 需要关联的Model模块ID.
     */
    public LeanbackPresenter(@NonNull IView view, @NonNull String pid, @NonNull String modelID) {
        super(view, pid, modelID);
    }

    /**
     * 初始化数据,必须调用{@link #register()}
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
     * 销毁桌面,需要销毁与桌面相关的资源：内存,线程.必须调用{@link #unRegister()}方法.
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
