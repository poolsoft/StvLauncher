
package com.xstv.desktop.app.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xstv.library.base.LetvLog;
import com.xstv.desktop.app.R;
import com.xstv.desktop.app.bean.PosterInfo;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.util.Utilities;

public class RecommPosterCellView extends PosterCellView {
    private static final String TAG = RecommPosterCellView.class.getSimpleName();

    private ImageView mEcoImageView;
    private ImageView mLogoImageView;
    private ImageView mPosterSimpleDraweeView;
    private ImageView mLogoSimpleDraweeView;
    private TextView mTitleTV;
    private TextView mSubTitleTV;
    private View mShadeView;
    private Handler mHandler = new Handler();

    private boolean isShowSubTitle;

    public RecommPosterCellView(Context context) {
        this(context, null);
    }

    public RecommPosterCellView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecommPosterCellView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void initView(Context context) {
        ViewGroup rootView = (ViewGroup) View.inflate(context, R.layout.recomm_poster_cellview_layout, this);
        mShadeView = findViewById(R.id.shade);
        mTitleTV = (TextView) findViewById(R.id.poster_cellview_title);
        mSubTitleTV = (TextView) findViewById(R.id.poster_cellview_subtitle);

        mTitleTV.setText("title");

        if (Utilities.verifySupportSdk(Utilities.support_sdk_version_100)) {
            mEcoImageView = new ImageView(context);
            mEcoImageView.setFocusable(false);
            mEcoImageView.setFocusableInTouchMode(false);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            rootView.addView(mEcoImageView, 0, params);

            mLogoImageView = new ImageView(context);
            mLogoImageView.setFocusable(false);
            mLogoImageView.setFocusableInTouchMode(false);
            mLogoImageView.setVisibility(INVISIBLE);
            params = new RelativeLayout.LayoutParams(40, 40);
            params.topMargin = 10;
            params.rightMargin = 10;
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            rootView.addView(mLogoImageView, params);
        } else {
            mPosterSimpleDraweeView = new ImageView(context);
            mPosterSimpleDraweeView.setFocusable(false);
            mPosterSimpleDraweeView.setFocusableInTouchMode(false);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            rootView.addView(mPosterSimpleDraweeView, 0, params);

            mLogoSimpleDraweeView = new ImageView(context);
            mLogoSimpleDraweeView.setFocusable(false);
            mLogoSimpleDraweeView.setFocusableInTouchMode(false);
            mLogoSimpleDraweeView.setVisibility(INVISIBLE);
            params = new RelativeLayout.LayoutParams(40, 40);
            params.topMargin = 10;
            params.rightMargin = 10;
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            rootView.addView(mLogoSimpleDraweeView, params);
        }
    }

    @Override
    public void bindData(ItemInfo itemInfo) {
        super.bindData(itemInfo);
        //LetvLog.d(TAG, "bindData folderInfo = " + itemInfo + " url = ");
        if (mEcoImageView != null) {
            if (itemInfo == null) {
                setEcoImageView(mEcoImageView, null);
                return;
            }
            if (itemInfo instanceof PosterInfo) {
                String iconUrl = ((PosterInfo) itemInfo).getIconUrl();
                LetvLog.d(TAG, " iconUrl=" + iconUrl + " mEcoImageView=" + mEcoImageView.getVisibility());
                if (iconUrl == null) {
                    setEcoImageView(mEcoImageView, null);
                } else {
                    setEcoImageView(mEcoImageView, iconUrl);
                }
            }
        } else if (mPosterSimpleDraweeView != null) {
            if (itemInfo == null) {
                setPosterSimpleDraweeView(mPosterSimpleDraweeView, null);
                return;
            }
            if (itemInfo instanceof PosterInfo) {
                String iconUrl = ((PosterInfo) itemInfo).getIconUrl();
                if (iconUrl == null) {
                    setPosterSimpleDraweeView(mPosterSimpleDraweeView, null);
                } else {
                    setPosterSimpleDraweeView(mPosterSimpleDraweeView, iconUrl);
                }
            }
        }

        if (mLogoImageView != null) {
            if (itemInfo != null && itemInfo instanceof PosterInfo && ((PosterInfo) itemInfo).getLogoUrl() != null) {
                if (isFocused() && mLogoImageView.getVisibility() != View.VISIBLE) {
                    mLogoImageView.setVisibility(VISIBLE);
                    setLogoImageView(mLogoImageView, ((PosterInfo) itemInfo).getLogoUrl());
                } else {
                    setLogoImageView(mLogoImageView, "");
                    mLogoImageView.setVisibility(INVISIBLE);
                }
            }
        } else if (mLogoSimpleDraweeView != null) {
            if (itemInfo != null && itemInfo instanceof PosterInfo && ((PosterInfo) itemInfo).getLogoUrl() != null) {
                if (isFocused() && mLogoSimpleDraweeView.getVisibility() != View.VISIBLE) {
                    mLogoSimpleDraweeView.setVisibility(VISIBLE);
                    setLogoSimpleDraweeView(mLogoSimpleDraweeView, ((PosterInfo) itemInfo).getLogoUrl());
                } else {
                    setLogoSimpleDraweeView(mLogoSimpleDraweeView, "");
                    mLogoSimpleDraweeView.setVisibility(INVISIBLE);
                }
            }
        }
    }

    @Override
    public void setLabel(ItemInfo itemInfo) {
        super.setLabel(itemInfo);
        String title = ((PosterInfo) itemInfo).getFirstTitle();
        String subTitle = ((PosterInfo) itemInfo).getSecondTitle();
        if (!TextUtils.isEmpty(title)) {
            mTitleTV.setText(title);
        }
        if (!TextUtils.isEmpty(subTitle)) {
            isShowSubTitle = true;
            mSubTitleTV.setVisibility(View.VISIBLE);
            mSubTitleTV.setText(subTitle);
        } else {
            isShowSubTitle = false;
            mSubTitleTV.setVisibility(View.GONE);
        }

        setTitleViewBottomMargin();
    }

    private void setTitleViewBottomMargin() {
        int singleMarginBottom = mContext.getResources().getDimensionPixelOffset(R.dimen.poster_cellview_label_marginBottom);
        int marginBottom = mContext.getResources().getDimensionPixelOffset(R.dimen.poster_cellview_title_marginBottom);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTitleTV.getLayoutParams();
        params.bottomMargin = isShowSubTitle ? marginBottom : singleMarginBottom;
        mTitleTV.setLayoutParams(params);
    }

    @Override
    protected void setShadeVisibility() {
        super.setShadeVisibility();
        if (mItemInfo == null) {
            mShadeView.setVisibility(View.GONE);
            return;
        }

        String firstTitle = ((PosterInfo) mItemInfo).getFirstTitle();
        String secondTitle = ((PosterInfo) mItemInfo).getSecondTitle();
        if (TextUtils.isEmpty(firstTitle) && TextUtils.isEmpty(secondTitle)) {
            mShadeView.setVisibility(View.GONE);
            return;
        }

        mShadeView.setVisibility(View.VISIBLE);
    }

    public void setDeleteState(boolean isFocus) {
        super.setDeleteState(isFocus);
        setImageViewAlpha(0.3f);
    }

    public void resetDeleteState() {
        super.resetDeleteState();
        setImageViewAlpha(0.3f);
    }

    public void setNewFolderState(boolean isFocus) {
        super.setNewFolderState(isFocus);
        setImageViewAlpha(0.3f);
    }

    public void resetState() {
        super.resetState();
        setImageViewAlpha(1.0f);
    }

    public void setAddState() {
        super.setAddState();
        setImageViewAlpha(0.3f);
    }

    private void setImageViewAlpha(float alpha) {
        if (mTitleTV != null) {
            mTitleTV.setAlpha(alpha);
        }
        if (mSubTitleTV != null) {
            mSubTitleTV.setAlpha(alpha);
        }

        if (mEcoImageView != null) {
            mEcoImageView.setAlpha(alpha);
        }
        if (mLogoImageView != null) {
            mLogoImageView.setAlpha(alpha);
        }
        if (mPosterSimpleDraweeView != null) {
            mPosterSimpleDraweeView.setAlpha(alpha);
        }
        if (mLogoSimpleDraweeView != null) {
            mLogoSimpleDraweeView.setAlpha(alpha);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (isShowLoading) {
            if (mLogoImageView != null) {
                if (mLogoImageView.getVisibility() != View.VISIBLE) {
                    setLogoImageView(mLogoImageView, ((PosterInfo) mItemInfo).getLogoUrl());
                    mLogoImageView.setVisibility(VISIBLE);
                }
            } else if (mLogoSimpleDraweeView != null) {
                if (mLogoSimpleDraweeView.getVisibility() != View.VISIBLE) {
                    setLogoSimpleDraweeView(mLogoSimpleDraweeView, ((PosterInfo) mItemInfo).getLogoUrl());
                    mLogoSimpleDraweeView.setVisibility(VISIBLE);
                }
            }
            if (isShowSubTitle && mSubTitleTV.getVisibility() == View.VISIBLE) {
                mSubTitleTV.setVisibility(View.INVISIBLE);
            }
            if (mTitleTV.getVisibility() == View.VISIBLE) {
                mTitleTV.setVisibility(View.INVISIBLE);
            }
        } else {
            if (isShowSubTitle && mSubTitleTV.getVisibility() != View.VISIBLE) {
                mSubTitleTV.setVisibility(View.VISIBLE);
            }
            if (mTitleTV.getVisibility() != View.VISIBLE) {
                mTitleTV.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onLoadingFinished() {
        super.onLoadingFinished();
        if (isShowSubTitle && mSubTitleTV.getVisibility() != View.VISIBLE) {
            mSubTitleTV.setVisibility(View.VISIBLE);
        }
        if (mTitleTV.getVisibility() != View.VISIBLE) {
            mTitleTV.setVisibility(View.VISIBLE);
        }
        if (mLogoImageView != null) {
            if (mLogoImageView.getVisibility() == View.VISIBLE && !mHasFocus) {
                setLogoImageView(mLogoImageView, "");
                mLogoImageView.setVisibility(INVISIBLE);
            }
        } else if (mLogoSimpleDraweeView != null) {
            if (mLogoSimpleDraweeView.getVisibility() == View.VISIBLE && !mHasFocus) {
                setLogoSimpleDraweeView(mLogoSimpleDraweeView, "");
                mLogoSimpleDraweeView.setVisibility(INVISIBLE);
            }
        }
    }

    @Override
    public void onFocusChange(View v, final boolean hasFocus) {
        super.onFocusChange(v, hasFocus);
        // LetvLog.d(TAG, "onFocusChange isShowLoading = " + isShowLoading);
        mHandler.removeCallbacksAndMessages(null);
        if (hasFocus) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mTitleTV != null) {
                        mTitleTV.setSelected(hasFocus);
                    }
                }
            }, 500);
        } else {
            if (mTitleTV != null) {
                mTitleTV.setSelected(hasFocus);
            }
        }
        if (isShowLoading) {
            return;
        }
        if (mLogoImageView != null) {
            if (hasFocus && mItemInfo != null) {
                setLogoImageView(mLogoImageView, ((PosterInfo) mItemInfo).getLogoUrl());
                mLogoImageView.setVisibility(VISIBLE);
            } else {
                setLogoImageView(mLogoImageView, "");
                mLogoImageView.setVisibility(INVISIBLE);
            }
        } else if (mLogoSimpleDraweeView != null) {
            if (hasFocus && mItemInfo != null) {
                setLogoSimpleDraweeView(mLogoSimpleDraweeView, ((PosterInfo) mItemInfo).getLogoUrl());
                mLogoSimpleDraweeView.setVisibility(VISIBLE);
            } else {
                setLogoSimpleDraweeView(mLogoSimpleDraweeView, "");
                mLogoSimpleDraweeView.setVisibility(INVISIBLE);
            }
        }
    }

    @Override
    protected Point getLoadingTextPoint() {
        Point point = new Point();
        if (isShowSubTitle) {
            int x = mSubTitleTV.getLeft();
            int y = mSubTitleTV.getTop() + mSubTitleTV.getBaseline();
            point.set(x, y);
        } else {
            int x = mTitleTV.getLeft();
            int y = mTitleTV.getTop() + mTitleTV.getBaseline();
            point.set(x, y);
        }
        return point;
    }

    @Override
    protected RectF getLogoRect() {
        if (mItemInfo == null || TextUtils.isEmpty(((PosterInfo) mItemInfo).getLogoUrl())) {
            return null;
        }
        RectF rectF = new RectF();
        if (mLogoImageView != null) {
            rectF.left = mLogoImageView.getLeft();
            rectF.top = mLogoImageView.getTop();
            rectF.right = mLogoImageView.getRight();
            rectF.bottom = mLogoImageView.getBottom();
        } else if (mLogoSimpleDraweeView != null) {
            rectF.left = mLogoSimpleDraweeView.getLeft();
            rectF.top = mLogoSimpleDraweeView.getTop();
            rectF.right = mLogoSimpleDraweeView.getRight();
            rectF.bottom = mLogoSimpleDraweeView.getBottom();
        }
        return rectF;
    }

    @Override
    protected float getLoadingTextSize() {
        float textSize = 0;
        if (isShowSubTitle) {
            textSize = mSubTitleTV.getTextSize();
        } else {
            textSize = mTitleTV.getTextSize();
        }
        return textSize;
    }
}
