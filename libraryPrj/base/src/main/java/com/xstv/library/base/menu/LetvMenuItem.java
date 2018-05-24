package com.xstv.library.base.menu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xstv.library.base.R;

// import com.android.internal.R;
// EUI START liuteng1 add for letvmenu at 2016-04-11

// EUI END
public class LetvMenuItem extends LinearLayout {
    private final String TAG = "LetvMenuItem";
    public static final int FOCUS_POS = 3;
    private Context mContext = null;
    private Drawable mFocusIconDrawable = null;
    private Drawable mNormalIconDrawable = null;
    private String mNameStr = null;
    private String mStatusStr = null;
    private TextView mStatusView = null;
    private TextView mNameView = null;
    private ImageView mIconView = null;
    private int mNormalColor = 0;
    private int mFocusColor = 0;
    private int mSecColor = 0;
    private int mKey = 0;
    private static int mKeyBase = 0;
    private static boolean mFinish = true;
    private boolean mEnable = true;
    private boolean mNoAnimationEnlarge = false;
    // EUI START liuteng1 add for letvmenu at 2016-04-11
    private int mTextWidth = 320;

    // EUI END
    public LetvMenuItem(Context context) {
        this(context, null);
    }

    public LetvMenuItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
                R.styleable.LetvMenuItem);
        mNormalIconDrawable = mTypedArray.getDrawable(R.styleable.LetvMenuItem_itemNormalIcon);
        mFocusIconDrawable = mTypedArray.getDrawable(R.styleable.LetvMenuItem_itemFocusIcon);
        mNameStr = mTypedArray.getString(R.styleable.LetvMenuItem_itemTitle);
        mStatusStr = mTypedArray.getString(R.styleable.LetvMenuItem_itemDetail);
        mTypedArray.recycle();

        LayoutInflater Inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Inflater.inflate(R.layout.letv_menu_item, this, true);
        setFocusable(true);

        mNameView = (TextView) findViewById(R.id.item_title);
        mNameView.setText(mNameStr);
        // EUI START liuteng1 modify for letvmenu at 2016-04-11
        mNameView.setFocusable(true);
        mNameView.setFocusableInTouchMode(true);
        mNameView.setSingleLine(true);
        mNameView.setEllipsize(TruncateAt.MARQUEE);
        mNameView.setMarqueeRepeatLimit(1);
        mTextWidth = context.getResources().getDimensionPixelOffset(R.dimen.letv_menu_item_text_width);
        // EUI END
        mStatusView = (TextView) findViewById(R.id.item_detail);
        if (mStatusStr != null)
            mStatusView.setText(mStatusStr);
        else
            mStatusView.setVisibility(View.GONE);
        mIconView = (ImageView) findViewById(R.id.item_icon);
        mIconView.setImageDrawable(mNormalIconDrawable);
        mNormalColor = getResources().getColor(R.color.primary_holo_letv_normal);
        mFocusColor = getResources().getColor(R.color.primary_holo_letv_focused);
        mSecColor = getResources().getColor(R.color.primary_holo_letv_info);
    }

    // EUI START liuteng1 modify for letvmenu at 2016-04-11
    /*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!mFinish) {
            Log.d(TAG, " ignore keydown");
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP)
            mKey = keyCode;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        //Log.d(TAG,"onFocusChangeds focused = "+focused+" mKey = "+mKey+" isInTouchMode = "+ isInTouchMode());
        mFinish = false;
        if (isInTouchMode()) {
            mKey = 0;
            enNormal(mKey);
            return;
        }
        if (focused) {
            if ((mKey == 0) && (mKeyBase == KeyEvent.KEYCODE_DPAD_DOWN))
                mKey = KeyEvent.KEYCODE_DPAD_UP;
            else if ((mKey == 0) && (mKeyBase == KeyEvent.KEYCODE_DPAD_UP))
                mKey = KeyEvent.KEYCODE_DPAD_DOWN;
            enLarge(mKey);
        } else {
            mKeyBase = mKey;
            enNormal(mKey);
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }
    */
    // EUI END

    public void move(float times) {
        // EUI START liuteng1 modify for letvmenu at 2016-04-11
        /*
        animate().translationY(getHeight() * times).setDuration(500).start();
        */
        // EUI END
    }

    public void setPos(float times) {
        // EUI START liuteng1 modify for letvmenu at 2016-04-11
        /*
        setY(getHeight() * times);
        */
        // EUI END
    }

    /**
     * start animation then perform click
     *
     * @param anim
     */
    public void startClickAnimation(Animation anim) {
        mIconView.startAnimation(anim);
    }

    /**
     * for radio button
     *
     * @param flag
     */
    public void setSelect(boolean flag) {
        if (flag) {
            mNormalIconDrawable = mContext.getResources().getDrawable(R.drawable.btn_radio_on_letv);
            mFocusIconDrawable = mContext.getResources().getDrawable(R.drawable.btn_radio_on_focused_letv);
            mIconView.setImageDrawable(mFocusIconDrawable);
        } else {
            mNormalIconDrawable = mContext.getResources().getDrawable(R.drawable.btn_radio_off_letv);
            mFocusIconDrawable = mContext.getResources().getDrawable(R.drawable.btn_radio_off_focused_letv);
            mIconView.setImageDrawable(mNormalIconDrawable);
        }
    }

    public void setEnable(boolean enable) {
        if (enable) {
            mNameView.setTextColor(mNormalColor);
            mStatusView.setTextColor(mNormalColor);
        } else {
            mNameView.setTextColor(mSecColor);
            mStatusView.setTextColor(mSecColor);
        }
        mEnable = enable;
        setEnabled(mEnable);
    }

    public ImageView getIconView() {
        return mIconView;
    }

    public TextView getNameView() {
        return mNameView;
    }

    public TextView getStatusView() {
        return mStatusView;
    }

    public void setNormalDrawable(Drawable normalDrawable) {
        this.mNormalIconDrawable = normalDrawable;
        if (mIconView != null) {
            mIconView.setImageDrawable(normalDrawable);
        }
    }

    public void setFocusDrawable(Drawable focusDrawable) {
        this.mFocusIconDrawable = focusDrawable;
    }

    public void refreshCurrent() {
        this.mNoAnimationEnlarge = true;
        requestFocus();
        setPos(FOCUS_POS + 0.5f);
    }

    // EUI START liuteng1 modify for letvmenu at 2016-04-11
    /*
    private void enLarge(int key) {
        ObjectAnimator moveY = null;
        AnimatorSet set = new AnimatorSet();
        set.setDuration(300);
        if (mNoAnimationEnlarge) {
            mNoAnimationEnlarge = false;
            set.setDuration(1);
        }
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mNormalIconDrawable != null && mFocusIconDrawable != null) {
                    Drawable[] iconBmp = new Drawable[2];
                    iconBmp[0] = mNormalIconDrawable;
                    iconBmp[1] = mFocusIconDrawable;
                    TransitionDrawable drw = new TransitionDrawable(iconBmp);
                    mIconView.setImageDrawable(drw);
                    drw.startTransition(100);
                }
                if (mEnable) {
                    mNameView.setTextColor(mFocusColor);
                    mNameView.setTypeface(Typeface.DEFAULT_BOLD);
                    mStatusView.setTextColor(mFocusColor);
                    mStatusView.setTypeface(Typeface.DEFAULT_BOLD);
                }
                mFinish = true;
            }
        });
        ObjectAnimator enlargeX = ObjectAnimator.ofFloat(this, "scaleX", 1.5f);
        ObjectAnimator enlargeY = ObjectAnimator.ofFloat(this, "scaleY", 1.5f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(this, "alpha", 0.5f, 1f);
        if (key == KeyEvent.KEYCODE_DPAD_DOWN) {
            moveY = ObjectAnimator.ofFloat(this, "translationY", getHeight()
                    * (FOCUS_POS - (Integer) getTag() + 0.5f));
            set.play(enlargeX).with(enlargeY).with(alpha).with(moveY);
        } else if (key == KeyEvent.KEYCODE_DPAD_UP) {
            moveY = ObjectAnimator.ofFloat(this, "translationY", getHeight()
                    * (4 - (Integer) getTag() - 0.5f));
            set.play(enlargeX).with(enlargeY).with(alpha).with(moveY);
        } else {
            set.play(enlargeX).with(enlargeY).with(alpha);
        }
        set.start();
    }

    private void enNormal(int key) {
        ObjectAnimator moveY = null;
        AnimatorSet set = new AnimatorSet();
        set.setDuration(400);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIconView.setImageDrawable(mNormalIconDrawable);
                if (mEnable) {
                    mNameView.setTextColor(mNormalColor);
                    mNameView.setTypeface(Typeface.DEFAULT);
                    mStatusView.setTextColor(mSecColor);
                    mStatusView.setTypeface(Typeface.DEFAULT);
                }
            }
        });
        ObjectAnimator narrowX = ObjectAnimator.ofFloat(this, "scaleX", 1f);
        ObjectAnimator narrowY = ObjectAnimator.ofFloat(this, "scaleY", 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(this, "alpha", 0.5f, 1f);
        if (key == KeyEvent.KEYCODE_DPAD_DOWN) {
            moveY = ObjectAnimator.ofFloat(this, "translationY", getHeight()
                    * (2 - (Integer) getTag()));
            set.play(narrowX).with(narrowY).with(alpha).with(moveY);
        } else if (key == KeyEvent.KEYCODE_DPAD_UP) {
            moveY = ObjectAnimator.ofFloat(this, "translationY", getHeight()
                    * (5 - (Integer) getTag()));
            set.play(narrowX).with(narrowY).with(alpha).with(moveY);
        } else
            set.play(narrowX).with(narrowY).with(alpha);
        set.start();
    }
    */

    public void setLargeAnimationEnd() {
        setAlpha((float) 1.0);
        setScaleX((float) 1.5);
        setScaleY((float) 1.5);
        if (mNormalIconDrawable != null && mFocusIconDrawable != null) {
            Drawable[] iconBmp = new Drawable[2];
            iconBmp[0] = mNormalIconDrawable;
            iconBmp[1] = mFocusIconDrawable;
            TransitionDrawable drw = new TransitionDrawable(iconBmp);
            mIconView.setImageDrawable(drw);
            drw.startTransition(100);
        }
        if (mEnable) {
            mNameView.setTextColor(mFocusColor);
            mNameView.setTypeface(Typeface.DEFAULT_BOLD);
            Drawable[] mDrawables = mNameView.getCompoundDrawables();
            Drawable mDrawable = mDrawables[2];
            int mDrawableWidth = 0;
            if (mDrawable != null) {
                mDrawableWidth = mDrawable.getMinimumWidth();
            }
            LayoutParams lp = (LayoutParams) mNameView.getLayoutParams();
            lp.width = mTextWidth - mDrawableWidth / 2;
            mNameView.setLayoutParams(lp);
            mNameView.requestFocus();
            mStatusView.setTextColor(mFocusColor);
            mStatusView.setTypeface(Typeface.DEFAULT_BOLD);
        }
    }

    public void setNormalAnimationEnd() {
        setScaleX((float) 1.0);
        setScaleY((float) 1.0);
        setAlpha((float) 0.5);
        mIconView.setImageDrawable(mNormalIconDrawable);
        if (mEnable) {
            mNameView.setTextColor(mNormalColor);
            mNameView.setTypeface(Typeface.DEFAULT);
            mStatusView.setTextColor(mSecColor);
            mStatusView.setTypeface(Typeface.DEFAULT);
        }
    }
    // EUI END
}
