package com.xstv.desktop.emodule.view;

import android.animation.TimeAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.support.v17.leanback.graphics.ColorOverlayDimmer;
import android.support.v17.leanback.widget.ShadowOverlayContainer;
import android.support.v17.leanback.widget.ShadowOverlayHelper;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import com.xstv.desktop.emodule.R;

import static android.support.v17.leanback.widget.FocusHighlight.ZOOM_FACTOR_LARGE;
import static android.support.v17.leanback.widget.FocusHighlight.ZOOM_FACTOR_MEDIUM;
import static android.support.v17.leanback.widget.FocusHighlight.ZOOM_FACTOR_NONE;
import static android.support.v17.leanback.widget.FocusHighlight.ZOOM_FACTOR_SMALL;
import static android.support.v17.leanback.widget.FocusHighlight.ZOOM_FACTOR_XSMALL;

public class FocusHelper {
    public static final int FOCUS_ANIM_DURATION = 400;

    static boolean isValidZoomIndex(int zoomIndex) {
        return zoomIndex == ZOOM_FACTOR_NONE || getResId(zoomIndex) > 0;
    }

    private static int getResId(int zoomIndex) {
        switch (zoomIndex) {
            case ZOOM_FACTOR_SMALL:
                return android.support.v17.leanback.R.fraction.lb_focus_zoom_factor_small;
            case ZOOM_FACTOR_XSMALL:
                return android.support.v17.leanback.R.fraction.lb_focus_zoom_factor_xsmall;
            case ZOOM_FACTOR_MEDIUM:
                return android.support.v17.leanback.R.fraction.lb_focus_zoom_factor_medium;
            case ZOOM_FACTOR_LARGE:
                return android.support.v17.leanback.R.fraction.lb_focus_zoom_factor_large;
            default:
                return 0;
        }
    }

    public static float getScale(View view) {
        float scale = 1;
        int width = view.getWidth();
        int maxWidth = (int) (view.getContext().getResources().getDisplayMetrics().widthPixels * 0.8f);
        int minWidth = view.getContext().getResources().getDisplayMetrics().widthPixels / 8;
        if (width < minWidth) {
            scale = 1.15f;
        } else if (width > maxWidth) {
            scale = 1.03f;
        } else {
            scale = 1.15f - 0.07f * (width - minWidth) / (maxWidth - minWidth);
        }
        return scale;
    }

    public static class FocusAnimator implements TimeAnimator.TimeListener {
        private final View mView;
        private final int mDuration;
        private final ShadowOverlayContainer mWrapper;
        private float mScaleDiff;
        private float mFocusLevel = 0f;
        private float mFocusLevelStart;
        private float mFocusLevelDelta;
        private final TimeAnimator mAnimator = new TimeAnimator();
        private final Interpolator mInterpolator = new OvershootInterpolator();
        private final ColorOverlayDimmer mDimmer;

        public void animateFocus(boolean select, boolean immediate) {
            endAnimation();
            final float end = select ? 1 : 0;
            if (immediate) {
                setFocusLevel(end);
            } else if (mFocusLevel != end) {
                mFocusLevelStart = mFocusLevel;
                mFocusLevelDelta = end - mFocusLevelStart;
                mAnimator.start();
            }
        }

        public FocusAnimator(View view, float scale, boolean useDimmer, int duration) {
            mView = view;
            mDuration = duration;
            mScaleDiff = scale - 1f;
            if (view instanceof ShadowOverlayContainer) {
                mWrapper = (ShadowOverlayContainer) view;
            } else {
                mWrapper = null;
            }
            mAnimator.setTimeListener(this);
            if (useDimmer) {
                mDimmer = ColorOverlayDimmer.createDefault(view.getContext());
            } else {
                mDimmer = null;
            }
        }

        void setFocusLevel(float level) {
            mFocusLevel = level;
            float scale = 1f + mScaleDiff * level;
            mView.setScaleX(scale);
            mView.setScaleY(scale);
            if (mWrapper != null) {
                mWrapper.setShadowFocusLevel(level);
            } else {
                ShadowOverlayHelper.setNoneWrapperShadowFocusLevel(mView, level);
            }
            if (mDimmer != null) {
                mDimmer.setActiveLevel(level);
                int color = mDimmer.getPaint().getColor();
                if (mWrapper != null) {
                    mWrapper.setOverlayColor(color);
                } else {
                    ShadowOverlayHelper.setNoneWrapperOverlayColor(mView, color);
                }
            }
        }

        float getFocusLevel() {
            return mFocusLevel;
        }

        void endAnimation() {
            mAnimator.end();
        }

        @Override
        public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
            float fraction;
            if (totalTime >= mDuration) {
                fraction = 1;
                mAnimator.end();
            } else {
                fraction = (float) (totalTime / (double) mDuration);
            }
            if (mInterpolator != null) {
                fraction = mInterpolator.getInterpolation(fraction);
            }
            setFocusLevel(mFocusLevelStart + fraction * mFocusLevelDelta);
        }

        public void setScale(float scale) {
            mScaleDiff = scale - 1f;
        }
    }

    public interface FocusHighlightHandler {
        /**
         * Called when an staggered_item gains or loses focus.
         *
         * @param view     The view whose focus is changing.
         * @param hasFocus True if focus is gained; false otherwise.
         * @hide
         */
        void onItemFocused(View view, boolean hasFocus);

        /**
         * Called when the view is being created.
         */
        void onInitializeView(View view);
    }

    static final class OnFocusChangeListener implements View.OnFocusChangeListener {
        View.OnFocusChangeListener mChainedListener;
        FocusHighlightHandler mFocusHighlightHandler;

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (mFocusHighlightHandler != null) {
                mFocusHighlightHandler.onItemFocused(view, hasFocus);
            }
            if (mChainedListener != null) {
                mChainedListener.onFocusChange(view, hasFocus);
            }

        }

        public void setFocusHighlightHandler(FocusHighlightHandler highlightHandler) {
            mFocusHighlightHandler = highlightHandler;
        }

    }

    static class ItemFocusHighlight implements FocusHighlightHandler {
        private static boolean sInitialized;
        private static float sSelectScale;
        private static int sDuration;

        ItemFocusHighlight(Context context) {
            lazyInit(context.getResources());
        }

        private static void lazyInit(Resources res) {
            if (!sInitialized) {
                sSelectScale = 1.1f;
                //Float.parseFloat(res.getString(R.dimen.lb_browse_header_select_scale));
                sDuration = 150;
                //Integer.parseInt(res.getString(R.dimen.lb_browse_header_select_duration));
                sInitialized = true;
            }
        }

        private void viewFocused(View view, boolean hasFocus) {
            view.setSelected(hasFocus);
            FocusHelper.FocusAnimator animator = (FocusHelper.FocusAnimator) view.getTag(android.support.v17.leanback.R.id.lb_focus_animator);
            if (animator == null) {
                animator = new FocusHelper.FocusAnimator(view, sSelectScale, false, sDuration);
                view.setTag(android.support.v17.leanback.R.id.lb_focus_animator, animator);
            }
            animator.animateFocus(hasFocus, false);
        }

        public void onItemFocused(View view, boolean hasFocus) {
            viewFocused(view, hasFocus);
        }

        public void onInitializeView(View view) {
        }

    }

    public static class BrowseItemFocusHighlight implements FocusHighlightHandler {
        private static final int DURATION_MS = FOCUS_ANIM_DURATION;

        private int mScaleIndex;
        private final boolean mUseDimmer;
        private float mNormalAlpha = 0.8f;
        private float mSelectedAlpha = 1;

        public BrowseItemFocusHighlight(int zoomIndex, boolean useDimmer) {
            if (!isValidZoomIndex(zoomIndex)) {
                throw new IllegalArgumentException("Unhandled zoom index");
            }
            mScaleIndex = zoomIndex;
            mUseDimmer = useDimmer;
        }

        private float getScale(Resources res) {
            return mScaleIndex == ZOOM_FACTOR_NONE ? 1f :
                    res.getFraction(getResId(mScaleIndex), 1, 1);
        }

        public float getScale(View view) {
            return FocusHelper.getScale(view);
        }


        public void onItemFocused(View view, boolean hasFocus) {
            view.setSelected(hasFocus);
            getOrCreateAnimator(view).setScale(getScale(view));
            getOrCreateAnimator(view).animateFocus(hasFocus, false);

            /*View breakImg = view.findViewById(R.id.di_break_img);
            if (breakImg != null && breakImg.getVisibility() == View.VISIBLE) {
                breakImg.setPivotY(breakImg.getHeight());
                getOrCreateAnimator(breakImg).setScale((getScale(view) - 1) / 3 + 1);
                getOrCreateAnimator(breakImg).animateFocus(hasFocus, false);
            }
            ScanLightView effect = (ScanLightView) view.findViewById(R.id.di_focus_effect);
            if (effect != null) {
                if (hasFocus) {
                    effect.setVisibility(View.VISIBLE);
                    effect.startAnim();
                } else {
                    effect.setVisibility(View.INVISIBLE);
                    effect.cancelAnim();
                }
            }
            if (hasFocus && view.getTag(R.id.view_item) != null) {
                DisplayItemSelected.onSelect(view.getContext(), (DisplayItem) view.getTag(R.id.view_item), BackgroundHelper.BACKGROUND_LEVEL_1);
            }*/
        }

        public void onInitializeView(View view) {
            getOrCreateAnimator(view).animateFocus(false, true);
            OnFocusChangeListener focusChangeListener = new OnFocusChangeListener();
            focusChangeListener.mChainedListener = view.getOnFocusChangeListener();
            focusChangeListener.setFocusHighlightHandler(this);
            view.setOnFocusChangeListener(focusChangeListener);
        }

        protected FocusAnimator getOrCreateAnimator(View view) {
            FocusAnimator animator = (FocusAnimator) view.getTag(android.support.v17.leanback.R.id.lb_focus_animator);
            if (animator == null) {
                animator = new FocusAnimator(
                        view, FocusHelper.getScale(view), mUseDimmer, DURATION_MS);
                view.setTag(android.support.v17.leanback.R.id.lb_focus_animator, animator);
            }
            return animator;
        }
    }

    public static class HomeItemFocusHighlight extends BrowseItemFocusHighlight {

        public HomeItemFocusHighlight(int zoomIndex, boolean useDimmer) {
            super(zoomIndex, useDimmer);
        }

        @Override
        public float getScale(View view) {
            float scale = 1.1f;
            view.setTag(R.id.focus_scale, scale);
            return scale;
        }

    }

    public static class HeaderItemFocusHighlight implements FocusHighlightHandler {
        private static boolean sInitialized;
        private static float sSelectScale;
        private static int sDuration;

        public HeaderItemFocusHighlight(Context context) {
            lazyInit(context.getResources());
        }

        private static void lazyInit(Resources res) {
            if (!sInitialized) {
                sSelectScale = 1.05f;
                //Float.parseFloat(res.getString(R.dimen.lb_browse_header_select_scale));
                sDuration = 150;
                //Integer.parseInt(res.getString(R.dimen.lb_browse_header_select_duration));
                sInitialized = true;
            }
        }

        private void viewFocused(View view, boolean hasFocus) {
            //view.setSelected(hasFocus);
            FocusHelper.FocusAnimator animator = (FocusHelper.FocusAnimator) view.getTag(android.support.v17.leanback.R.id.lb_focus_animator);
            if (animator == null) {
                animator = new FocusHelper.FocusAnimator(view, sSelectScale, false, sDuration);
                view.setTag(android.support.v17.leanback.R.id.lb_focus_animator, animator);
            }
            animator.animateFocus(hasFocus, false);
            if (hasFocus && view.getTag(R.id.view_item) != null) {
                //DisplayItemSelected.onSelect(view.getContext(), (DisplayItem) view.getTag(R.id.view_item), BackgroundHelper.BACKGROUND_LEVEL_2);
            }
        }

        public void onItemFocused(View view, boolean hasFocus) {
            viewFocused(view, hasFocus);
        }

        public void onInitializeView(View view) {
        }

    }

}
