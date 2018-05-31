package com.xstv.library.base.model;

import android.support.annotation.NonNull;
import android.util.ArrayMap;

import com.xstv.library.base.Logger;
import com.xstv.library.base.presenter.BasePresenter;
import com.xstv.library.base.presenter.DataType;

import java.util.Map;

/**
 * @author wuh
 * @date 18-5-28 下午4:07
 * @describe BaseDataModel
 * 每一个桌面都有一个数据管理类Model,用来请求网络数据,缓存管理,数据库存储.
 * <p>
 * 每一个DataModel都注册到DataManager中,统一由DataManager来管理.
 */
public abstract class BaseDataModel {

    private String TAG = "BaseDataModel";
    @NonNull
    public final String ID;
    private Map<String, BasePresenter> mPresenterMap;
    private Logger logger = Logger.getLogger("BaseFrame", TAG);
    public DataModelManager mDataModelManager;

    /**
     * 在创建Presenter的时候会调用.
     *
     * @param id 一个Model的唯一标示,通常是包名+类名.
     */
    public BaseDataModel(@NonNull final String id) {
        ID = id;
        mPresenterMap = new ArrayMap<>(3);
        if (mDataModelManager == null) {
            mDataModelManager = DataModelManager.getInstance();
        }
        mDataModelManager.addModel(id, this);
    }

    /**
     * 添加一个Presenter到Model.如果之前添加过同样ID的Presenter则会替换掉之前的.
     *
     * @param pId           Presenter ID,Presenter的包名+类名.
     * @param basePresenter
     */
    public void addPresenter(@NonNull final String pId, @NonNull BasePresenter basePresenter) {
        logger.d("addPresenter pID = " + pId);
        if (pId == null || basePresenter == null) {
            return;
        }
        mPresenterMap.put(pId, basePresenter);
    }

    /**
     * 从Model中移除Presenter.移除的时候会调用{@link #recyclePresenter(String)}回收Presenter对应在DataModel的资源.
     *
     * @param pId Presenter ID,Presenter的包名+类名.
     */
    public void removePresenter(@NonNull final String pId) {
        logger.d("removePresenter pID = " + pId);
        if (pId == null) {
            return;
        }
        BasePresenter presenter = mPresenterMap.remove(pId);
        if (null != presenter) {
            logger.d("can unBind Presenter pID = " + pId);
            recyclePresenter(pId);
        }
    }

    /**
     * 查找并返回Presenter
     *
     * @param pId
     * @return
     */
    public BasePresenter getPresenter(@NonNull final String pId) {
        if (pId == null) {
            return null;
        }
        return mPresenterMap.get(pId);
    }

    /**
     * 回收Presenter对应在DataModel的资源
     *
     * @param pId
     */
    public abstract void recyclePresenter(@NonNull final String pId);

    public abstract void stopWork(@NonNull final String pId);

    public abstract void fetchData(DataType type);

    public abstract void initData();
}
