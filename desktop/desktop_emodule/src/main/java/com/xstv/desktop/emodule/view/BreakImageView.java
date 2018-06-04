package com.xstv.desktop.emodule.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import static android.widget.RelativeLayout.ALIGN_PARENT_LEFT;


public class BreakImageView extends ImageView {
    int mAlign = Gravity.CENTER;

    public BreakImageView(Context context) {
        super(context);
    }

    public BreakImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BreakImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable != null) {
            setPivotY(drawable.getIntrinsicHeight());
        }

        super.setImageDrawable(drawable);
        if (getParent() != null && getParent() instanceof ViewGroup) {

        }
        post(new Runnable() {
            @Override
            public void run() {
                int width = getWidth();
                switch (mAlign) {
                    case Gravity.LEFT:
                        setPivotX(0);
                        break;
                    case Gravity.RIGHT:
                        setPivotX(width);
                        break;
                    case Gravity.CENTER:
                        setPivotX(width / 2);
                        break;
                    default:
                        break;
                }
            }
        });

    }

    public void setImageAlign(int align) {
        mAlign = align;
        if (getLayoutParams() instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
            switch (mAlign) {
                case Gravity.LEFT:
                    layoutParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
                    break;
                case Gravity.RIGHT:
                    layoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
                    break;
                case Gravity.CENTER:
                    layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
                    break;
                default:
                    layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
                    break;
            }

        } else if (getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
            switch (mAlign) {
                case Gravity.LEFT:
                    layoutParams.addRule(ALIGN_PARENT_LEFT);
                    break;
                case Gravity.RIGHT:
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    break;
                case Gravity.CENTER:
                    layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    break;
                default:
                    layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    break;
            }
        }
    }
}