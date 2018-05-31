package com.xstv.library.base.presenter;

import android.support.annotation.NonNull;

import com.xstv.library.base.Logger;
import com.xstv.library.base.model.BaseDataModel;
import com.xstv.library.base.model.DataModelManager;
import com.xstv.library.base.model.ExampleDataModel;
import com.xstv.library.base.model.LeanbackDataModel;
import com.xstv.library.base.model.ModelID;

/**
 * @author wuh
 * @date 18-5-28 下午4:10
 * @describe BasePresenter
 * 功能：
 * 1.负责从model层获取数据,然后调用view显示数据.
 * 2.一个Presenter对应一个IView,可以有多个Presenter,每个Presenter都注册到DataModel中.
 * 3.编写具体的业务逻辑和数据请求.
 */
public abstract class BasePresenter implements IPresenter {
    private Logger logger = Logger.getLogger("BaseFrame", "BasePresenter");
    @NonNull
    public IView mView;
    @NonNull
    public final String mPresenterID;
    protected BaseDataModel mDataModel;
    protected IView.DataCallback mDataCallback;
    protected IView.FragmentCallback mFragmentCallback;

    /**
     * @param view    实现了IView的对象
     * @param pid     Presenter的ID,不能为NULL,或者空字符串,最好是Presenter的包名+类名.
     * @param modelID 需要关联的Model模块ID.
     */
    public BasePresenter(@NonNull IView view, @NonNull final String pid, @NonNull final String modelID) {
        mPresenterID = this.getClass().getName();
        logger.d(mPresenterID + " is init.");
        mView = view;
        mDataModel = getDataModel(modelID);
        mDataCallback = view.bindDataCallback();
        mFragmentCallback = view.bindFragmentCallback();
    }

    /**
     * 注册Presenter到DataModel,在BasePresenter中实现.
     */
    protected final void register() {
        mDataModel.addPresenter(mPresenterID, this);
    }

    /**
     * Presenter从DataModel中解绑,在BasePresenter中实现.会调用到DataModel的{@code recyclePresenter}方法进行清理工作.
     */
    protected final void unRegister() {
        mDataModel.removePresenter(mPresenterID);
    }

    @Override
    public String toString() {
        return mPresenterID;
    }

    /**
     * 创建Presenter的时候会自动调用.先从DataModelManager缓存中查找,如果没有则新建一个.
     *
     * @param modelID
     * @return
     */
    private BaseDataModel getDataModel(@NonNull final String modelID) {
        logger.d("getDataModel " + modelID);
        BaseDataModel dataModel = DataModelManager.getInstance().getDataModel(modelID);
        if (dataModel == null) {
            switch (modelID) {
                case ModelID.EXAMPLE_MODEL_ID:
                    dataModel = new ExampleDataModel(modelID);
                    break;
                case ModelID.LEANBACK_MODEL_ID:
                    dataModel = new LeanbackDataModel(modelID);
                    break;
                default:
                    break;
            }
        }
        return dataModel;
    }
}
