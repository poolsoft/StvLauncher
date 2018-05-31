package com.xstv.library.base.model;

import android.support.annotation.NonNull;
import android.util.ArrayMap;

import java.util.Map;

/**
 * @author wuh
 * @date 18-5-28 下午4:09
 * @describe DataModelManager 数据管理类.
 * <p>
 * 功能：
 * 1.统一管理所有桌面的数据获取和返回;
 * 2.管理桌面数据的加载顺序和时机;
 * 3.统一网络调用底层功能;
 * 4.统一线程处理,各个DataModel不需要再创建线程池或者线程.
 * <p>
 * 调用过程:
 * Presenter----->DataModel----->DataModelManager
 * TODO: 其他功能
 */

public class DataModelManager {
    Map<String, BaseDataModel> mModelMap = new ArrayMap<String, BaseDataModel>(5);

    private static volatile DataModelManager mInstance;

    private DataModelManager() {
    }

    public static synchronized DataModelManager getInstance() {
        if (null == mInstance) {
            synchronized (DataModelManager.class) {
                mInstance = new DataModelManager();
            }
        }
        return mInstance;
    }

    /**
     * 通过id查找model并返回,如果没找到则返回为 null.
     *
     * @param id Model的ID,不能为null
     * @return null或者model
     */
    public BaseDataModel getDataModel(@NonNull String id) {
        if (id == null) {
            return null;
        }
        return mModelMap.get(id);
    }

    /**
     * 添加Model到DataManager
     *
     * @param id            Model的唯一标识,通常是包名+类名,不能为 null或者空字符串.
     * @param baseDataModel
     */
    public void addModel(@NonNull String id, @NonNull BaseDataModel baseDataModel) {
        if (id == null || baseDataModel == null) {
            return;
        }
        if (!mModelMap.containsKey(id)) {
            mModelMap.put(id, baseDataModel);
        }
    }

    /**
     * 桌面销毁时调用此方法来释放资源.释放DataModel在Manager中的资源.
     *
     * @param id Model的ID,不能为null.
     */
    public void removeModel(@NonNull String id) {
        if (id == null) {
            return;
        }
        BaseDataModel model = mModelMap.remove(id);
        if (model != null) {
            //TODO:清理工作
        }
    }

}