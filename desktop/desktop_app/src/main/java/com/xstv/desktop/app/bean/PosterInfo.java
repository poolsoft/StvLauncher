package com.xstv.desktop.app.bean;

import com.xstv.desktop.app.db.ItemInfo;

public class PosterInfo extends ItemInfo {

    private String firstTitle;
    private String secondTitle;
    private String iconUrl;
    private String logoUrl;
    private ParamBean jumpParam;
    private String posid;
    private String posterId;
    private String promoid;
    private String reqid;

    public String getFirstTitle() {
        return firstTitle;
    }

    public void setFirstTitle(String firstTitle) {
        this.firstTitle = firstTitle;
    }

    public String getSecondTitle() {
        return secondTitle;
    }

    public void setSecondTitle(String secondTitle) {
        this.secondTitle = secondTitle;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public ParamBean getJumpParam() {
        return jumpParam;
    }

    public void setJumpParam(ParamBean jumpParam) {
        this.jumpParam = jumpParam;
    }

    public String getPosid() {
        return posid;
    }

    public void setPosid(String posid) {
        this.posid = posid;
    }

    public String getPosterId() {
        return posterId;
    }

    public void setPosterId(String posterId) {
        this.posterId = posterId;
    }

    public String getPromoid() {
        return promoid;
    }

    public void setPromoid(String promoid) {
        this.promoid = promoid;
    }

    public String getReqid() {
        return reqid;
    }

    public void setReqid(String reqid) {
        this.reqid = reqid;
    }

    @Override
    public boolean equals(Object o) {
        PosterInfo posterInfo = (PosterInfo) o;
        return posterId != null && posterId.equals(posterInfo.posterId);
    }
}
