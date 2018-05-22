
package com.xstv.desktop.app.util;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;

import com.xstv.base.LetvLog;
import com.xstv.desktop.app.AppPluginActivator;
import com.xstv.desktop.app.R;
import com.xstv.desktop.app.bean.AppIconBean;
import com.xstv.desktop.app.bean.FolderInfo;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.model.AppDataModel;

import java.util.List;

public class IconFilterUtil {
    public static final String TAG = "IconFilterUtil";
    private static boolean isTheme;

    private static int sWidth;
    private static int sHeight;

    static {
        Resources r = AppPluginActivator.getContext().getResources();
        sWidth = r.getDimensionPixelSize(R.dimen.poster_workspace_item_width1);
        sHeight = r.getDimensionPixelSize(R.dimen.poster_workspace_item_height2);

        try {
            if (Utilities.isVersion50s()) {
                isTheme = isUsedThemeUseOldApi();
            } else {
                isTheme = isUsedThemeUseNewApi();
            }
        } catch (NoClassDefFoundError ex) {
            LetvLog.e(TAG, "isUsedTheme NoClassDefFoundError ", ex);
        } catch (Exception ex) {
            LetvLog.e(TAG, "isUsedTheme error ", ex);
        }
    }

    public static boolean isUsedTheme() {
        return isTheme;
    }

    private static boolean isUsedThemeUseNewApi() {
        return false;
    }

    private static boolean isUsedThemeUseOldApi() {
        return false;
    }

    public static boolean hasIconFromSystem(String packageName) {
        boolean hasIcon = false;
        if (packageName == null) {
            return false;
        }
        try {
            if (Utilities.isVersion50s()) {
                hasIcon = hasIconFromSystemUseOldApi(packageName);
            } else {
                hasIcon = hasIconFromSystemUseNewApi(packageName);
            }
        } catch (NoClassDefFoundError error) {
            LetvLog.e(TAG, "hasIconFromSystem NoClassDefFoundError", error);
        } catch (Exception ex) {
            LetvLog.e(TAG, "hasIconFromSystem error", ex);
        }
        return hasIcon;
    }

    private static boolean hasIconFromSystemUseNewApi(String packageName) {
        return false;
    }

    private static boolean hasIconFromSystemUseOldApi(String packageName) {
        return false;
    }

    public static AppIconBean createIconBitmap(ItemInfo itemInfo) {
        return createIconBean(itemInfo, false);
    }

    public static AppIconBean createIconAndBgBitmap(ItemInfo itemInfo) {
        return createIconBean(itemInfo, true);
    }

    public static void createFolderItemIconBitmap(FolderInfo folderInfo) {
        List<ItemInfo> itemList = folderInfo.getContents();
        if (itemList == null || itemList.size() == 0) {
            return;
        }
        for (ItemInfo itemInfo : itemList) {
            createIconBitmap(itemInfo);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static AppIconBean createIconBean(ItemInfo itemInfo, boolean isNeedBg) {
        Context context = AppPluginActivator.getContext();
        long id = itemInfo.getId();
        AppIconBean iconBean = BitmapCache.getInstance().getBitmapFromMemCache(id);
        if (iconBean == null) {
            Drawable appIcon = getIconDrawable(context, itemInfo);
            if (appIcon == null) {
                return null;
            }
            AppIconBean appIconBean = new AppIconBean();
            Bitmap iconBitmap = BitmapUtil.getPixelAreaOfBitmap(BitmapUtil.drawableToBitmap(appIcon));
            appIconBean.setIconBitmap(iconBitmap);
            if (isNeedBg && iconBitmap != null) {
                Bitmap blurBitmap = BitmapUtil.blur(context, iconBitmap, sWidth, sHeight);
                appIconBean.setBlurBgBitmap(blurBitmap);
            }
            LetvLog.d(TAG, "createIconAndBgBitmap put cache title = " + itemInfo.getTitle());
            BitmapCache.getInstance().addBitmapToMemoryCache(id, appIconBean);
            return appIconBean;
        } else {
            Bitmap iconBitmap = iconBean.getIconBitmap();
            if (iconBitmap == null || iconBitmap.isRecycled()) {
                Drawable appIcon = getIconDrawable(context, itemInfo);
                if (appIcon == null) {
                    return iconBean;
                }
                iconBitmap = BitmapUtil.getPixelAreaOfBitmap(BitmapUtil.drawableToBitmap(appIcon));
                iconBean.setIconBitmap(iconBitmap);
            }
            if (isNeedBg) {
                Bitmap blurBitmap = iconBean.getBlurBgBitmap();
                if (blurBitmap == null || blurBitmap.isRecycled()) {
                    if (iconBitmap != null) {
                        blurBitmap = BitmapUtil.blur(context, iconBitmap, sWidth, sHeight);
                        iconBean.setBlurBgBitmap(blurBitmap);
                    }
                }
            }
            return iconBean;
        }
    }

    public static Drawable getIconDrawable(Context context, ItemInfo itemInfo) {
        Drawable appIcon = null;
        PackageManager pm = context.getPackageManager();
        try {
            if (itemInfo.getType() == AppDataModel.ITEM_TYPE_PRELOADED) {
                appIcon = AppDataModel.getInstance().getPreLoadedAppIcon(itemInfo.getPackageName());
            } else if (itemInfo.getType() == AppDataModel.ITEM_TYPE_SHORTCUT) {
                byte[] iconByte = itemInfo.getShortcutIcon();
                LetvLog.d(TAG, "getIconDrawable iconByte = " + iconByte);
                if (iconByte != null) {
                    Bitmap icon = BitmapFactory.decodeByteArray(iconByte, 0, iconByte.length);
                    appIcon = new BitmapDrawable(icon);
                } else if (!TextUtils.isEmpty(itemInfo.getShortcutResourseName())) {
                    Resources resources = pm.getResourcesForApplication(itemInfo.getPackageName());
                    if (resources != null) {
                        final int id = resources.getIdentifier(itemInfo.getShortcutResourseName(), null, null);
                        appIcon = resources.getDrawable(id);
                    }
                }
            } else {
                appIcon = pm.getActivityIcon(new ComponentName(itemInfo.getPackageName(), itemInfo.getClassName()));
                LetvLog.d(TAG, " getIconDrawable getActivityIcon = " + appIcon);
                if (appIcon == null) {
                    appIcon = pm.getApplicationIcon(itemInfo.getPackageName());
                }
            }
        } catch (Exception e) {
            LetvLog.d(TAG, " getIconDrawable catch error, getBitmapFromApp fetchFailed !!!!", e);
        }

        if (appIcon == null) {
            appIcon = context.getPackageManager().getDefaultActivityIcon();
        }
        if (appIcon == null || !(appIcon instanceof BitmapDrawable)) {
            LetvLog.w(TAG, " getIconDrawable, icon is null or not BitmapDrawable error");
            return null;
        }
        return appIcon;
    }
}
