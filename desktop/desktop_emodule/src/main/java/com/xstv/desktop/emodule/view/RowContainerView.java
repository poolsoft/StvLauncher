package com.xstv.desktop.emodule.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.xstv.desktop.emodule.R;

/**
 * RowContainerView wraps header and user defined row view
 */
public final class RowContainerView extends LinearLayout {

    private ViewGroup mHeaderDock;
    private Drawable mForeground;
    private boolean mForegroundBoundsChanged = true;
    private int paddingV = 0;
    private int paddingH = 0;

    public RowContainerView(Context context) {
        this(context, null, 0);
    }

    public RowContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RowContainerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.block_row_container, this);

        mHeaderDock = (ViewGroup) findViewById(R.id.row_container_header_dock);
        setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        paddingV = (int) getResources().getDimension(R.dimen.grid_block_vertical_padding);
        paddingH = (int) getResources().getDimension(R.dimen.grid_block_hor_padding);
        setPadding(paddingH, 0, paddingH, paddingV);
        setClipToPadding(false);
        setClipChildren(false);
    }

    public void addHeaderView(View headerView) {
        if (mHeaderDock.indexOfChild(headerView) < 0) {
            mHeaderDock.addView(headerView, 0);
        }
    }

    public void removeHeaderView(View headerView) {
        if (mHeaderDock.indexOfChild(headerView) >= 0) {
            mHeaderDock.removeView(headerView);
        }
    }

    public void addRowView(View view) {
        addView(view);
    }

    public void showHeader(boolean show) {
        mHeaderDock.setVisibility(show ? View.VISIBLE : View.GONE);
//        if (show) {
//            setPadding(paddingH, paddingV / 2, paddingH, 0);
//        } else {
//            setPadding(paddingH, paddingV, paddingH, 0);
//        }
    }

    public void setForeground(Drawable d) {
        mForeground = d;
        setWillNotDraw(mForeground == null);
        invalidate();
    }

    public void setForegroundColor(@ColorInt int color) {
        if (mForeground instanceof ColorDrawable) {
            ((ColorDrawable) mForeground.mutate()).setColor(color);
            invalidate();
        } else {
            setForeground(new ColorDrawable(color));
        }
    }

    public Drawable getForeground() {
        return mForeground;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mForegroundBoundsChanged = true;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mForeground != null) {
            if (mForegroundBoundsChanged) {
                mForegroundBoundsChanged = false;
                mForeground.setBounds(0, 0, getWidth(), getHeight());
            }
            mForeground.draw(canvas);
        }
    }
}