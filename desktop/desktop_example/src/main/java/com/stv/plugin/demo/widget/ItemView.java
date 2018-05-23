package com.stv.plugin.demo.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;

public class ItemView extends FrameLayout {

    private int mDuration;
    private float mPivotX;
    private float mPivotY;
    private float mShadowZ;
    private float mInitialScaleValue;
    private float mInitAlphaValue;
    private float mCurrentScaleX;
    private float mCurrentScaleY;
    private float mCurrentAlpha;
    private boolean mResetByAnimation;
    private ScaleXUpdateListener mScaleXUpdateListener;
    private ScaleYUpdateListener mScaleYUpdateListener;
    private AlphaUpdateListener mAlphaUpdateListener;

    private ArrayList<Animator> mAnimators = new ArrayList<Animator>(3);

    public ItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        /**
         * 防止字体放大后变虚，先设置最大字号，再缩小显示
         * mInitialValue为缩小比例
         */
        mInitialScaleValue = 0.95f;

        /**
         * 默认透明度
         */
        mInitAlphaValue = 0.6f;

        mDuration = 150;
        mPivotX = -1;
        mPivotY = -1;
        mShadowZ = 40;
        mResetByAnimation = true;

        setScaleX(mInitialScaleValue);
        setScaleY(mInitialScaleValue);
        //setAlpha(mInitAlphaValue);

        mCurrentScaleX = mCurrentScaleY = mInitialScaleValue;
        mCurrentAlpha = mInitAlphaValue;

        mScaleXUpdateListener = new ScaleXUpdateListener();
        mScaleYUpdateListener = new ScaleYUpdateListener();
        mAlphaUpdateListener = new AlphaUpdateListener();

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                setPivotX(mPivotX > 0 ? mPivotX : getWidth() / 2);
                setPivotY(mPivotY > 0 ? mPivotY : getHeight() / 2);
                ItemView.this.getViewTreeObserver().removeOnPreDrawListener(this);
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
        ObjectAnimator alphaAnim = null;
        if (gainFocus) {
            xScaleAnim = ObjectAnimator.ofFloat(ItemView.this, "scaleX", mCurrentScaleX, 1.05f);
            yScaleAnim = ObjectAnimator.ofFloat(ItemView.this, "scaleY", mCurrentScaleY, 1.05f);
            alphaAnim = ObjectAnimator.ofFloat(ItemView.this, "alpha", mCurrentAlpha, 1.0f);
            xScaleAnim.setDuration(mDuration);
            yScaleAnim.setDuration(mDuration);
            alphaAnim.setDuration(mDuration);
            xScaleAnim.addUpdateListener(mScaleXUpdateListener);
            yScaleAnim.addUpdateListener(mScaleYUpdateListener);
            alphaAnim.addUpdateListener(mAlphaUpdateListener);

            if (mShadowZ > 0) {
                upAnim = ObjectAnimator.ofFloat(ItemView.this, "translationZ", mShadowZ);
                upAnim.setInterpolator(new DecelerateInterpolator());
                upAnim.setDuration(mDuration);
            }
        } else {
            if (mResetByAnimation) {
                xScaleAnim = ObjectAnimator.ofFloat(ItemView.this, "scaleX", mCurrentScaleX, mInitialScaleValue);
                yScaleAnim = ObjectAnimator.ofFloat(ItemView.this, "scaleY", mCurrentScaleY, mInitialScaleValue);
                alphaAnim = ObjectAnimator.ofFloat(ItemView.this, "alpha", mCurrentAlpha, mInitAlphaValue);
                xScaleAnim.setDuration(mDuration);
                yScaleAnim.setDuration(mDuration);
                alphaAnim.setDuration(mDuration);
                xScaleAnim.addUpdateListener(mScaleXUpdateListener);
                yScaleAnim.addUpdateListener(mScaleYUpdateListener);
                alphaAnim.addUpdateListener(mAlphaUpdateListener);
            } else {
                ItemView.this.setScaleX(mInitialScaleValue);
                ItemView.this.setScaleY(mInitialScaleValue);
                ItemView.this.setAlpha(mInitAlphaValue);
                mCurrentScaleX = mInitialScaleValue;
                mCurrentScaleY = mInitialScaleValue;
                mCurrentAlpha = mInitAlphaValue;
            }

            if (mShadowZ > 0) {
                upAnim = ObjectAnimator.ofFloat(this, "translationZ", 0);
                upAnim.setInterpolator(new AccelerateInterpolator());
                upAnim.setDuration(mDuration);
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
        if (alphaAnim != null) {
            //mAnimators.add(alphaAnim);
        }

        AnimatorSet aniSet = new AnimatorSet();
        aniSet.playTogether(mAnimators);
        aniSet.start();
    }


    final class ScaleXUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mCurrentScaleX = (Float) animation.getAnimatedValue("scaleX");
        }
    }

    final class ScaleYUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mCurrentScaleY = (Float) animation.getAnimatedValue("scaleY");
        }
    }

    final class AlphaUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mCurrentAlpha = (Float) animation.getAnimatedValue("alpha");
        }
    }
}
