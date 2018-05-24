
package com.xstv.desktop.app.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.xstv.desktop.app.bean.ContentBean;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.interfaces.IAppFragment;
import com.xstv.desktop.app.util.Utilities;

import java.lang.ref.WeakReference;
import java.util.List;

public abstract class BaseContent extends LinearLayout {
    private static final String TAG = BaseContent.class.getSimpleName();

    public BaseContent(Context context) {
        this(context, null);
    }

    public BaseContent(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseContent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
        initData();
    }

    public abstract void initView(Context context);

    public void initData() {

    }

    public void bindData(ContentBean contentBean) {
        List<ItemInfo> itemList = null;
        if(contentBean != null){
            itemList = contentBean.getContentItemList();
        }
        int childCount = getChildCount();
        int k = 0;
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (view instanceof BaseCellView) {
                BaseCellView baseCellView = (BaseCellView) view;
                int dataSize = 0;
                if (itemList != null) {
                    dataSize = itemList.size();
                }
                if (k < dataSize) {
                    ItemInfo itemInfo = itemList.get(k);
                    //LetvLog.d(TAG, "bindData itemInfo = " + itemInfo);
                    baseCellView.bindData(itemInfo);
                } else {
                    baseCellView.bindData(null);
                }
                String tag = (String) getTag();
                //LetvLog.d(TAG, "bindData tag = " + tag);
                baseCellView.setTag(tag + "," + k);
                k++;
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setFocusable(false);
        setFocusableInTouchMode(false);
        setClipChildren(false);
        setClipToPadding(false);
        setChildrenDrawingOrderEnabled(Utilities.isChangeDrawOrder());
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int iteration) {
        View focusedChild = getFocusedChild();
        int order = iteration;

        if (focusedChild != null) {
            int focusedIndex = indexOfChild(focusedChild);
            if (iteration == childCount - 1) {
                order = focusedIndex;
            } else if (iteration >= focusedIndex) {
                order = iteration + 1;
            }
        }
        return order;
    }

    public void setAppFragment(WeakReference<IAppFragment> fragmentRef) {
        int childCount = getChildCount();
        // LetvLog.d(TAG, "setAppFragment childCount = " + childCount);
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView instanceof BaseCellView) {
                ((BaseCellView) childView).setAppFragment(fragmentRef);
            }
        }
    }

    public void clearMemory() {

    }
}
