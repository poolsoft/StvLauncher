package com.xstv.library.base.presenter;

/**
 * @author wuh
 * @date 18-5-28 下午6:29
 * @describe IPresenter 一个数据请求接口,在View有数据请求时,View调用Presenter的方法进行数据请求,具体请求过程是在Model进行,Model请求到数据后,返回给Presenter,Presenter回调IView接口通知UI进行数据显示或刷新.
 */
public interface IPresenter {
    /**
     * 初始化数据,必须调用{@link BasePresenter#register()}
     */
    void bind();

    /**
     * 停止后台工作,比如后台线程刷新,定时获取数据等一切与桌面有关的工作.
     */
    void stopWork();

    /**
     * 销毁桌面,需要销毁与桌面相关的资源：内存,线程.必须调用{@link BasePresenter#unRegister()}方法.
     */
    void unBind();

    /**
     * 通知Model获取数据,然后通过{@link IView.DataCallback#onDataBack(Object)}返回数据.
     *
     * @param params 请求参数,包含请求URL,请求类型等.请求类型用来标识请求是从哪个模块触发的.
     */
    void getData(DataType params);

    /**
     * 通知Model准备数据,一般情况下Model都会准备好初始数据,立刻会回调{@link IView.DataCallback#onDataBack(Object)}方法.
     * <p>
     * 如果没有缓存数据则触发异步获取数据,然后通过{@link IView.DataCallback#onDataBack(Object)}返回数据.
     */
    void initData();
}
