package com.xstv.launcher.provider.db;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 

import android.text.TextUtils;

import com.xstv.launcher.logic.manager.DataModel;
import com.xstv.library.base.LetvLog;

import java.util.ArrayList;

/**
 * Entity mapped to table SCREEN_INFO.
 */
public class ScreenInfo {
    public static final String PLUGIN_STATE_CRASH = "crash";
    public static final String PLUGIN_STATE_AVAILABLE = "available";
    public static final String PLUGIN_STATE_OFFLINE = "offline";
    public static final String PLUGIN_STATE_LOADERROR = "load_error";

    public static final String OFFLINE_STATE_NOCHANGE = "nochange";
    public static final String OFFLINE_STATE_HASCHANGE = "haschange";

    private static final String TAG = ScreenInfo.class.getSimpleName();
    private Long id;
    private String tag;
    private String name;
    private Boolean local;
    private Boolean sortable;
    private Boolean removable;
    private String pluginUrl;
    private Integer pluginId;
    private String pluginPath;
    private String pluginType;
    private String pluginSize;
    private String pluginState;
    private String updateType;
    private String versionName;
    // 44s
    private Integer screenOrder;
    /**
     * 多语言 tag名称，JSON格式 viewName:{"viewName": [ { "lang": "zh-CN","content": "乐见" }, { "lang": "en-US", "content": "LeView" } ]}
     * {@link com.stv.plugin.upgrade.model.BaseInfo#getViewNameJson()}
     */
    private String screenTag;
    private String dependModel;
    private String versionCode;
    private String fileName;
    private String packageName;
    private String tabName;
    private String baseVersion;
    private String updateVersionCode;
    private Boolean showOnTab;
    private String fileSize;
    private String md5;
    /**
     * 插件角标图 ,hot 等角标 {@link com.stv.plugin.upgrade.model.BaseInfo#getDesktopCornerPic()}
     */
    private String tagType;
    /**
     * 插件描述，JSON格式  description:{"description" :""} {@link com.stv.plugin.upgrade.model.BaseInfo#getDescription()}
     */
    private String describe;
    /**
     * 使用中的桌面的icon {@link com.stv.plugin.upgrade.model.BaseInfo#getDesktopicon_selected()}
     */
    private String iconUrl;
    /**
     * 插件效果图 {@link com.stv.plugin.upgrade.model.BaseInfo#getDesktopEffectPic()}
     */
    private String imageUrl;
    /**
     * 更新日志，JSON格式 updateComment:{"updateComment" :""} {@link com.stv.plugin.upgrade.model.UpgradeRecord#getUpdateComment()}
     */
    private String updateInfo;
    private Boolean locked;
    private Boolean isNew;
    private Integer hot;
    private String notSupport;
    private String mark1;
    /**
     * 待添加桌面的icon的图标 {@link com.stv.plugin.upgrade.model.BaseInfo#desktopicon_unselected}
     */
    private String mark2;
    private String mark3;
    private String mark4;
    //57s
    /**服务端下发的插件位置*/
    private Integer position;
    /**插件是否使用过，在开机加速和桌面管理中操作完，如果showOnTable为true则表明用过*/
    private Boolean hasUsed;
    /**插件下线时候的插件顺序快照*/
    private String offlineShot;
    /**强制更新正在显示的桌面位置,需要先判断是否强制显示*/
    public boolean forceChangeTab;
    /**如果插件没有用过是否强制显示*/
    public boolean forceAddToTable;

    private boolean forceUpdateFromXml;

    public ScreenInfo() {
    }

    public ScreenInfo(Long id) {
        this.id = id;
    }

    public ScreenInfo(Long id, String tag, String name, Boolean local, Boolean sortable, Boolean removable, String pluginUrl, Integer pluginId, String pluginPath, String pluginType, String pluginSize, String pluginState, String updateType, String versionName, Integer screenOrder, String screenTag, String dependModel, String versionCode, String fileName, String packageName, String tabName, String baseVersion, String updateVersionCode, Boolean showOnTab, String fileSize, String md5, String tagType, String describe, String iconUrl, String imageUrl, String updateInfo, Boolean locked, Boolean isNew, Integer hot, String notSupport, String mark1, String mark2, String mark3, String mark4, Integer position, Boolean hasUsed, String offlineShot) {
        this.id = id;
        this.tag = tag;
        this.name = name;
        this.local = local;
        this.sortable = sortable;
        this.removable = removable;
        this.pluginUrl = pluginUrl;
        this.pluginId = pluginId;
        this.pluginPath = pluginPath;
        this.pluginType = pluginType;
        this.pluginSize = pluginSize;
        this.pluginState = pluginState;
        this.updateType = updateType;
        this.versionName = versionName;
        this.screenOrder = screenOrder;
        this.screenTag = screenTag;
        this.dependModel = dependModel;
        this.versionCode = versionCode;
        this.fileName = fileName;
        this.packageName = packageName;
        this.tabName = tabName;
        this.baseVersion = baseVersion;
        this.updateVersionCode = updateVersionCode;
        this.showOnTab = showOnTab;
        this.fileSize = fileSize;
        this.md5 = md5;
        this.tagType = tagType;
        this.describe = describe;
        this.iconUrl = iconUrl;
        this.imageUrl = imageUrl;
        this.updateInfo = updateInfo;
        this.locked = locked;
        this.isNew = isNew;
        this.hot = hot;
        this.notSupport = notSupport;
        this.mark1 = mark1;
        this.mark2 = mark2;
        this.mark3 = mark3;
        this.mark4 = mark4;
        this.position = position;
        this.hasUsed = hasUsed;
        this.offlineShot = offlineShot;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getLocal() {
        return checkBoolean(local);
    }

    public void setLocal(Boolean local) {
        this.local = local;
    }

    public Boolean getSortable() {
        return checkBoolean(sortable);
    }

    public void setSortable(Boolean sortable) {
        this.sortable = sortable;
    }

    public Boolean getRemovable() {
        return checkBoolean(removable);
    }

    public void setRemovable(Boolean removable) {
        this.removable = removable;
    }

    public String getPluginUrl() {
        return pluginUrl;
    }

    public void setPluginUrl(String pluginUrl) {
        this.pluginUrl = pluginUrl;
    }

    public Integer getPluginId() {
        return pluginId;
    }

    public void setPluginId(Integer pluginId) {
        this.pluginId = pluginId;
    }

    public String getPluginPath() {
        return pluginPath;
    }

    public void setPluginPath(String pluginPath) {
        this.pluginPath = pluginPath;
    }

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    public String getPluginSize() {
        return pluginSize;
    }

    public void setPluginSize(String pluginSize) {
        this.pluginSize = pluginSize;
    }

    public String getPluginState() {
        return pluginState;
    }

    public void setPluginState(String pluginState) {
        this.pluginState = pluginState;
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public Integer getScreenOrder() {
        return screenOrder;
    }

    public void setScreenOrder(Integer screenOrder) {
        this.screenOrder = screenOrder;
    }

    /**
     * 多语言 tag名称，JSON格式 viewName:{"viewName": [ { "lang": "zh-CN","content": "乐见" }, { "lang": "en-US", "content": "LeView" } ]}
     */
    public String getScreenTag() {
        return screenTag;
    }

    public void setScreenTag(String screenTag) {
        this.screenTag = screenTag;
    }

    public String getDependModel() {
        return dependModel;
    }

    public void setDependModel(String dependModel) {
        this.dependModel = dependModel;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public String getBaseVersion() {
        return baseVersion;
    }

    public void setBaseVersion(String baseVersion) {
        this.baseVersion = baseVersion;
    }

    public String getUpdateVersionCode() {
        return updateVersionCode;
    }

    public void setUpdateVersionCode(String updateVersionCode) {
        this.updateVersionCode = updateVersionCode;
    }

    public Boolean getShowOnTab() {
        return checkBoolean(showOnTab);
    }

    public void setShowOnTab(Boolean showOnTab) {
        this.showOnTab = showOnTab;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUpdateInfo() {
        return updateInfo;
    }

    public void setUpdateInfo(String updateInfo) {
        this.updateInfo = updateInfo;
    }

    public Boolean getLocked() {
        return checkBoolean(locked);
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Boolean getIsNew() {
        return checkBoolean(isNew);
    }

    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }

    public Integer getHot() {
        return hot;
    }

    public void setHot(Integer hot) {
        this.hot = hot;
    }

    public String getNotSupport() {
        return notSupport;
    }

    public void setNotSupport(String notSupport) {
        this.notSupport = notSupport;
    }

    public String getMark1() {
        return mark1;
    }

    public void setMark1(String mark1) {
        this.mark1 = mark1;
    }

    public String getMark2() {
        return mark2;
    }

    public void setMark2(String mark2) {
        this.mark2 = mark2;
    }

    public String getMark3() {
        return mark3;
    }

    public void setMark3(String mark3) {
        this.mark3 = mark3;
    }

    public String getMark4() {
        return mark4;
    }

    public void setMark4(String mark4) {
        this.mark4 = mark4;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Boolean getHasUsed() {
        return checkBoolean(hasUsed);
    }

    public void setHasUsed(Boolean hasUsed) {
        this.hasUsed = hasUsed;
    }

    public String getOfflineShot() {
        return offlineShot;
    }

    public void setOfflineShot(String offlineShot) {
        this.offlineShot = offlineShot;
    }


    /**
     * Only temp variables
     */
    public int mSortLevel;
    public boolean isFristIn;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(" ScreenInfo [name= ");
        builder.append(name);
        builder.append(" packageName=");
        builder.append(packageName);
        builder.append(" versionCode=");
        builder.append(versionCode);
        builder.append(" showOnTab=");
        builder.append(showOnTab);
        builder.append(" isNew=");
        builder.append(isNew);
        builder.append(" locked=");
        builder.append(locked);
        builder.append(" pluginState = ");
        builder.append(pluginState);
        builder.append(" md5 = ");
        builder.append(md5);
        builder.append(" fileName=");
        builder.append(fileName);
        builder.append(" position = ");
        builder.append(position);
        builder.append(" screenOrder = ");
        builder.append(screenOrder);
        builder.append(" hasUsed = ");
        builder.append(hasUsed);
        builder.append(" offlineShot = ");
        builder.append(offlineShot);
        builder.append("] ");
        return builder.toString();
    }

    private boolean checkBoolean(Boolean b) {
        if (b == null) {
            return false;
        }
        return b;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ScreenInfo) {
            if (!TextUtils.isEmpty(((ScreenInfo) o).packageName) && ((ScreenInfo) o).packageName.equals(this.packageName)) {
                return true;
            }
            if (!TextUtils.isEmpty(((ScreenInfo) o).tag) && ((ScreenInfo) o).tag.equals(this.tag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Only update parameter in xml
     <sortable></sortable>
     <removable></removable>
     <pluginId></pluginId>
     <tag></tag>
     <fileName></fileName>
     <versionCode></versionCode>
     <versionName></versionName>
     <packageName></packageName>
     <dependModel></dependModel>
     <pluginType></pluginType>
     <order></order>
     <showOnTab></showOnTab>
     <local></local>
     {@link DataModel#compareWithXml(ArrayList, ArrayList)}
     * @param screenOfXML
     */
    public void updateInfoFromXml(ScreenInfo screenOfXML) {
        LetvLog.i(TAG, " updateInfoFromXml screenOfXML = " + screenOfXML);
        if (screenOfXML == null) {
            return;
        }
        this.sortable = screenOfXML.getSortable();
        this.removable = screenOfXML.getRemovable();
        this.pluginId = screenOfXML.getPluginId();
        this.tag = screenOfXML.getTag();
        this.packageName = screenOfXML.getPackageName();
        this.fileName = screenOfXML.getFileName();
        this.versionCode = screenOfXML.getVersionCode();
        this.versionName = screenOfXML.getVersionName();
        this.md5 = screenOfXML.getMd5();
        this.notSupport = screenOfXML.getNotSupport();
        this.pluginType = screenOfXML.getPluginType();
        this.dependModel = screenOfXML.getDependModel();
        LetvLog.i(TAG, " updateInfoFromXml isForceUpdateFromXml = " + screenOfXML.isForceUpdateFromXml());
        if (screenOfXML.isForceUpdateFromXml()) {
            this.screenOrder = screenOfXML.getScreenOrder();
            this.showOnTab = screenOfXML.getShowOnTab();
        }
        this.local = screenOfXML.getLocal();

//        this.pluginUrl = screenOfXML.getPluginUrl();
//        this.describe = screenOfXML.getDescribe();
//        this.screenTag = screenOfXML.getScreenTag();
//        this.updateInfo = screenOfXML.getUpdateInfo();
//        this.updateType = screenOfXML.getUpdateType();
    }

    /**
     * Only use to update screen info from server
     * {@linkplain DataModel#compareWithServer(ArrayList)}}
     * 
     * @param screenOfServer
     */
    public void updateInfoFromServer(ScreenInfo screenOfServer) {
        LetvLog.i(TAG, " updateInfoFromServer screenOfXML = " + screenOfServer);
        this.packageName = screenOfServer.getPackageName();
        this.fileName = screenOfServer.getFileName();
        this.updateType = screenOfServer.getUpdateType();
        this.versionName = screenOfServer.getVersionName();
        this.versionCode = screenOfServer.getVersionCode();
        this.describe = screenOfServer.getDescribe();
        this.screenTag = screenOfServer.getScreenTag();
        this.updateInfo = screenOfServer.getUpdateInfo();
        this.isNew = screenOfServer.getIsNew();
        this.md5 = screenOfServer.getMd5();
        this.pluginUrl = screenOfServer.getPluginUrl();
        this.tagType = screenOfServer.getTagType();
        this.iconUrl = screenOfServer.getIconUrl();
        this.imageUrl = screenOfServer.getImageUrl();
//        this.position = screenOfServer.getPosition();
//        this.hasUsed = screenOfServer.getHasUsed();
//        this.offlineShot = screenOfServer.getOfflineShot();
//        this.forceAddToTable = screenOfServer.forceAddToTable;
//        this.forceChangeTab = screenOfServer.forceChangeTab;
    }

    public ScreenInfo copy(ScreenInfo info) {
        this.id = info.getId();
        this.tag = info.getTag();
        this.name = info.getName();
        this.local = info.getLocal();
        this.sortable = info.getSortable();
        this.removable = info.getRemovable();
        this.pluginUrl = info.getPluginUrl();
        this.pluginId = info.getPluginId();
        this.pluginPath = info.getPluginPath();
        this.pluginType = info.getPluginType();
        this.pluginSize = info.getPluginSize();
        this.pluginState = info.getPluginState();
        this.updateType = info.getUpdateType();
        this.versionName = info.getVersionName();
        this.screenOrder = info.getScreenOrder();
        this.screenTag = info.getScreenTag();
        this.dependModel = info.getDependModel();
        this.versionCode = info.getVersionCode();
        this.fileName = info.getFileName();
        this.packageName = info.getPackageName();
        this.tabName = info.getTabName();
        this.baseVersion = info.getBaseVersion();
        this.updateVersionCode = info.getVersionCode();
        this.showOnTab = info.getShowOnTab();
        this.fileSize = info.getFileSize();
        this.md5 = info.getMd5();
        this.tagType = info.tagType;
        this.describe = info.getDescribe();
        this.iconUrl = info.getIconUrl();
        this.imageUrl = info.getImageUrl();
        this.updateInfo = info.updateInfo;
        this.locked = info.getLocked();
        this.isNew = info.getIsNew();
        this.hot = info.getHot();
        this.notSupport = info.getNotSupport();
        this.mark1 = info.getMark1();
        this.mark2 = info.getMark2();
        this.mark3 = info.getMark3();
        this.mark4 = info.getMark4();
        this.position = info.position;
        this.hasUsed = info.hasUsed;
        this.offlineShot = info.offlineShot;
        return this;
    }

    public boolean isForceUpdateFromXml() {
        return forceUpdateFromXml;
    }

    public void setForceUpdateFromXml(boolean forceUpdateFromXml) {
        this.forceUpdateFromXml = forceUpdateFromXml;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
