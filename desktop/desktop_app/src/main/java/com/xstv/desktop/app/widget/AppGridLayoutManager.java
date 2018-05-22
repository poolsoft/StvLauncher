
package com.xstv.desktop.app.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class AppGridLayoutManager extends GridLayoutManager {

    private static final String TAG = AppGridLayoutManager.class.getSimpleName();

    public static final int HAND_DETECT_PAGE_NONE = 0;
    public static final int HAND_DETECT_PAGE_UP_DOWN = HAND_DETECT_PAGE_NONE + 1;
    public static final int HAND_DETECT_PAGE_UP = HAND_DETECT_PAGE_UP_DOWN + 1;
    public static final int HAND_DETECT_PAGE_DOWN = HAND_DETECT_PAGE_UP + 1;

    /**
     * 当获取焦点后需要recyclerView移动时,为了移动更大的距离 所以增加一个偏移量
     */
    private int mOffsetScrolling = 0;

    private int mMotionStatus = HAND_DETECT_PAGE_DOWN;
    private int mOverallScroll = 0;

    private int mHeaderSize = 0;

    public AppGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public AppGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    public void offsetScrolling(int dy) {
        mOffsetScrolling = dy;
    }

    @Override
    public View onFocusSearchFailed(View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
        View nextFocus = super.onFocusSearchFailed(focused, focusDirection, recycler, state);
        if (focused instanceof PosterCellView) {
            if (nextFocus == null && focusDirection == View.FOCUS_UP) {
                nextFocus = focused;
                ViewGroup parent = (ViewGroup) focused.getParent();
                int fromPos = getPosition(parent);
                if (fromPos == 0) {
                    nextFocus = null;
                }
            }
            return nextFocus;
        } else {
            if(nextFocus == null){
                return null;
            }
            int fromPos = getPosition(focused);
            int nextPos = getNextViewPos(fromPos, focusDirection);
            return findViewByPosition(nextPos);
        }
    }

    /**
     * Manually detect next view to focus.
     *
     * @param fromPos   from what position start to seek.
     * @param direction in what direction start to seek. Your regular {@code View.FOCUS_*}.
     * @return adapter position of next view to focus. May be equal to {@code fromPos}.
     */
    protected int getNextViewPos(int fromPos, int direction) {
        int offset = calcOffsetToNextView(direction);

        if (hitBorder(fromPos, offset)) {
            return fromPos;
        }

        return fromPos + offset;
    }

    /**
     * Calculates position offset.
     *
     * @param direction regular {@code View.FOCUS_*}.
     * @return position offset according to {@code direction}.
     */
    protected int calcOffsetToNextView(int direction) {
        int spanCount = getSpanCount();
        int orientation = getOrientation();

        if (orientation == VERTICAL) {
            switch (direction) {
                case View.FOCUS_DOWN:
                    return spanCount;
                case View.FOCUS_UP:
                    return -spanCount;
                case View.FOCUS_RIGHT:
                    return 1;
                case View.FOCUS_LEFT:
                    return -1;
            }
        } else if (orientation == HORIZONTAL) {
            switch (direction) {
                case View.FOCUS_DOWN:
                    return 1;
                case View.FOCUS_UP:
                    return -1;
                case View.FOCUS_RIGHT:
                    return spanCount;
                case View.FOCUS_LEFT:
                    return -spanCount;
            }
        }

        return 0;
    }

    /**
     * Checks if we hit borders.
     *
     * @param from   from what position.
     * @param offset offset to new position.
     * @return {@code true} if we hit border.
     */
    private boolean hitBorder(int from, int offset) {
        int spanCount = getSpanCount();
        if (Math.abs(offset) == 1) {
            int spanIndex = from % spanCount;
            int newSpanIndex = spanIndex + offset;
            return newSpanIndex < 0 || newSpanIndex >= spanCount;
        } else {
            int newPos = from + offset;
            return newPos < 0 && newPos >= spanCount;
        }
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int des = super.scrollVerticallyBy(dy, recycler, state);
        mOverallScroll += des;
        // if overall scrolled dy is not 0,
        // then the page could be scrolled up and down,
        // or else only could be scrolled down.
        if (mOverallScroll == 0) {
            mMotionStatus = HAND_DETECT_PAGE_DOWN;
        } else {
            mMotionStatus = HAND_DETECT_PAGE_UP_DOWN;
        }
        return des;
    }

    public int getPageStatus() {
        return mMotionStatus;
    }

    @Override
    public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate) {
        int position = parent.getChildPosition(child);
        int parentLeft = this.getPaddingLeft();
        int parentTop = this.getPaddingTop();
        int parentRight = this.getWidth() - this.getPaddingRight();
        int parentBottom = this.getHeight() - this.getPaddingBottom();
        int childLeft = child.getLeft() + rect.left;
        int childTop = child.getTop() + rect.top;
        int childRight = childLeft + rect.width();
        int childBottom = childTop + rect.height();
        int offScreenLeft = Math.min(0, childLeft - parentLeft);
        int offScreenTop = Math.min(0, childTop - parentTop);
        int offScreenRight = Math.max(0, childRight - parentRight);
        int offScreenBottom = Math.max(0, childBottom - parentBottom);

        if(offScreenTop != 0){
            //向上移动
            if(!isFirstRow(position)){
                if (offScreenTop < 0) {
                    offScreenTop -= mOffsetScrolling;
                } else if (offScreenTop == 0) {
                    if (child.getTop() - rect.top < mOffsetScrolling) {
                        offScreenTop -= mOffsetScrolling;
                    }
                }
            }
        }else{
            //向下移动
            if(isLastRow(position)){
                if(parentBottom - childBottom <= 0){
                    offScreenBottom += 30;
                }
            }else{
                if (offScreenBottom > 0) {
                    offScreenBottom += mOffsetScrolling;
                } else if (offScreenBottom == 0) {
                    if (parentBottom - childBottom < mOffsetScrolling) {
                        offScreenBottom += mOffsetScrolling;
                    }
                }
            }
        }

        int dx;
        if (ViewCompat.getLayoutDirection(parent) == 1) {
            dx = offScreenRight != 0 ? offScreenRight : offScreenLeft;
        } else {
            dx = offScreenLeft != 0 ? offScreenLeft : offScreenRight;
        }

        int dy = offScreenTop != 0 ? offScreenTop : offScreenBottom;
        if (dx == 0 && dy == 0) {
            return false;
        } else {
            if (immediate) {
                parent.scrollBy(dx, dy);
            } else {
                parent.smoothScrollBy(dx, dy);
            }

            return true;
        }
    }

    private boolean isFirstRow(int position) {
        return position == 0;
    }

    public void setHeaderSize(int headerSize){
        this.mHeaderSize = headerSize;
    }

    private boolean isLastRow(int position) {
        int spanCount = getSpanCount();
        return ((position - mHeaderSize) / spanCount) == ((getItemCount() - 1 - mHeaderSize) / spanCount);
    }
}
