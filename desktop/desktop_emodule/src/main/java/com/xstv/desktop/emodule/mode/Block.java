package com.xstv.desktop.emodule.mode;

import java.util.ArrayList;

public class Block<T> extends DisplayItem {

    public ArrayList<T> items;

    @Override
    public String toString() {
        return "Block{" +
                "title='" + title + '\'' +
                ", UI='" + (ui != null ? ui.ui_type : "null") + '\'' +
                ", items='" + items + '\'' +
                '}';
    }
}
