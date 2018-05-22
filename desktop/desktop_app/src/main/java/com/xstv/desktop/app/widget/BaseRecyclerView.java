
package com.xstv.desktop.app.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.xstv.desktop.app.util.Utilities;

public class BaseRecyclerView extends RecyclerView {

    protected boolean isUserVisible;

    public BaseRecyclerView(Context context) {
        this(context, null);
    }

    public BaseRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setDrawingCacheEnabled(false);
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

    public void setUserVisible(boolean isUserVisible){
        this.isUserVisible = isUserVisible;
    }

    public void resetScroll() {
        scrollToPosition(0);
    }

    public void resetSmoothScroll() {
        smoothScrollToPosition(0);
    }

    public void scrollUpByHandDetect() {
        int dy = (int) (getHeight() * 0.9);
        dy = -dy;
        smoothScrollBy(0, dy);

    }

    public void scrollDownByHandDetect() {
        int dy = (int) (getHeight() * 0.9);
        smoothScrollBy(0, dy);
    }
}
