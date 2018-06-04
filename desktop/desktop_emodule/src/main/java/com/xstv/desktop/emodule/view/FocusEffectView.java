package com.xstv.desktop.emodule.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.xstv.desktop.emodule.R;

public class FocusEffectView extends ScanLightView {
    int mResId = 0;
    Drawable mDrawableRect;
    Rect mPadding = new Rect();

    public FocusEffectView(Context context) {
        this(context, null);
    }

    public FocusEffectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusEffectView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mResId = R.drawable.img_focus;
        mDrawableRect = getResources().getDrawable(R.drawable.img_focus);
        setEnableAnim(true);
        mDrawableRect.getPadding(mPadding);
    }

    public void setFocusDrawable(int resId) {
        mDrawableRect = getResources().getDrawable(resId);
        mDrawableRect.getPadding(mPadding);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(-mPadding.left, -mPadding.top);
        mDrawableRect.draw(canvas);
        canvas.restore();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mDrawableRect.setBounds(0, 0, getWidth() + mPadding.left + mPadding.right, getHeight() + mPadding.top + mPadding.bottom);
    }


}
