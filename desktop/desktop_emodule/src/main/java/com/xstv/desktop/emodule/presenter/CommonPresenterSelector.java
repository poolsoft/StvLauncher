package com.xstv.desktop.emodule.presenter;

import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;

import com.xstv.desktop.emodule.mode.Block;
import com.xstv.desktop.emodule.mode.DisplayItem;
import com.xstv.library.base.Logger;

import java.util.ArrayList;


public class CommonPresenterSelector extends PresenterSelector {

    Logger mLogger = Logger.getLogger("EModule", "CommonPresenterSelector");

    RegularHorizontalBlockPresenter horizontalPresenter = new RegularHorizontalBlockPresenter();
    RegularVerticalBlockPresenter verticalPresenter = new RegularVerticalBlockPresenter();
    StaggeredBlockPresenter staggeredBlockPresenter = new StaggeredBlockPresenter();
    DisplayItemPresenter displayItemPresenter = new DisplayItemPresenter();
    StaggeredItemPresenter simpleItemPresenter = new StaggeredItemPresenter();

    ArrayList<Presenter> list = new ArrayList<>();

    public CommonPresenterSelector() {
        horizontalPresenter.setPresenterSelector(this);
        verticalPresenter.setPresenterSelector(this);
        staggeredBlockPresenter.setPresenterSelector(this);
    }

    @Override
    public Presenter getPresenter(Object item) {
        Presenter presenter = proxyGetPresenter(item);
        mLogger.d("getPresenter with: " + item + " presenter=" + presenter);
        return presenter;
    }

    private Presenter proxyGetPresenter(Object item) {
        if (item instanceof Block) {
            Block block = (Block) item;
            if (block.ui.ui_type.equals(Block.UIType.T1)) {
                return horizontalPresenter;
            } else if (block.ui.ui_type.equals(Block.UIType.T2)) {
                return verticalPresenter;
            }else if (block.ui.ui_type.equals(Block.UIType.T3)) {
                return staggeredBlockPresenter;
            }
        } else if (item instanceof DisplayItem) {
            DisplayItem displayItem = (DisplayItem) item;
            if (displayItem.ui.ui_type.equals(Block.UIType.T4)) {
                return displayItemPresenter;
            } else if (displayItem.ui.ui_type.equals(Block.UIType.T5)) {
                return simpleItemPresenter;
            }
        }
        return null;
    }

    public ArrayList<Presenter> getPresentersArray() {
        return list;
    }

}
