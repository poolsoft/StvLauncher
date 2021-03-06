
package com.xstv.desktop.app.db;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.xstv.desktop.app.bean.DownloadStatusBean;
import com.xstv.desktop.app.model.AppDataModel;
import com.xstv.desktop.app.util.Utilities;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

/**
 * Entity mapped to table "ITEM_INFO".
 */
public class ItemInfo {

    private static final String TAG = ItemInfo.class.getSimpleName();

    /**
     * Intent extra to store the profile
     */
    public static final String EXTRA_PROFILE = "profile";
    public static final int DOWNLOADED_FLAG = 1;
    public static final int UPDATED_SYSTEM_APP_FLAG = 2;

    private Long id;
    private Integer index = -1;
    private Integer type = 0;
    private String title;
    private String packageName;
    private String className;
    private Integer flags = 0;
    private Long container = 0L;
    private String containerName;
    private Long installTime = 0L;
    private String componentNameStr;
    private Integer inFolderIndex = 0;
    private String folder_id;
    /**
     * 用于存储最近使用应用排序的timestamp
     */
    private Long orderTimestamp;
    private String shortcutIntentUrl;
    private byte[] shortcutIcon;
    private String shortcutResourseName;

    private String reserve1;
    private String reserve2;
    private String reserve3;

    public Intent intent;
    public ComponentName componentName;

    //角标显示类型
    public Integer superscriptType = -1;
    /**
     * 角标显示个数
     */
    public Integer superscriptCount = -1;

    /**
     * for app report,no save db, only temp
     */
    private int versionCode;
    private String versionName;
    private int position;

    private DownloadStatusBean downloadStatusBean;

    public ItemInfo(){
        setType(AppDataModel.ITEM_TYPE_APPLICATION);
        init();
    }

    public ItemInfo(Long id) {
        this.id = id;
    }

    public ItemInfo(PackageManager pm, ResolveInfo info) {
        // flags = initFlags(info.activityInfo.applicationInfo);
        flags = info.activityInfo.applicationInfo.flags;
        packageName = info.activityInfo.applicationInfo.packageName;
        className = info.activityInfo.name;
        componentName = new ComponentName(packageName, className);
        componentNameStr = componentName.flattenToString();
        installTime = Utilities.getPackageInstallTime(pm, packageName);
        setActivity(componentName, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        title = info.loadLabel(pm).toString();
        if (title == null) {
            title = info.activityInfo.name;
        }
    }

    public ItemInfo(Long id, Integer index, Integer type, String title, String packageName,
                    String className, Integer flags, Long container, String containerName,
                    Long installTime, String componentNameStr, Integer inFolderIndex, String folder_id,
                    Long orderTimestamp, String shortcutIntentUrl, byte[] shortcutIcon, String shortcutResourseName,
                    String reserve1, String reserve2, String reserve3) {
        this.id = id;
        this.index = index;
        this.type = type;
        this.title = title;
        this.packageName = packageName;
        this.className = className;
        this.flags = flags;
        this.container = container;
        this.containerName = containerName;
        this.installTime = installTime;
        this.componentNameStr = componentNameStr;
        this.inFolderIndex = inFolderIndex;
        this.folder_id = folder_id;
        this.orderTimestamp = orderTimestamp;
        this.shortcutIntentUrl = shortcutIntentUrl;
        this.shortcutIcon = shortcutIcon;
        this.shortcutResourseName = shortcutResourseName;
        this.reserve1 = reserve1;
        this.reserve2 = reserve2;
        this.reserve3 = reserve3;
    }


    public void init() {
        if (packageName != null && className != null) {
            componentName = new ComponentName(packageName, className);
            makeLaunchIntent();
            componentNameStr = componentName.flattenToString();
            setActivity(componentName, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }
    }

    public static int initFlags(ApplicationInfo info) {
        int appFlags = info.flags;
        int flags = 0;
        if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
            flags |= DOWNLOADED_FLAG;
            if ((appFlags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                flags |= UPDATED_SYSTEM_APP_FLAG;
            }
        }
        return flags;
    }

    final void setActivity(ComponentName componentName, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(componentName);
        intent.setFlags(launchFlags);
    }

    public Intent makeLaunchIntent() {
        if (intent == null) {
            if (componentName == null) {
                componentName = new ComponentName(packageName, className);
            }
            return new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(componentName)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }
        return intent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getFlags() {
        return flags;
    }

    public void setFlags(Integer flags) {
        this.flags = flags;
    }

    public Long getContainer() {
        return container;
    }

    /**
     * 设置所属文件夹ID
     *
     * @param container
     */
    public void setContainer(Long container) {
        this.container = container;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public Long getInstallTime() {
        return installTime;
    }

    public void setInstallTime(Long installTime) {
        this.installTime = installTime;
    }

    public String getComponentNameStr() {
        return componentNameStr;
    }

    public void setComponentNameStr(String componentNameStr) {
        this.componentNameStr = componentNameStr;
    }

    public Integer getInFolderIndex() {
        return inFolderIndex;
    }

    public void setInFolderIndex(Integer inFolderIndex) {
        this.inFolderIndex = inFolderIndex;
    }

    public String getFolder_id() {
        return folder_id;
    }

    public void setFolder_id(String folder_id) {
        this.folder_id = folder_id;
    }

    public Long getOrderTimestamp() {
        return orderTimestamp;
    }

    public void setOrderTimestamp(Long orderTimestamp) {
        this.orderTimestamp = orderTimestamp;
    }

    public String getShortcutIntentUrl() {
        return shortcutIntentUrl;
    }

    public void setShortcutIntentUrl(String shortcutIntentUrl) {
        this.shortcutIntentUrl = shortcutIntentUrl;
    }

    public byte[] getShortcutIcon() {
        return shortcutIcon;
    }

    public void setShortcutIcon(byte[] shortcutIcon) {
        this.shortcutIcon = shortcutIcon;
    }

    public String getShortcutResourseName() {
        return shortcutResourseName;
    }

    public void setShortcutResourseName(String shortcutResourseName) {
        this.shortcutResourseName = shortcutResourseName;
    }

    public String getReserve1() {
        return reserve1;
    }

    public void setReserve1(String reserve1) {
        this.reserve1 = reserve1;
    }

    public String getReserve2() {
        return reserve2;
    }

    public void setReserve2(String reserve2) {
        this.reserve2 = reserve2;
    }

    public String getReserve3() {
        return reserve3;
    }

    public void setReserve3(String reserve3) {
        this.reserve3 = reserve3;
    }

    public Integer getSuperscriptType() {
        return superscriptType;
    }

    public void setSuperscriptType(Integer superscriptType) {
        this.superscriptType = superscriptType;
    }

    public Integer getSuperscriptCount() {
        return superscriptCount;
    }

    public void setSuperscriptCount(Integer superscriptCount) {
        this.superscriptCount = superscriptCount;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public DownloadStatusBean getDownloadStatusBean() {
        return downloadStatusBean;
    }

    public void setDownloadStatusBean(DownloadStatusBean downloadStatusBean) {
        this.downloadStatusBean = downloadStatusBean;
    }

    @Override
    public String toString() {
        return "[ title = " + title + " packageName = " + packageName + " className = " + className + " type = " + type +
                " flags = " + flags + " ]";
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ItemInfo) {
            ItemInfo itemInfo = (ItemInfo) o;

            if (type == AppDataModel.ITEM_TYPE_APPLICATION) {
                return ((componentNameStr != null) && componentNameStr.equals(itemInfo.getComponentNameStr())) ||
                        (packageName != null && packageName.equals(itemInfo.packageName) && className != null && className.equals(itemInfo.className));
            } else if (type == AppDataModel.ITEM_TYPE_PRELOADED) {
                return packageName != null && packageName.equals(itemInfo.packageName);
            } else if (type == AppDataModel.ITEM_TYPE_SHORTCUT) {
                return shortcutIntentUrl != null && shortcutIntentUrl.equals(itemInfo.shortcutIntentUrl);
            }
            return false;
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        // id must unique and different
        if (id != null) {
            return id.hashCode();
        }
        return super.hashCode();
    }
}
