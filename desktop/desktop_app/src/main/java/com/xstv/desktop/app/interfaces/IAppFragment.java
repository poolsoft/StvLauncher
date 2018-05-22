package com.xstv.desktop.app.interfaces;

import com.xstv.desktop.app.widget.AppFolderWorkspace;
import com.xstv.desktop.app.widget.AppWorkspace;

public interface IAppFragment {

    AppWorkspace getAppWorkspace();

    AppFolderWorkspace getFolderWorkspace();

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
