
package com.xstv.desktop.app.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.xstv.base.LetvLog;
import com.xstv.desktop.app.AppPluginActivator;
import com.xstv.desktop.app.R;
import com.xstv.desktop.app.bean.AppIconBean;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.util.BitmapUtil;
import com.xstv.desktop.app.util.IconFilterUtil;
import com.xstv.desktop.app.util.Utilities;

/**
 * Created by wuh on 15-12-7.
 */
public class AppFolderItem extends RelativeLayout {
    String TAG = AppFolderItem.class.getSimpleName();

    private Context mContext;

    ImageView mIcon;
//    TextView label;
    private ItemInfo mItemInfo;
    /**
     * Use for notify superscript
     */
    private ImageView mSuperscriptView = null;

    // 显示数字类型的消息，如果消息数大于99，则显示“99+”，否则显示实际的未读消息数
    private static final int APP_BADGE_MSG_TYPE_DIGIT = 0;
    // 显示文字类型的消息，如系统升级，显示“new”
    private static final int APP_BADGE_MSG_TYPE_TEXT = 1;

    public AppFolderItem(Context context) {
        this(context, null);
    }

    public AppFolderItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppFolderItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;

        View.inflate(context, R.layout.app_folder_item, this);
        mIcon = (ImageView) findViewById(R.id.folder_item_icon);
//        label = (TextView) findViewById(R.id.folder_item_label);
    }

    public void setData(ItemInfo itemInfo) {
        mItemInfo = itemInfo;
        LetvLog.d(TAG, " setData " + mItemInfo);
        if (mItemInfo != null) {
            setIconRes();
//            label.setText(mItemInfo.getTitle());
            // set or update superscript view
            updateSuperscriptView(mItemInfo);
        }
    }

    private void updateSuperscriptView(ItemInfo itemInfo) {
        if(itemInfo == null){
            return;
        }

        LetvLog.i(TAG, " updateSuperscriptView msg count = " + itemInfo.superscriptCount
                + "type = " + itemInfo.superscriptType);

        int count = itemInfo.superscriptCount;
        if (count <= 0) {
            removeSuperscriptView();
            return;
        }

        if (mSuperscriptView == null) {
            mSuperscriptView = new ImageView(mContext);
            mSuperscriptView.setBackgroundResource(R.drawable.ic_files_notification);
            RelativeLayout.LayoutParams
                    layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            addView(mSuperscriptView, layoutParams);
        }
        if (indexOfChild(mSuperscriptView) == -1) {
            addView(mSuperscriptView, mSuperscriptView.getLayoutParams());
        }
    }

    private void removeSuperscriptView() {
        if (mSuperscriptView != null && indexOfChild(mSuperscriptView) != -1) {
            removeView(mSuperscriptView);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void setIconRes() {
        if (mItemInfo != null) {
            if (IconFilterUtil.isUsedTheme()) {
                setIconUseTheme(mItemInfo);
                return;
            }
            setIconDefault(mItemInfo);
        }
    }

    private void setIconDefault(ItemInfo itemInfo) {
        if (Utilities.isNeedBlur()) {
            AppIconBean appIconBean = IconFilterUtil.createIconBitmap(itemInfo);
            if (appIconBean != null) {
                mIcon.setImageBitmap(appIconBean.getIconBitmap());
            }
        } else {
            Drawable iconDrawable = IconFilterUtil.getIconDrawable(mContext, itemInfo);
            Bitmap iconBitmap = BitmapUtil.drawableToBitmap(iconDrawable);
            if (!Utilities.isPlatform648()) {
                iconBitmap = BitmapUtil.getPixelAreaOfBitmap(iconBitmap);
            }
            mIcon.setImageBitmap(iconBitmap);
        }
        mIcon.setVisibility(VISIBLE);
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
        } else {
            // 没有适配的图标使用统一的背景
            Resources resources = AppPluginActivator.getContext().getResources();// Must use plugin context
            Drawable bg = resources.getDrawable(R.drawable.app_cellview_usetheme_bg);
            LetvLog.d(TAG, " get backgroud from theme bg ");
            mIcon.setImageBitmap(iconBitmap);
            mIcon.setVisibility(VISIBLE);
            setBackground(bg);
        }
    }

    public void clearData() {
        mIcon.setImageDrawable(null);
        mIcon.setVisibility(GONE);
//        label.setText("");
        setBackgroundResource(0);
        removeSuperscriptView();
    }

    public void recycle(boolean isClean){
        if(mIcon != null){
            mIcon.setImageDrawable(null);
        }
        if(isClean){
            setBackground(null);
        }
    }
}
