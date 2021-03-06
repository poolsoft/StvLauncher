package com.xstv.library.base.presenter;

/**
 * @author wuh
 * @date 18-5-28 下午4:11
 * @describe IView 数据层通知UI的接口.由具体的Fragment或者ViewGroup来实现这个接口.
 * Presenter处理完数据后回调UI进行数据显示，刷新等操作。
 * <p>
 * 一个IView一个Presenter，这样便于不同页面的解耦。
 * <p>
 * 调用例子：UI--getData-->Presenter--Model获取数据，然后回调-->UI
 * <p>
 * 注意：回调IView的方法线程都是在UI主线程，不需要UI再进行线程切换，所以从UI发起数据请求到回调可能延时的情况，比如从网络获取数据。
 */
public interface IView<Result> {

    /**
     * 如果你想从model获取数据, 那么就实现这个接口.
     *
     * @param <Result> 返回的数据类型.
     */
    interface DataCallback<Result> {
        /**
         * model获取到数据后回调方法,运行在UI线程.
         *
         * @param result
         */
        void onDataBack(Result result);

        /**
         * 数据更新回调方法,运行在UI线程.
         *
         * @param result
         */
        void onDataChange(Result result);
    }

    /**
     * 与Fragment交互相关的回调接口
     */
    interface FragmentCallback {
        /**
         * 显示页面错误信息
         *
         * @param error
         */
        void showError(String error);

        void startAppAnim();

        void backToTab();

        void checkHandDetectEnter();

        void showStatusbar();

        void hideStatusBar();

        void showTabView();

        void hideTabView();

        void setKeyDragOut(boolean is);

        void setTouchDragOut(boolean is);

    }

    /**
     * 如果有获取数据功能,则创建一个实现了{@link DataCallback}接口的实例,并返回.实例会绑定到Presenter中.
     *
     * @return
     */
    DataCallback<Result> bindDataCallback();

    /**
     * 如果有Fragment交互功能,则创建一个实现{@link FragmentCallback}接口的实例,并返回,实例会绑定到Presenter中.
     *
     * @return
     */
    FragmentCallback bindFragmentCallback();
}
