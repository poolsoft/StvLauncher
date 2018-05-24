
package com.xstv.desktop.app.widget;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xstv.library.base.LetvLog;
import com.xstv.desktop.app.AppPluginActivator;
import com.xstv.desktop.app.R;
import com.xstv.desktop.app.bean.AppIconBean;
import com.xstv.desktop.app.bean.DownloadStatusBean;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.db.ItemInfoDBHelper;
import com.xstv.desktop.app.model.AppDataModel;
import com.xstv.desktop.app.util.BitmapUtil;
import com.xstv.desktop.app.util.IconFilterUtil;
import com.xstv.desktop.app.util.LauncherState;
import com.xstv.desktop.app.util.Utilities;

import java.util.List;

/**
 * Created by wuh on 15-8-10. The view like a icon of app or folder
 */

public class AppCellView extends CellView<ItemInfo> {
    private static final String TAG = AppCellView.class.getSimpleName();

    private static final String PRIME_SENSE = "prime_sense";

    private ImageView mIconApp;
    private TextView mLabel;

    public AppCellView(Context context) {
        this(context, null);
    }

    public AppCellView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppCellView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void initView(Context context) {
        View.inflate(context, R.layout.app_cellview_layout, this);
        mIconApp = (ImageView) findViewById(R.id.cellview_icon_app);
        mLabel = (TextView) findViewById(R.id.cellview_label);
    }

    @Override
    public void bindData(ItemInfo itemInfo) {
        super.bindData(itemInfo);
        if (itemInfo == null) {
            LetvLog.i(TAG, "bindData itemInfo is null.");
            setBackgroundResource(R.drawable.app_folder_bg);
            return;
        }
        setIcon(itemInfo);
        // LetvLog.d(TAG, "bindData itemInfo = " + itemInfo);
        updateSuperscriptView(itemInfo);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (isShowLoading) {
            if (mLabel != null && mLabel.getVisibility() == View.VISIBLE) {
                mLabel.setVisibility(View.INVISIBLE);
            }
        }else {
            if (mLabel != null && mLabel.getVisibility() != View.VISIBLE) {
                mLabel.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onLoadingFinished() {
        super.onLoadingFinished();
        if (mLabel != null && mLabel.getVisibility() != View.VISIBLE) {
            mLabel.setVisibility(View.VISIBLE);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setIcon(ItemInfo itemInfo) {
        if (IconFilterUtil.isUsedTheme()) {
            setIconUseTheme(itemInfo);
            return;
        }
        setIconDefault(itemInfo);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setIconDefault(ItemInfo itemInfo) {
        if (Utilities.isNeedBlur()) {
            AppIconBean appIconBean = IconFilterUtil.createIconAndBgBitmap(itemInfo);
            if (appIconBean != null) {
                mIconApp.setImageBitmap(appIconBean.getIconBitmap());
                setBackground(new BitmapDrawable(appIconBean.getBlurBgBitmap()));
            }
        } else {
            Drawable iconDrawable = IconFilterUtil.getIconDrawable(mContext, itemInfo);
            Bitmap iconBitmap = BitmapUtil.drawableToBitmap(iconDrawable);
            if(!Utilities.isPlatform648()){
                iconBitmap = BitmapUtil.getPixelAreaOfBitmap(iconBitmap);
            }
            mIconApp.setImageBitmap(iconBitmap);
            setBackgroundResource(R.drawable.app_folder_bg);
        }
        Utilities.setShadowZ(this, 8f);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void setIconUseTheme(ItemInfo itemInfo) {
        Bitmap iconBitmap = null;
        if (Utilities.isNeedBlur()) {
            AppIconBean appIconBean = IconFilterUtil.createIconBitmap(itemInfo);
            if (appIconBean != null) {
                iconBitmap = appIconBean.getIconBitmap();
            }
        } else {
            Drawable appIcon = IconFilterUtil.getIconDrawable(mContext, itemInfo);
            iconBitmap = BitmapUtil.drawableToBitmap(appIcon);
        }

        if (IconFilterUtil.hasIconFromSystem(itemInfo.getPackageName())) {
            setBackground(new BitmapDrawable(iconBitmap));
            if (mIconApp != null) {
                mIconApp.setVisibility(GONE);
            }
        } else {
            // 没有适配的图标使用统一的背景
            Resources resources = AppPluginActivator.getContext().getResources();// Must use plugin context
            Drawable bg = resources.getDrawable(R.drawable.app_cellview_usetheme_bg);
            LetvLog.d(TAG, " get backgroud from theme bg");
            if (mIconApp != null) {
                mIconApp.setImageBitmap(iconBitmap);
                mIconApp.setVisibility(View.VISIBLE);
            }
            setBackground(bg);
        }
    }

    @Override
    public void setLabel(ItemInfo itemInfo) {
        super.setLabel(itemInfo);
        mLabel.setVisibility(View.VISIBLE);
        mLabel.setText(itemInfo.getTitle());
    }

    @Override
    public void openCellView() {
        LetvLog.d(TAG, "openCellView mItemInfo = " + mItemInfo);
        if(mItemInfo == null){
            return;
        }
        startActivitySafely();
        //记录打开的时间
        ItemInfo itemInfo = null;
        if(mItemInfo.getType() == AppDataModel.ITEM_TYPE_SHORTCUT){
            List<ItemInfo> itemInfoList = ItemInfoDBHelper.getInstance().getShortByIntentUrl(
                    mItemInfo.getShortcutIntentUrl());
            if(itemInfoList != null && itemInfoList.size() > 0){
                itemInfo = itemInfoList.get(0);
            }
        }else {
            itemInfo = ItemInfoDBHelper.getInstance().getShortByComponent(
                    mItemInfo.getComponentNameStr());
        }
        if(itemInfo != null){
            itemInfo.setOrderTimestamp(System.currentTimeMillis());
            ItemInfoDBHelper.getInstance().update(itemInfo);
        }
    }

    @Override
    public void removeCellView() {
        if (isSystemApp()) {
            Toast.makeText(getContext(), getResources().getText(R.string.can_not_rm_system_app),
                    Toast.LENGTH_SHORT).show();
        } else {
            if (mItemInfo != null) {
                if (mItemInfo.getType() == AppDataModel.ITEM_TYPE_APPLICATION) {
                    uninstallApp(LauncherState.getInstance().getHostContext(), mItemInfo.getPackageName());
                } else if (mItemInfo.getType() == AppDataModel.ITEM_TYPE_PRELOADED) {
                    if(Utilities.checkApkExist(mContext, mItemInfo.getPackageName())){
                        PackageManager packageManager = mContext.getPackageManager();
                        Intent mainIntent = packageManager.getLaunchIntentForPackage(mItemInfo.getPackageName());
                        ComponentName componentName = mainIntent.getComponent();
                        if (componentName != null) {
                            String className = componentName.getClassName();
                            mItemInfo.setType(AppDataModel.ITEM_TYPE_APPLICATION);
                            mItemInfo.setClassName(className);
                            mItemInfo.setComponentNameStr(componentName.flattenToString());
                            mItemInfo.init();
                        }
                        uninstallApp(LauncherState.getInstance().getHostContext(), mItemInfo.getPackageName());
                        return;
                    }
                    DownloadStatusBean downloadStatusBean = mItemInfo.getDownloadStatusBean();
                    if (downloadStatusBean.getDownloadStatus() != null && downloadStatusBean.getCurrentBytes() > 0) {
                        Toast.makeText(mContext, downloadStatusBean.getLoadingTitle(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AppDataModel.getInstance().removePreloadedApp(mItemInfo);
                }else if(mItemInfo.getType() == AppDataModel.ITEM_TYPE_SHORTCUT){//快捷方式
                    AppDataModel.getInstance().removeShortcutApp(mItemInfo);
                }
            }
        }
    }

    public boolean isSystemApp() {
        boolean isSystemApp = false;
        if (mItemInfo != null) {
            if ((mItemInfo.getFlags() & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                isSystemApp = false;
            } else if ((mItemInfo.getFlags() & ApplicationInfo.FLAG_SYSTEM) != 0) {
                isSystemApp = true;
            } else {
                isSystemApp = false;
            }
        }
        return isSystemApp;
    }

    /**
     * 卸载一个应用
     *
     * @param context
     * @param packageName
     */
    public static void uninstallApp(Context context, String packageName) {
        Intent intent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:"
                + packageName));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            LetvLog.d(TAG, " uninstallApp catch an Exception... intent = " + intent + "  ", e);
        }
    }

    @Override
    public void setDeleteState(boolean isFocus) {
        super.setDeleteState(isFocus);
        if (isSystemApp()) {
            setMaskView();
            setEditIcon(-1);
        } else {
            if (isFocus) {
                setMaskView();
                setEditIcon(R.drawable.ic_home_app_delete);
            } else {
                removeMaskView();
                setEditIcon(-1);
            }
        }
    }

    @Override
    public void resetDeleteState() {
        super.resetDeleteState();
        if (!isSystemApp()) {
            removeMaskView();
            setEditIcon(-1);
        }
    }

    @Override
    public void setNewFolderState(boolean isFocus) {
        super.setNewFolderState(isFocus);
        setMaskView();
        setEditIcon(R.drawable.ic_home_app_add);
    }

    @Override
    public void resetState() {
        super.resetState();
        setImageViewAlpha(1.0f);
        removeMaskView();
        setEditIcon(-1);
        canMove = false;
        updateArrow(false);
    }

    private void setImageViewAlpha(float alpha) {
        if (mLabel != null) {
            mLabel.setAlpha(alpha);
        }
        if (mIconApp != null) {
            mIconApp.setAlpha(alpha);
        }
    }

    @Override
    public void setAddState() {
        super.setAddState();
        setNewFolderState(false);
    }

    @Override
    protected Point getLoadingTextPoint() {
        Point point = new Point();
        if (mLabel != null) {
            int x = getWidth();
            int y = mLabel.getTop() + mLabel.getBaseline();
            point.set(x, y);
        }
        return point;
    }

    @Override
    protected float getLoadingTextSize() {
        float textSize = 0;
        if (mLabel != null) {
            textSize = mLabel.getTextSize();
        }
        return textSize;
    }

    @Override
    protected String getLoadingText() {
        if (mItemInfo == null) {
            return "";
        }

        DownloadStatusBean downloadStatusBean = mItemInfo.getDownloadStatusBean();
        if(downloadStatusBean == null){
            return "";
        }
        return downloadStatusBean.getLoadingTitle();
    }

    @Override
    public void recycle(boolean isCleanView){
        if(mIconApp != null){
            mIconApp.setImageBitmap(null);
        }
        setBackground(null);
    }
}
