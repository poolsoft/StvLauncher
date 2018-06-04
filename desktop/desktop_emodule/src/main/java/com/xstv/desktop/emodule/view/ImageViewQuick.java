package com.xstv.desktop.emodule.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Debug;
import android.os.SystemClock;
import android.support.v4.os.TraceCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class ImageViewQuick extends ImageView {
    Drawable mDrawable;
    private static final int DEFAULT_DURATION_MS = 200;
    private static final String TAG = "TransitionDrawable";

    private static final boolean mEnableDebug = false;
    private static final boolean mEnableDetailTime = false;
    /**
     * A transition is about to start.
     */
    private static final int TRANSITION_STARTING = 0;

    /**
     * The transition has started and the animation is in progress
     */
    private static final int TRANSITION_RUNNING = 1;

    /**
     * No transition will be applied
     */
    private static final int TRANSITION_NONE = 2;

    private static int mDrawSeq = 0;
    /**
     * The current state of the transition. One of {@link #TRANSITION_STARTING},
     * {@link #TRANSITION_RUNNING} and {@link #TRANSITION_NONE}
     */
    private int mTransitionState = TRANSITION_NONE;

    private long mStartTimeMillis;
    private int mFrom;
    private int mTo;
    private int mDuration;
    private int mOriginalDuration;
    private int mAlpha = 0;
    private boolean mDoTransition = true;

    public ImageViewQuick(Context context) {
        super(context);
    }

    public ImageViewQuick(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageViewQuick(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        canvas.save();
        canvas.clipRect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
        if (mDrawable == null) return;

        if (getScaleType() == ScaleType.CENTER_CROP) {
            float scale;
            float dx = 0, dy = 0;
            int dwidth = mDrawable.getIntrinsicWidth();
            int dheight = mDrawable.getIntrinsicHeight();
            int vwidth = getWidth();
            int vheight = getHeight();
            if (dwidth * vheight > vwidth * dheight) {
                scale = (float) vheight / (float) dheight;
                dx = (vwidth - dwidth * scale) * 0.5f;
            } else {
                scale = (float) vwidth / (float) dwidth;
                dy = (vheight - dheight * scale) * 0.5f;
            }
            mDrawable.setBounds((int) dx, (int) dy, (int) (mDrawable.getIntrinsicWidth() * scale + dx), (int) (mDrawable.getIntrinsicHeight() * scale + dy));
        } else {
            mDrawable.setBounds(0, 0, getWidth(), getHeight());
        }
        if (!mDoTransition) {
            mDrawable.draw(canvas);
            return;
        }

        mDrawSeq++;
        long start_draw_time = 0;
        long start_draw_time_t = 0;
        long last_time = 0;
        long last_time_t = 0;
        long cur_time = 0;
        long cur_time_t = 0;

        boolean done = true;

        if (mEnableDebug) {
            if (mTransitionState == TRANSITION_STARTING) {
                TraceCompat.beginSection("sTrans#draw" + mDrawSeq + "#" + Integer.toHexString(System.identityHashCode(this)));
            } else {
                TraceCompat.beginSection("rTrans#draw" + mDrawSeq + "#" + Integer.toHexString(System.identityHashCode(this)));
            }
            start_draw_time = System.nanoTime();
            start_draw_time_t = Debug.threadCpuTimeNanos();
        }

        switch (mTransitionState) {
            case TRANSITION_STARTING:
                mStartTimeMillis = SystemClock.uptimeMillis();
                done = false;
                mTransitionState = TRANSITION_RUNNING;
                break;

            case TRANSITION_RUNNING:
                if (mStartTimeMillis >= 0) {
                    float normalized = (float)
                            (SystemClock.uptimeMillis() - mStartTimeMillis) / mDuration;
                    done = normalized >= 1.0f;
                    normalized = Math.min(normalized, 1.0f);
                    mAlpha = (int) (mFrom + (mTo - mFrom) * normalized);
                }
                break;
        }

        if (mEnableDetailTime) {
            last_time = System.nanoTime();
            last_time_t = Debug.threadCpuTimeNanos();
            Log.d(TAG, "state=" + (last_time - start_draw_time) + "," + (last_time_t - start_draw_time_t));
        }

        final int alpha = mAlpha;
        final boolean crossFade = true;  // see setImageDrawable, when code runs to here, crossFade always true
        if (mDrawable instanceof TransitionDrawable) {
            TransitionDrawable tr = (TransitionDrawable) mDrawable;

            if (done) {
                // the setAlpha() calls below trigger invalidation and redraw. If we're done, just draw
                // the appropriate drawable[s] and return
                if (!crossFade || alpha == 0) {
                    tr.getDrawable(0).draw(canvas);
                }
                if (alpha == 0xFF) {
                    tr.getDrawable(1).draw(canvas);
                }
                if (mEnableDebug) {
                    Log.d(TAG, mDrawSeq + "done, dur=" + (System.nanoTime() - start_draw_time) + "," +
                            (Debug.threadCpuTimeNanos() - start_draw_time_t));
                    TraceCompat.endSection();
                }
                return;
            }

            Drawable d;
            d = tr.getDrawable(0);
            if (crossFade) {
                d.setAlpha(255 - alpha);
            }

            if (mEnableDetailTime) {
                cur_time = System.nanoTime();
                cur_time_t = Debug.threadCpuTimeNanos();
                Log.d(TAG, "1stalpha=" + (cur_time - last_time) + "," + (cur_time_t - last_time_t));
                last_time = cur_time;
                last_time_t = cur_time_t;
            }

            d.draw(canvas);

            if (mEnableDetailTime) {
                cur_time = System.nanoTime();
                cur_time_t = Debug.threadCpuTimeNanos();
                Log.d(TAG, "1stdraw=" + (cur_time - last_time) + "," + (cur_time_t - last_time_t));
                last_time = cur_time;
                last_time_t = cur_time_t;
            }

            if (crossFade) {
                d.setAlpha(0xFF);
            }

            if (mEnableDetailTime) {
                cur_time = System.nanoTime();
                cur_time_t = Debug.threadCpuTimeNanos();
                Log.d(TAG, "2ndalpha=" + (cur_time - last_time) + "," + (cur_time_t - last_time_t));
                last_time = cur_time;
                last_time_t = cur_time_t;
            }

            if (alpha > 0) {
                d = tr.getDrawable(1);
                d.setAlpha(alpha);

                if (mEnableDetailTime) {
                    cur_time = System.nanoTime();
                    cur_time_t = Debug.threadCpuTimeNanos();
                    Log.d(TAG, "3rdalpha=" + (cur_time - last_time) + "," + (cur_time_t - last_time_t));
                    last_time = cur_time;
                    last_time_t = cur_time_t;
                }

                d.draw(canvas);

                if (mEnableDetailTime) {
                    cur_time = System.nanoTime();
                    cur_time_t = Debug.threadCpuTimeNanos();
                    Log.d(TAG, "2nddraw=" + (cur_time - last_time) + "," + (cur_time_t - last_time_t));
                    last_time = cur_time;
                    last_time_t = cur_time_t;
                }

                d.setAlpha(0xFF);

                if (mEnableDetailTime) {
                    cur_time = System.nanoTime();
                    cur_time_t = Debug.threadCpuTimeNanos();
                    Log.d(TAG, "4thalpha=" + (cur_time - last_time) + "," + (cur_time_t - last_time_t));
                    last_time = cur_time;
                    last_time_t = cur_time_t;
                }
            }
        } else {
            mDrawable.draw(canvas);
        }

        if (!done) {
            invalidate();

            if (mEnableDetailTime) {
                cur_time = System.nanoTime();
                cur_time_t = Debug.threadCpuTimeNanos();
                Log.d(TAG, "lastinv=" + (cur_time - last_time) + "," + (cur_time_t - last_time_t));
                last_time = cur_time;
                last_time_t = cur_time_t;
            }
        }

        if (mEnableDebug) {
            Log.d(TAG, mDrawSeq + "not done, dur=" + (System.nanoTime() - start_draw_time) + ","
                    + (Debug.threadCpuTimeNanos() - start_draw_time_t));
            TraceCompat.endSection();
        }

        canvas.restore();
    }

    public void setImageDrawable(Drawable drawable) {
        if (drawable == mDrawable) {
            return;
        }
        if (mDrawable != null) {
            mDrawable.setCallback(null);
            unscheduleDrawable(mDrawable);
        }


        mDrawable = drawable;

        if (mDrawable != null) {
            mDoTransition = false;

            if (mDrawable instanceof TransitionDrawable) {
                TransitionDrawable tr = (TransitionDrawable) mDrawable;
                if (tr.isCrossFadeEnabled() && tr.getNumberOfLayers() == 2) {
                    Drawable tr1 = tr.getDrawable(1);
                    if (tr1 instanceof com.bumptech.glide.load.resource.gif.GifDrawable)
                        tr1.setCallback(this);

                    mDoTransition = true;
                    startTransition(DEFAULT_DURATION_MS);
                }
            }

            if (!mDoTransition) {
                mDrawable.setCallback(this);
                invalidate();
            }

            if (mDrawable.isStateful()) {
                mDrawable.setState(getDrawableState());
            }
            mDrawable.setVisible(getVisibility() == VISIBLE, true);
        } else {
            invalidate();
        }
    }

    public void setImageResource(int resource) {
        setImageDrawable(getResources().getDrawable(resource));
    }

    public void setImageBitmap(Bitmap bm) {
        // if this is used frequently, may handle bitmaps explicitly
        // to reduce the intermediate drawable object
        setImageDrawable(new BitmapDrawable(getResources(), bm));
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public boolean isOpaque() {
        return super.isOpaque() || mDrawable != null;
    }

    @Override
    protected boolean verifyDrawable(Drawable dr) {
        return mDrawable != null || super.verifyDrawable(dr);
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mDrawable != null) mDrawable.jumpToCurrentState();
    }

    @Override
    public void invalidateDrawable(Drawable dr) {
        invalidate();
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (mDrawable != null) {
            mDrawable.setVisible(visibility == VISIBLE, false);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mDrawable != null) {
            mDrawable.setVisible(getVisibility() == VISIBLE, false);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDrawable != null) {
            mDrawable.setVisible(false, false);
        }
    }

    /**
     * Begin the second layer on top of the first layer.
     *
     * @param durationMillis The length of the transition in milliseconds
     */
    public void startTransition(int durationMillis) {
        mFrom = 0;
        mTo = 255;
        mAlpha = 0;
        mDuration = mOriginalDuration = durationMillis;
        mTransitionState = TRANSITION_STARTING;
        invalidate();
    }

    /**
     * Show only the first layer.
     */
    public void resetTransition() {
        mAlpha = 0;
        mTransitionState = TRANSITION_NONE;
        invalidate();
    }
}
