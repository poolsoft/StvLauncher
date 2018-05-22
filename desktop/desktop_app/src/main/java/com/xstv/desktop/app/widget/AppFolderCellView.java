
package com.xstv.desktop.app.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xstv.base.LetvLog;
import com.xstv.desktop.app.R;
import com.xstv.desktop.app.bean.FolderInfo;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.interfaces.IAppFragment;

import java.util.ArrayList;

/**
 * Created by wuh on 15-11-9.
 */

public class AppFolderCellView extends CellView<FolderInfo> {
    private static final String TAG = AppFolderCellView.class.getSimpleName();

    public static final int FOLDER_COUNT = 12;

    private ArrayList<AppFolderItem> mChildrens;
    private ImageView mMaskView;
    TextView mLabel;
    private ImageView mEditView;

    public AppFolderCellView(Context context) {
        super(context);
    }

    public AppFolderCellView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void initView(Context context) {
        mChildrens = new ArrayList<AppFolderItem>(6);
        View.inflate(context, R.layout.app_folder_layout, this);
        AppFolderItem child0 = (AppFolderItem) findViewById(R.id.app_folder_item_0);
        mChildrens.add(child0);
        AppFolderItem child1 = (AppFolderItem) findViewById(R.id.app_folder_item_1);
        mChildrens.add(child1);
        AppFolderItem child2 = (AppFolderItem) findViewById(R.id.app_folder_item_2);
        mChildrens.add(child2);
        AppFolderItem child3 = (AppFolderItem) findViewById(R.id.app_folder_item_3);
        mChildrens.add(child3);
        AppFolderItem child4 = (AppFolderItem) findViewById(R.id.app_folder_item_4);
        mChildrens.add(child4);
        AppFolderItem child5 = (AppFolderItem) findViewById(R.id.app_folder_item_5);
        mChildrens.add(child5);
        mLabel = (TextView) findViewById(R.id.app_folder_item_label);
        mMaskView = (ImageView) findViewById(R.id.folderview_mask);
        mEditView = (ImageView) findViewById(R.id.folderview_editIcon);
        setBackground(getResources().getDrawable(R.drawable.app_folder_bg));
    }

    @Override
    public void bindData(FolderInfo folderInfo) {
        super.bindData(folderInfo);
        setChildrenData(folderInfo);
        // LetvLog.d(TAG, "bindData folderInfo = " + folderInfo);
    }

    private void setChildrenData(FolderInfo folderInfo) {
        if (folderInfo == null) {
            return;
        }
        // 1.clear older data
        for (AppFolderItem item : mChildrens) {
            item.clearData();
        }
        // 2.set new data
        int content = folderInfo.getLength();
        int j = 0;
        // 3.reset
        for (int i = 0; i < content; i++) {
            ItemInfo itemInfo = folderInfo.getChildrenByIndex(i);
            LetvLog.d(TAG, " setChildrenData itemInfo = " + itemInfo);
            if (itemInfo != null) {
                if (j > (mChildrens.size() - 1)) {
                    break;
                }
                AppFolderItem child = mChildrens.get(j);
                child.setData(itemInfo);
                j++;
            }
        }
    }

    @Override
    public void setLabel(FolderInfo folderInfo) {
        super.setLabel(folderInfo);
        if (mLabel != null) {
            mLabel.setText(folderInfo.getTitle());
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (isShowLoading) {
            if (mLabel != null && mLabel.getVisibility() == View.VISIBLE) {
                mLabel.setVisibility(View.INVISIBLE);
            }
        } else {
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

    @Override
    public void openCellView() {
        openFolder();
    }

    private void openFolder() {
        LetvLog.d(TAG, "openFolder");
        if (fragmentRef != null && fragmentRef.get() != null) {
            IAppFragment fragment = fragmentRef.get();
            AppWorkspace appWorkspace = fragment.getAppWorkspace();
            AppFolderWorkspace folderWorkspace = fragment.getFolderWorkspace();
            if (appWorkspace != null && folderWorkspace != null) {
                appWorkspace.openFolder(mItemInfo);
            }
        }
    }

    @Override
    public void removeCellView() {
        String msg = getResources().getString(R.string.into_folder_to_handle);
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setDeleteState(boolean isFocus) {
        super.setDeleteState(isFocus);
        if (mMaskView != null) {
            mMaskView.setVisibility(VISIBLE);
        }
    }

    @Override
    public void resetDeleteState() {
        super.resetDeleteState();
        if (mMaskView != null) {
            mMaskView.setVisibility(GONE);
        }
    }

    @Override
    public void setNewFolderState(boolean isFocus) {
        super.setNewFolderState(isFocus);
    }

    @Override
    public void resetState() {
        super.resetState();
        if (mMaskView != null) {
            mMaskView.setVisibility(GONE);
        }
        if (mItemInfo != null) {
            mItemInfo.isAdding = false;
        }

        if (mEditView != null) {
            mEditView.setImageResource(0);
            mEditView.setVisibility(GONE);
        }
        removeMaskView();
        setEditIcon(-1);
        canMove = false;
        updateArrow(false);
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

        /*DownloadStatusBean downloadStatusBean = mItemInfo.getDownloadStatusBean();
        if(downloadStatusBean == null){
            return "";
        }
        return downloadStatusBean.getLoadingTitle();*/
        return "";
    }

    @Override
    public void recycle(boolean isCleanView) {
        if (mChildrens == null) {
            return;
        }
        int size = mChildrens.size();
        for (int i = 0; i < size; i++) {
            AppFolderItem appFolderItem = mChildrens.get(i);
            if (isCleanView) {
                removeView(appFolderItem);
            }
            appFolderItem.recycle(isCleanView);
        }
        if (isCleanView) {
            mChildrens.clear();
            mLabel = null;
            mMaskView = null;
            mEditView = null;
        }
    }
}
