package com.xstv.launcher.ui.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.xstv.launcher.R;
import com.xstv.launcher.ui.presenter.LauncherAdapterPresenter;
import com.xstv.base.WeakHandler;

import static android.view.View.GONE;

public class SwitchIndicator implements LauncherAdapterPresenter.OnScreenSwitchedListener {

    private ViewPagerSpace mViewPagerSpace;

    private ImageView mArrowLeftIv;
    private ImageView mArrowRightIv;
    private Animation mShakeAnim;
    private Animation mShakeAnimRev;
    private Animation mArrowAlphaOut;
    private WeakHandler<SwitchIndicator> mHandler = new WeakHandler<SwitchIndicator>(this);

    public SwitchIndicator(ViewGroup attachView, ViewPagerSpace viewPagerSpace) {
        mViewPagerSpace = viewPagerSpace;
        initIndicatorViews(attachView);
    }

    private void initIndicatorViews(ViewGroup attachView) {
        Context context = attachView.getContext();

        mArrowLeftIv = new ImageView(context);
        mArrowRightIv = new ImageView(context);

        mArrowLeftIv.setImageResource(R.drawable.ic_newguidelines_arrow_left);
        mArrowRightIv.setImageResource(R.drawable.ic_newguidelines_arrow_right);

        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(50, 80);
        params1.gravity = Gravity.LEFT;
        params1.leftMargin = 54;
        params1.topMargin = 540;
        attachView.addView(mArrowLeftIv, params1);

        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(50, 80);
        params2.gravity = Gravity.RIGHT;
        params2.rightMargin = 54;
        params2.topMargin = 540;
        attachView.addView(mArrowRightIv, params2);

        mShakeAnim = AnimationUtils.loadAnimation(context, R.anim.shake);
        mShakeAnimRev = AnimationUtils.loadAnimation(context, R.anim.shake_reverse);
        mArrowAlphaOut = AnimationUtils.loadAnimation(context, R.anim.alpha_out);
    }

    private void hideLeftArrow(boolean anim) {
        if (mArrowLeftIv != null) {
            mArrowLeftIv.clearAnimation();
            if (anim && mArrowLeftIv.isShown()) {
                mArrowLeftIv.startAnimation(mArrowAlphaOut);
            }
            mArrowLeftIv.setVisibility(View.GONE);
        }
    }

    private void hideRightArrow(boolean anim) {
        if (mArrowRightIv != null) {
            mArrowRightIv.clearAnimation();
            if (anim && mArrowRightIv.isShown()) {
                mArrowRightIv.startAnimation(mArrowAlphaOut);
            }
            mArrowRightIv.setVisibility(View.GONE);
            return;
        }
    }

    private Runnable mHideLeftArrowRunnable = new Runnable() {

        @Override
        public void run() {
            hideLeftArrow(true);
        }
    };

    private Runnable mHideRightArrowRunnable = new Runnable() {

        @Override
        public void run() {
            hideRightArrow(true);
        }
    };

    @Override
    public void onScreenSelected(String screenTag, int index, int total) {
        if (mViewPagerSpace != null && mViewPagerSpace.currentCanKeyDragOut()) {
            if (index == total - 1) {
                mArrowLeftIv.setVisibility(View.VISIBLE);
                mArrowRightIv.setVisibility(GONE);

                mArrowLeftIv.startAnimation(mShakeAnimRev);
                mHandler.removeCallbacks(mHideLeftArrowRunnable);
                mHandler.postDelayed(mHideLeftArrowRunnable, 3000);
            } else if (index == 0) {
                mArrowLeftIv.setVisibility(GONE);
                mArrowRightIv.setVisibility(View.VISIBLE);
                mArrowRightIv.startAnimation(mShakeAnim);

                mHandler.removeCallbacks(mHideLeftArrowRunnable);
                mHandler.removeCallbacks(mHideRightArrowRunnable);
                mHandler.postDelayed(mHideLeftArrowRunnable, 3000);
                mHandler.postDelayed(mHideRightArrowRunnable, 3000);
            } else {
                mArrowLeftIv.setVisibility(View.VISIBLE);
                mArrowRightIv.setVisibility(View.VISIBLE);

                mArrowLeftIv.startAnimation(mShakeAnimRev);
                mArrowRightIv.startAnimation(mShakeAnim);
                mHandler.removeCallbacks(mHideLeftArrowRunnable);
                mHandler.removeCallbacks(mHideRightArrowRunnable);
                mHandler.postDelayed(mHideLeftArrowRunnable, 3000);
                mHandler.postDelayed(mHideRightArrowRunnable, 3000);
            }
        }

    }

    @Override
    public void onScreenScrolling(int state) {

    }
}
