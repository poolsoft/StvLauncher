package com.xstv.library.base;

import android.util.ArrayMap;

import com.xstv.library.base.presenter.BasePresenter;

import java.util.Map;

public class DataManager {
    Map<String, BasePresenter> mContracts = new ArrayMap<String, BasePresenter>(4);

    void addPresenter(String id, BasePresenter basePresenter) {
        if (!mContracts.containsKey(id)) {
            mContracts.put(id, basePresenter);
        }
    }

    void removePresenter(String id) {
        if (mContracts.containsKey(id)) {
            mContracts.remove(id);
        }
    }

}
