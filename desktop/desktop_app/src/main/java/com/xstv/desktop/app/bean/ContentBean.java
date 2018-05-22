package com.xstv.desktop.app.bean;

import com.xstv.desktop.app.db.ItemInfo;

import java.util.List;

public class ContentBean {
    private String posid;
    private String title;
    private String subTitle;
    private int TempleteType;
    private List<ItemInfo> contentItemList;

    public String getPosid() {
        return posid;
    }

    public void setPosid(String posid) {
        this.posid = posid;
    }

    public List<ItemInfo> getContentItemList() {
        return contentItemList;
    }

    public void setContentItemList(List<ItemInfo> contentItemList) {
        this.contentItemList = contentItemList;
    }

    public int getTempleteType() {
        return TempleteType;
    }

    public void setTempleteType(int templeteType) {
        TempleteType = templeteType;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "ContentBean{" +
                "posid='" + posid + '\'' +
                ", title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
                ", TempleteType=" + TempleteType +
                ", contentItemList=" + contentItemList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContentBean that = (ContentBean) o;

        return posid.equals(that.posid);
    }

    @Override
    public int hashCode() {
        return posid.hashCode();
    }
}
