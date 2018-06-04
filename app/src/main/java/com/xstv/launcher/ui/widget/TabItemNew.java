package com.xstv.launcher.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.xstv.launcher.R;
import com.xstv.library.base.LetvLog;

/**
 * Item to be shown on {@link TabStripImpl}
 * Created by ShaoDong on 16-9-21.
 */
public class TabItemNew extends FrameLayout {
    private static String TAG = "TabItemNew";
    private AlphaGradientTextView tabTextView;
    private Drawable mTempBackGroundDrawable;

    /**
     * textView's color
     */
    private ColorStateList textColor = ColorStateList.valueOf(0x8fffffff);
    private OnGlobalLayoutListener onGlobalLayoutListener;

    public TabItemNew(Context context) {
        this(context, null);
    }

    public TabItemNew(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabItemNew(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.tabspace_item_normal, this);
        tabTextView = (AlphaGradientTextView) findViewById(R.id.launcher_tab_item_title);
        setClipToPadding(false);
        setClipChildren(false);

        tabTextView.setColorPointColor(Color.parseColor("#FC6A02"));
        tabTextView.setColorPointRadius(6);
        //TODO temp set height here
        tabTextView.setHeight(getResources().getDimensionPixelSize(R.dimen.tabspace_text_height));
    }

    /**
     * this method should be called when view added to parent
     */
    public void addViewTreeObserverListener() {
        onGlobalLayoutListener = new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                //set the red Point position
                tabTextView.setColorPointLocation(tabTextView.getWidth() + 6, 6);
            }
        };
        getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public void removeViewTreeObserverListener() {
        getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
    }

    public AlphaGradientTextView getTabTextView() {
        return tabTextView;
    }

    /**
     * clear this view's background to out stand the foreground view
     */
    private void clearAndStoreBackground() {
        mTempBackGroundDrawable = getBackground();
        setBackgroundDrawable(null);
    }

    private void restoreBackground() {
        setBackgroundDrawable(mTempBackGroundDrawable);
    }

    public void setText(String content) {
        tabTextView.setText(content);
        tabTextView.requestLayout();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        int color = textColor.getColorForState(getDrawableState(), 0);
        tabTextView.setTextColor(color);
        if (isSelected()) {
            tabTextView.getPaint().setFakeBoldText(true);
        } else {
            tabTextView.getPaint().setFakeBoldText(false);
        }
    }

    public ColorStateList getTextColor() {
        return textColor;
    }

    public void setTextColor(ColorStateList textColor) {
        this.textColor = textColor;
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        try {
            TabSpace tabSpace = (TabSpace) getParent().getParent().getParent();
            if (tabSpace != null && tabSpace.getFocusedChild() == null && direction == View.FOCUS_UP) {
                tabSpace.setCurrentTab(tabSpace.getCurrentTab());
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.requestFocus(direction, previouslyFocusedRect);
    }
}
