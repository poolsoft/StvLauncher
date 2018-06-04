package com.xstv.desktop.emodule.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.xstv.desktop.emodule.R;

public class ScanLightView extends View implements ValueAnimator.AnimatorUpdateListener {
    final static int ANIMATION_TIME = 1200;
    static Drawable mLightDrawable = null;
    ValueAnimator mAnimator = null;
    float mAnimValue = 0;
    Paint mPaint;
    boolean mFirst = false;
    boolean mEnable = true;

    public ScanLightView(Context context) {
        this(context, null, 0);
    }

    public ScanLightView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanLightView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (mLightDrawable == null) {
            mLightDrawable = getResources().getDrawable(R.drawable.focus_light);
        }
        mAnimator = ValueAnimator.ofFloat(0, 1.0f);
        mAnimator.setDuration(ANIMATION_TIME);
        mAnimator.addUpdateListener(this);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
        mPaint.setAlpha(120);
    }

    private boolean enableScanAnimation() {
        return mEnable && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (enableScanAnimation()) {
            if (mLightDrawable instanceof BitmapDrawable) {
                if (mAnimValue > 0 && mAnimValue < 1) {
                    canvas.save();
                    canvas.clipRect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
                    int width = getWidth() - getPaddingLeft() - getPaddingRight();
                    int height = getHeight() - getPaddingTop() - getPaddingBottom();
                    if (width > 0 && height > 0) {
                        float scale = (float) (Math.sqrt(width * width + height * height) / Math.sqrt(240 * 240 + 360 * 360));
                        if (mFirst) {
                            mAnimator.setDuration((long) (ANIMATION_TIME * scale));
                            mFirst = false;
                        }
                        canvas.scale(scale, scale);
                        if (width >= height) {
                            float x = -mLightDrawable.getIntrinsicWidth() + getPaddingLeft() + (width + mLightDrawable.getIntrinsicWidth()) * mAnimValue;
                            float y = -mLightDrawable.getIntrinsicHeight() / 2 - mLightDrawable.getIntrinsicWidth() / 2 * height / width + getPaddingTop() + (height + mLightDrawable.getIntrinsicWidth() * height / width) * mAnimValue;
                            canvas.translate(x, y);
                        } else {
                            float y = -mLightDrawable.getIntrinsicHeight() + getPaddingTop() + (height + mLightDrawable.getIntrinsicHeight()) * mAnimValue;
                            float x = -mLightDrawable.getIntrinsicWidth() / 2 - mLightDrawable.getIntrinsicHeight() / 2 * width / height + getPaddingLeft() + (width + mLightDrawable.getIntrinsicHeight() * width / height) * mAnimValue;
                            canvas.translate(x, y);
                        }


                        Bitmap bitmap = ((BitmapDrawable) mLightDrawable).getBitmap();
                        canvas.drawBitmap(bitmap, 0, 0, mPaint);
                    }
                    canvas.restore();
                }
            }
        }

    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        mAnimValue = (float) animation.getAnimatedValue();
        invalidate();
    }

    public void startAnim() {
        if (enableScanAnimation()) {
            mAnimator.cancel();
            mAnimator.start();
            mFirst = true;
        }
    }

    public void cancelAnim() {
        if (enableScanAnimation()) {
            mAnimator.end();
        }
    }

    public void startAnimWithRepeat() {
        if (enableScanAnimation()) {
            mAnimator.cancel();
            mAnimator.start();
            mAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mFirst = true;
        }
    }

    public void setEnableAnim(boolean enableAnim) {
        mEnable = enableAnim;
    }
}
