package com.xstv.library.base.presenter;

import java.util.Collection;

public interface IContract {
    interface IPrenenter {
        /**
         * 初始化桌面数据
         *
         * @param pID
         */
        void init(String pID);

        /**
         * 更新数据
         */
        void updateData(String pID);
    }

    interface IView<T> {
        void init();

        void showError(String error);

        void showData(String pID, Collection<? extends T> collection);
    }

}
