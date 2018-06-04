package com.xstv.desktop.emodule.view;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.xstv.desktop.emodule.R;


public class FocusAss implements View.OnAttachStateChangeListener, View.OnLayoutChangeListener {

    Drawable mFocusHLTDrawable;
    Rect mPad = new Rect();
    ViewGroup mView;
    int mResId = R.drawable.img_focus;

    public FocusAss(ViewGroup aView) {
        // mResId = StyleManager.getInstance().getFocusImageRes();
        if (mResId == 0) mResId = R.drawable.img_focus;
        mView = aView;
        mFocusHLTDrawable = mView.getResources().getDrawable(mResId);
        mFocusHLTDrawable.getPadding(mPad);
        mView.addOnAttachStateChangeListener(this);
        mView.addOnLayoutChangeListener(this);
    }

    public void setFocusHLT(int resId) {
        mResId = resId;
        update();
    }

    private void setupParent() {
        ViewParent vp = mView.getParent();
        final int MaxCheck = 1;
        int check = -1;
        while (vp instanceof ViewGroup & ++check < MaxCheck) {
            ((ViewGroup) vp).setClipChildren(false);
            ((ViewGroup) vp).setClipToPadding(false);
            vp = vp.getParent();
        }
    }

    public void update() {
        if (mView.getTag(R.id.focus_res) != null) {
            mFocusHLTDrawable = mView.getResources().getDrawable((int) mView.getTag(R.id.focus_res));
        }
        mFocusHLTDrawable.getPadding(mPad);
        if (mView.getTag(R.id.focus_res) != null && (int) mView.getTag(R.id.focus_res) == R.drawable.img_focus_circle) {
            float s = (float) mView.getWidth() / 188;
            mPad.set((int) (28 * s), (int) (18 * s), (int) (28 * s), (int) (38 * s));
        }
    }

    public void drawFocusEffect(Canvas aCanvas) {
        if (mView.isFocused() || (mView instanceof ViewGroup & ((ViewGroup) mView).getFocusedChild() != null)) {
            aCanvas.save();
            View img = mView.findViewById(R.id.di_img);
            Rect rect = new Rect();
            if (img != null) {
                mView.offsetDescendantRectToMyCoords(img, rect);
                aCanvas.translate(-mPad.left + rect.left, -mPad.top + rect.top);
            } else {
                aCanvas.translate(-mPad.left, -mPad.top);
            }

            mFocusHLTDrawable.draw(aCanvas);
            aCanvas.restore();
        }
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        setupParent();
    }

    @Override
    public void onViewDetachedFromWindow(View v) {

    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        update();

        View tmp = mView.findViewById(R.id.di_img);
        int with = right - left, height = bottom - top;
        if (tmp != null) {
            with = tmp.getWidth();
            height = tmp.getHeight();
        }

        mFocusHLTDrawable.setBounds(0, 0, with + mPad.left + mPad.right, height + mPad.top + mPad.bottom);
    }
}
