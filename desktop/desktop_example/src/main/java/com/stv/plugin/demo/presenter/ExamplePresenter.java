package com.stv.plugin.demo.presenter;

import android.support.annotation.NonNull;

import com.xstv.library.base.Logger;
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
     * @param pid     Presenter的ID，不能为NULL，或者空字符串，最好是包名+类名。
     * @param modelID 需要关联的Model模块ID。
     */
    public ExamplePresenter(@NonNull IView view, @NonNull String pid, @NonNull String modelID) {
        super(view, pid, modelID);
    }

    /**
     * 初始化数据，必须调用{@code register()}
     *
     * @param type 数据类型
     */
    @Override
    public void initData(DataType type) {
        register();
        mDataModel.initData(type);
    }

    /**
     * 获取数据
     *
     * @param type 需要的数据类型
     */
    @Override
    public void fetchData(DataType type) {
        mDataModel.fetchData(type);
    }

    /**
     * 停止后台工作，比如后台线程刷新，定时获取数据等一切与桌面有关的工作。
     */
    @Override
    public void stopWork() {

    }

    /**
     * 销毁桌面，比如view的引用，需要销毁与桌面相关的资源：内存，线程，引用。必须调用{@code unRegister()}方法。
     */
    @Override
    public void recycle() {
        unRegister();
        //销毁与view的引用
    }
}
