package com.xstv.desktop.emodule.presenter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xstv.desktop.emodule.R;
import com.xstv.desktop.emodule.mode.DisplayItem;
import com.xstv.desktop.emodule.util.InflatTracer;
import com.xstv.desktop.emodule.util.Utils;
import com.xstv.desktop.emodule.view.BreakImageView;
import com.xstv.desktop.emodule.view.FocusHelper;
import com.xstv.desktop.emodule.view.HintTextView;
import com.xstv.desktop.emodule.view.ItemTextView;
import com.xstv.library.base.Logger;

import static android.support.v17.leanback.widget.FocusHighlight.ZOOM_FACTOR_SMALL;

public class DisplayItemPresenter extends ItemBasePresenter {

    Logger mLogger = Logger.getLogger("EModule", "DisplayItemPresenter");
    public FocusHelper.FocusHighlightHandler mFocusHighlight = new FocusHelper.BrowseItemFocusHighlight(ZOOM_FACTOR_SMALL, false);

    protected OnFocusChangeListener mOnFocusChangeListener;
    protected int mLayoutResId = R.layout.poster_item;

    public DisplayItemPresenter() {

    }

    public class VH extends ItemViewHolder {
        public ImageView mImg;
        public BreakImageView mBreakImg;
        public TextView mTitle;
        public ItemTextView mText;
        public HintTextView mHintText;
        public TextView mFreeHint;

        public VH(View aView) {
            super(aView);
            mImg = (ImageView) aView.findViewById(R.id.di_img);
            mTitle = (TextView) aView.findViewById(R.id.di_title);
            mText = (ItemTextView) aView.findViewById(R.id.di_text);
            mHintText = (HintTextView) aView.findViewById(R.id.di_hint);
            mBreakImg = (BreakImageView) aView.findViewById(R.id.di_break_img);
            mFreeHint = (TextView) aView.findViewById(R.id.di_free_hint);
            aView.setTag(VIEW_HOLD_ID, this);
        }

        public View getBaseSizeView() {
            return mImg;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        VH vh = (VH) createViewHolder(parent);
        View presenterView = vh.view;
        if (mOnFocusChangeListener == null) {
            newFocusChangeListener();
        }
        vh.view.setOnFocusChangeListener(mOnFocusChangeListener);
        if (mFocusHighlight != null) {
            mFocusHighlight.onInitializeView(presenterView);
        }
        if (mDefaultBgDrawable == null) {
        }
        return vh;
    }

    protected void newFocusChangeListener() {
        mOnFocusChangeListener = new OnFocusChangeListener();
    }

    public ViewHolder createViewHolder(ViewGroup parent) {
        View presenterView = InflatTracer.inflate(parent.getContext(), getLayoutResId(), parent);
        presenterView.setFocusable(true);
        VH vh = new VH(presenterView);
        return vh;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }

    public void setLayoutResId(int layoutResId) {
        mLayoutResId = layoutResId;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Object item) {
        super.onBindViewHolder(holder, item);
        mLogger.d(">>> onBindViewHolder " + item);
        final VH vh = (VH) holder;
        final DisplayItem displayItem = (DisplayItem) item;

        ViewGroup.LayoutParams params = vh.mImg.getLayoutParams();
        params.width = displayItem.ui.width;
        params.height = displayItem.ui.height;
        supportReleasePicWhenPaused(vh, item);
        vh.view.setTag(R.id.view_item, item);

        vh.mTitle.setTag(R.id.text_view_title, displayItem.title);
        vh.mTitle.setText(displayItem.title);
        vh.mTitle.setVisibility(View.VISIBLE);

        onBindViewImageOnly(vh, item);
    }

    public void onBindViewImageOnly(ViewHolder viewHolder, Object item) {
        DisplayItem displayItem = (DisplayItem) item;
        final VH vh = (VH) viewHolder;
        if (vh.mImg != null) {
            RequestOptions myOptions = new RequestOptions()
                    .override(vh.mImg.getWidth(), vh.mImg.getHeight())
                    .error(R.drawable.ic_launcher);

            Glide.with(viewHolder.view.getContext())
                    .applyDefaultRequestOptions(myOptions)
                    .load(displayItem.src)
                    .into(vh.mImg);
            vh.mImg.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean isDisplayItemDefaultPresenter() {
        return true;
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        super.onUnbindViewHolder(viewHolder);
        VH vh = (VH) viewHolder;
        if (vh.mImg != null) {
            vh.mImg.setTag(R.id.view_image_url, null);
        }
    }

    public class OnFocusChangeListener implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
        }
    }

    protected void postLoadImage(final ImageView view, final String url) {
        postLoadImage(view, url, true);
    }

    protected void postLoadImage(final ImageView view, final String url, final boolean hasHolder) {
        view.setTag(R.id.view_image_url, url);
        if (Utils.longPressScrolling()) {
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (url != null && url.equals(view.getTag(R.id.view_image_url))) {
                        if (hasHolder) {
                        } else {
                        }
                    }
                }
            }, 500);
        } else {
            view.post(new Runnable() {
                @Override
                public void run() {
                    if (hasHolder) {
                    } else {
                    }
                }
            });
        }
    }
}
