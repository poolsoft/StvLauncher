
package com.xstv.desktop.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.xstv.base.LetvLog;
import com.xstv.desktop.app.bean.FolderInfo;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.db.ItemInfoDBHelper;
import com.xstv.desktop.app.model.AppDataModel;
import com.xstv.desktop.app.model.DataModelList;
import com.xstv.desktop.app.util.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangguanhua on 17-9-25.
 * 在桌面上添加快捷方式
 */

public class InstallShortcutReceiver extends BroadcastReceiver {

    private static final String TAG = InstallShortcutReceiver.class.getSimpleName();

    public static final String ACTION_INSTALL_SHORTCUT = "com.stv.launcher.action.INSTALL_SHORTCUT";

    private static final String LAUNCH_INTENT_KEY = "intent.launch";
    private static final String NAME_KEY = "name";
    private static final String ICON_KEY = "icon";
    private static final String ICON_RESOURCE_NAME_KEY = "iconResource";
    private static final String ICON_RESOURCE_PACKAGE_NAME_KEY = "iconResourcePackage";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_INSTALL_SHORTCUT.equals(intent.getAction())) {
            return;
        }
        praseData(context, intent);
    }

    private void praseData(Context context, Intent data) {
        Intent launchIntent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String label = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        label = ensureValidName(context, launchIntent, label).toString();
        Bitmap icon = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
        LetvLog.d(TAG, "praseData launcherIntent = " + launchIntent + " launch intent url = " + launchIntent.toUri(0));
        LetvLog.d(TAG, "praseData label = " + label + " icon = " + icon);
        if (launchIntent == null || label == null) {
            LetvLog.e(TAG, "Invalid install shortcut intent");
            return;
        }
        ItemInfo itemInfo = new ItemInfo();
        itemInfo.setShortcutIntentUrl(launchIntent.toUri(0));
        itemInfo.setTitle(label);
        String packageName = launchIntent.getPackage();
        LetvLog.d(TAG, "praseData packageName = " + packageName);
        if (!TextUtils.isEmpty(packageName)) {
            itemInfo.setPackageName(packageName);
        }
        if (icon != null) {
            byte[] iconByteArray = Utilities.flattenBitmap(icon);
            itemInfo.setShortcutIcon(iconByteArray);
        }
        Intent.ShortcutIconResource iconResource = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
        if (iconResource != null) {
            LetvLog.d(TAG, "praseData packageName = " + iconResource.packageName + " resourceName = " + iconResource.resourceName);
            itemInfo.setPackageName(iconResource.packageName);
            itemInfo.setShortcutResourseName(iconResource.resourceName);
        }
        int lastIndex = ItemInfoDBHelper.getInstance().getLastIndex();
        itemInfo.setIndex(lastIndex + 1);
        itemInfo.setType(AppDataModel.ITEM_TYPE_SHORTCUT);
        itemInfo.setOrderTimestamp(System.currentTimeMillis());
        List<ItemInfo> itemInfoList = ItemInfoDBHelper.getInstance().getShortByIntentUrl(itemInfo.getShortcutIntentUrl());
        AppDataModel.Callbacks callbacks = AppDataModel.getInstance().getCallback();
        ArrayList<ItemInfo> itemList = new ArrayList<ItemInfo>(1);
        itemList.clear();
        if (itemInfoList != null && itemInfoList.size() > 0) {
            // 数据库中有记录,更新界面
            ItemInfo updateInfo = itemInfoList.get(0);
            updateInfo.setTitle(itemInfo.getTitle());
            updateInfo.setPackageName(itemInfo.getPackageName());
            updateInfo.setShortcutIcon(itemInfo.getShortcutIcon());
            updateInfo.setShortcutResourseName(itemInfo.getShortcutResourseName());
            updateInfo.setOrderTimestamp(itemInfo.getOrderTimestamp());
            if(updateInfo != null){
                ItemInfoDBHelper.getInstance().update(updateInfo);
                itemList.add(updateInfo);
                LetvLog.d(TAG, "praseData alerady in db");
                ArrayList<ItemInfo> updateContainFolderList = handleAppUpdate(itemList);
                if(callbacks != null){
                    callbacks.bindAppUpdated(itemList, updateContainFolderList);
                }
            }
        } else {
            ItemInfoDBHelper.getInstance().insert(itemInfo);
            // 数据库中没有,直接存入数据库中,将快捷方式添加到界面上
            LetvLog.d(TAG, "praseData will add to view");
            itemList.add(itemInfo);
            if(callbacks != null){
                callbacks.bindAppAdded(itemList);
            }
        }
    }

    private ArrayList<ItemInfo> handleAppUpdate(ArrayList<ItemInfo> updates){
        ArrayList<ItemInfo> updateList = new ArrayList<ItemInfo>(updates.size());
        List<ItemInfo> allAppList = DataModelList.getInstance().allAppList;
        if (updates == null || updates.size() == 0 || allAppList == null) {
            Log.w(TAG, "handleAppUpdate is null.");
            return updateList;
        }
        for (ItemInfo update : updates) {
            if (update != null) {
                for (ItemInfo cached : allAppList) {
                    if (cached instanceof FolderInfo) {
                        FolderInfo folderInfo = (FolderInfo) cached;
                        if (folderInfo.getContents().contains(update)) {
                            int index = folderInfo.getChildrenIndex(update);
                            ItemInfo info = folderInfo.getChildrenByIndex(index);
                            // update.setBackgroundResID(info.getBackgroundResID());
                            update.setInFolderIndex(info.getInFolderIndex());
                            update.setIndex(info.getIndex());
                            update.setContainer(info.getContainer());
                            update.setContainerName(info.getContainerName());
                            update.setVersionCode(0);
                            folderInfo.getContents().set(index, update);
                            updateList.add(folderInfo);
                            break;
                        }
                    } else {
                        if (update.equals(cached)) {
                            int index = allAppList.indexOf(cached);
                            if (index != -1) {
                                allAppList.set(index, update);
                            }
                            update.setVersionCode(0);
                            updateList.add(update);
                            break;
                        }
                    }
                }
            }
        }
        LetvLog.i(TAG, " onShortcutUpdated updateList = " + updateList);
        return updateList;
    }

    /**
     * Ensures that we have a valid, non-null name. If the provided name is null, we will return the application name instead.
     */
    private CharSequence ensureValidName(Context context, Intent intent, CharSequence name) {
        if (name == null) {
            try {
                PackageManager pm = context.getPackageManager();
                ActivityInfo info = pm.getActivityInfo(intent.getComponent(), 0);
                name = info.loadLabel(pm);
            } catch (PackageManager.NameNotFoundException nnfe) {
                return "";
            }
        }
        return name;
    }
}
