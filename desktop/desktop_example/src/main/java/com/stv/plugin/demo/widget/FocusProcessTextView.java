
package com.stv.plugin.demo.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import java.util.ArrayList;

public class FocusProcessTextView extends TextView {

    private int mDuration;
    private float mPivotX;
    private float mPivotY;
    private float mShadowZ;
    private float mInitialValue;
    private float mCurrentScaleX;
    private float mCurrentScaleY;
    private boolean mResetByAnimation;
    private ScaleXUpdateListener mScaleXUpdateListener;
    private ScaleYUpdateListener mScaleYUpdateListener;

    private ArrayList<Animator> mAnimators = new ArrayList<Animator>(3);

    public FocusProcessTextView(Context context) {
        this(context, null);
    }

    public FocusProcessTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusProcessTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        /** 防止字体放大后变虚，先设置最大字号，再缩小显示 */
        mInitialValue = 0.8f;
        mDuration = 150;
        mPivotX = 0;
        mPivotY = -1;
        mShadowZ = 20;
        mResetByAnimation = true;
        setScaleX(mInitialValue);
        setScaleY(mInitialValue);
        setAlpha(0.4f);

        mCurrentScaleX = mCurrentScaleY = mInitialValue;
        mScaleXUpdateListener = new ScaleXUpdateListener();
        mScaleYUpdateListener = new ScaleYUpdateListener();

        getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                setPivotX(mPivotX >= 0 ? mPivotX : getWidth() / 2);
                setPivotY(mPivotY >= 0 ? mPivotY : getHeight() / 2);
                FocusProcessTextView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

        for (Animator a : mAnimators) {
            a.cancel();
        }
        mAnimators.clear();

        ObjectAnimator xScaleAnim = null;
        ObjectAnimator yScaleAnim = null;
        ObjectAnimator upAnim = null;
        if (gainFocus) {
            setAlpha(1.0f);
            xScaleAnim = ObjectAnimator.ofFloat(FocusProcessTextView.this, "scaleX", mCurrentScaleX, 1.0f);
            yScaleAnim = ObjectAnimator.ofFloat(FocusProcessTextView.this, "scaleY", mCurrentScaleY, 1.0f);
            xScaleAnim.setDuration(mDuration);
            yScaleAnim.setDuration(mDuration);
            xScaleAnim.addUpdateListener(mScaleXUpdateListener);
            yScaleAnim.addUpdateListener(mScaleYUpdateListener);

            if (mShadowZ > 0) {
                upAnim = ObjectAnimator.ofFloat(FocusProcessTextView.this, "translationZ", mShadowZ);
                upAnim.setInterpolator(new DecelerateInterpolator());
                upAnim.setDuration(mDuration);
            }
        } else {
            setAlpha(0.4f);
            if (mResetByAnimation) {
                xScaleAnim = ObjectAnimator.ofFloat(FocusProcessTextView.this, "scaleX", mCurrentScaleX, mInitialValue);
                yScaleAnim = ObjectAnimator.ofFloat(FocusProcessTextView.this, "scaleY", mCurrentScaleY, mInitialValue);
                xScaleAnim.setDuration(mDuration);
                yScaleAnim.setDuration(mDuration);
                xScaleAnim.addUpdateListener(mScaleXUpdateListener);
                yScaleAnim.addUpdateListener(mScaleYUpdateListener);
            } else {
                FocusProcessTextView.this.setScaleX(mInitialValue);
                FocusProcessTextView.this.setScaleY(mInitialValue);
                mCurrentScaleX = mInitialValue;
                mCurrentScaleY = mInitialValue;
            }

            if (mShadowZ > 0) {
                upAnim = ObjectAnimator.ofFloat(this, "translationZ", 0);
                upAnim.setInterpolator(new AccelerateInterpolator());
                upAnim.setDuration(0);
            }
        }

        if (xScaleAnim != null) {
            mAnimators.add(xScaleAnim);
        }
        if (yScaleAnim != null) {
            mAnimators.add(yScaleAnim);
        }
        if (upAnim != null) {
            mAnimators.add(upAnim);
        }

        AnimatorSet aniSet = new AnimatorSet();
        aniSet.playTogether(mAnimators);
        aniSet.start();
    }

    class ScaleXUpdateListener implements AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mCurrentScaleX = (Float) animation.getAnimatedValue("scaleX");
        }
    }

    class ScaleYUpdateListener implements AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mCurrentScaleY = (Float) animation.getAnimatedValue("scaleY");
        }
    }
}
