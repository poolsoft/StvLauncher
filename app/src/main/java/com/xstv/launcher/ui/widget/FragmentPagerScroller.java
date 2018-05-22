
package com.xstv.launcher.ui.widget;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class FragmentPagerScroller extends Scroller {

    private int mDefaultDuration = 600;
    private int mDuration = mDefaultDuration;

    public FragmentPagerScroller(Context context) {
        super(context);
    }

    public FragmentPagerScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    public void setDuration(int time) {
        mDuration = time;
    }

    public int getDefaultDuration() {
        return mDefaultDuration;
    }
}
