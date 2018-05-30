package com.xstv.library.base.presenter;

/**
 * @author wuh
 * @date 18-5-28 下午6:29
 * @describe IPresenter 一个数据请求接口，在View有数据请求时，View调用Presenter的方法进行数据请求，具体请求过程是在Model进行，Model请求到数据后，返回给Presenter，Presenter回调IView接口通知UI进行数据显示或刷新。
 */
public interface IPresenter {

//    /**
//     * 注册Presenter到DataModel,在BasePresenter中实现。
//     */
//    void register();
//
//    /**
//     * Presenter从DataModel中解绑,在BasePresenter中实现。
//     */
//    void unRegister();

    /**
     * 初始化数据，必须调用{@code register()}
     * @param type 数据类型
     */
    void initData(DataType type);

    /**
     * 获取数据
     * @param type 需要的数据类型
     */
    void fetchData(DataType type);

    /**
     * 停止后台工作，比如后台线程刷新，定时获取数据等一切与桌面有关的工作。
     */
    void stopWork();

    /**
     * 销毁桌面，需要销毁与桌面相关的资源：内存，线程。必须调用{@code unRegister()}方法。
     */
    void recycle();
}
