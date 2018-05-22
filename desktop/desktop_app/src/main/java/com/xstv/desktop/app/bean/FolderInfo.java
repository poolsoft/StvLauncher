
package com.xstv.desktop.app.bean;

import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.model.AppDataModel;

import java.util.ArrayList;
import java.util.List;

public class FolderInfo extends ItemInfo {

    /**
     * The apps and shortcuts
     */
    private ArrayList<ItemInfo> contents = new ArrayList<ItemInfo>();
    /**
     * Flag is adding shortcut to this
     */
    public boolean isAdding = false;

    /**
     * Use for sort
     */
    public String mSortID = "";

    public FolderInfo() {
        setType(AppDataModel.ITEM_TYPE_FOLDER);
    }

    public boolean add(ItemInfo item) {
        if (contents.contains(item)) {
            return false;
        }
        return contents.add(item);
    }

    public void addAll(List<ItemInfo> list) {
        if (list != null) {
            contents.addAll(list);
        }
    }

    public boolean remove(ItemInfo item) {
        return contents.remove(item);
    }

    public void clear() {
        contents.clear();
    }

    /**
     * 设置图标所属的文件夹和在文件夹中的index
     */
    public void setItemContainer() {
        int index = 0;
        for (ItemInfo bean : contents) {
            ItemInfo itemInfo = bean;
            itemInfo.setContainer(getId());
            itemInfo.setContainerName(getTitle());
            itemInfo.setIndex(getIndex());
            itemInfo.setInFolderIndex(index);
            index++;
        }
    }

    public void setTitle(String title) {
        super.setTitle(title);
    }

    public int getLength() {
        return contents.size();
    }

    public ItemInfo getChildrenByIndex(int index) {
        ItemInfo bean = null;
        if (index > -1 && index < contents.size()) {
            bean = contents.get(index);
        }
        return bean;
    }

    public int getChildrenIndex(ItemInfo child) {
        return contents.indexOf(child);
    }

    public ArrayList<ItemInfo> getContents() {
        return contents;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FolderInfo) {
            FolderInfo folderInfo = (FolderInfo) o;
            if (getId() != null && getId().equals(((FolderInfo) o).getId())) {
                return true;
            } else {
                if (contents.containsAll(folderInfo.contents)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for (ItemInfo appBean : contents) {
            if (appBean != null) {
                sb.append(appBean.getTitle() + " ");
            }
        }
        sb.append(" getTitle : " + getTitle());
        sb.append(" getID : " + getId());
        sb.append(" getIndex : " + getIndex());
        sb.append(" isAdding : " + isAdding);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        if (contents != null) {
            return contents.hashCode();
        }
        return super.hashCode();
    }
}
