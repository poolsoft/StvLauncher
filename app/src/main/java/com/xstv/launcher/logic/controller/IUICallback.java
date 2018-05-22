package com.xstv.launcher.logic.controller;

import com.xstv.launcher.provider.db.ScreenInfo;

import java.util.List;

/**
 * Created by wuh on 16-3-23.
 * Notify view or activity plugin has changed,so some view or activity should implements this interface.
 */
public interface IUICallback {

    /**
     * Notify ui start load.
     */
    void startLoad();

    /**
     * Notify ui plugin is loading.
     *
     * @param loadingList
     */
    void onLoad(List<ScreenInfo> loadingList);

    /**
     * Notify ui plugin load finished.
     */
    void finishLoad();

    /**
     * Notify ui plugin is added.
     *
     * @param addList : list of plugin which to add
     */
    void add(List<ScreenInfo> addList);

    /**
     * Notify ui plugin is updated.
     *
     * @param updateList : list of plugin which to update
     */
    void update(List<ScreenInfo> updateList);

    /**
     * Notify ui plugin is removed.
     *
     * @param pluginIDList : list of plugin which to removed
     */
    void remove(List<String> pluginIDList);

    /**
     * Notify ui plugin lock state has changed
     * @param changedList ： list of plugin which lock state has changed
     */
    void changeLock(List<ScreenInfo> changedList);

    /**
     * Notify ui plugin show red dot
     */
    void showRedDot(String showList);

    /**
     * Notify ui boot video duration
     * @param second boot video second
     */
    void getBootVideoDuration(int second);
}
