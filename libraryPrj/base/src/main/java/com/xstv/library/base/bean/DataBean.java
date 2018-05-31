package com.xstv.library.base.bean;

public class DataBean {

    String tag;

    public DataBean(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return tag;
    }
}
