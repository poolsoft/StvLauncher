
package com.xstv.desktop.app.interfaces;

import com.xstv.desktop.app.db.ItemInfo;

import java.util.List;

public interface IDownloadApp {

    boolean bindService();

    void unbindService();

    boolean isContain(String pkg);

    boolean isContainCibn(String pkg);

    void installApp(ItemInfo itemInfo);

    void updateApp(ItemInfo itemInfo, int versionCode);

    void cancleDownloadApp(ItemInfo itemInfo);

    void putPreloadApp(ItemInfo itemInfo);

    void resetStatusIfInstallFail(String packageName);

    void installSuccess(String pkg);

    void installSuccess(ItemInfo itemInfo);

    List<ItemInfo> getExistPreload(String pkg);

    ItemInfo getExistCibnPreload(String pkg);

    void removePreloadIfNeed(ItemInfo itemInfo);

    void removePreloadByPkg(String packageName);

    void removeInvalidPreload();

    boolean isShowShade(String statusStr);

    void release();

    void crush();
}
