package com.stv.plugin.demo.data.common;

public interface OnDataChangedListener {

    void onDataInitialize(PosterHolder posterDefault);

    void onDataChange(PosterHolder posterCache);

    void onRefreshTimeLess(int time);

}
