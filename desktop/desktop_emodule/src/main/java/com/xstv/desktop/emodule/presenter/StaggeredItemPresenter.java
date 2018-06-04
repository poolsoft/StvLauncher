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
import com.xstv.desktop.emodule.view.FocusHelper;
import com.xstv.library.base.Logger;
import com.xstv.library.widget.recyclerview.StaggeredGridLayoutManager;

import static android.support.v17.leanback.widget.FocusHighlight.ZOOM_FACTOR_SMALL;

public class StaggeredItemPresenter extends ItemBasePresenter {

    Logger mLogger = Logger.getLogger("EModule", "StaggeredItemPresenter");
    public FocusHelper.FocusHighlightHandler mFocusHighlight = new FocusHelper.BrowseItemFocusHighlight(ZOOM_FACTOR_SMALL, false);

    protected StaggeredItemPresenter.OnFocusChangeListener mOnFocusChangeListener;
    protected int mLayoutResId = R.layout.staggered_item;

    public StaggeredItemPresenter() {

    }

    public class VH extends ItemViewHolder {
        public ImageView iv;
        public TextView tv;

        public VH(View aView) {
            super(aView);
            iv = aView.findViewById(R.id.image);
            tv = aView.findViewById(R.id.title);
            aView.setTag(VIEW_HOLD_ID, this);
        }

        public View getBaseSizeView() {
            return tv;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        VH vh = (VH) createViewHolder(parent);
        View presenterView = vh.view;
        if (mOnFocusChangeListener == null) {
            mOnFocusChangeListener = new StaggeredItemPresenter.OnFocusChangeListener();
        }
        vh.view.setOnFocusChangeListener(mOnFocusChangeListener);
        if (mFocusHighlight != null) {
            mFocusHighlight.onInitializeView(presenterView);
        }
        return vh;
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
        DisplayItem displayItem = (DisplayItem) item;

        final StaggeredGridLayoutManager.LayoutParams lp =
                (StaggeredGridLayoutManager.LayoutParams) vh.view.getLayoutParams();


        lp.span = displayItem.ui.rowSpan;
        lp.width = displayItem.ui.width;
        vh.view.setLayoutParams(lp);
        vh.view.setPadding(8,8,8,8);

        vh.view.setTag(R.id.view_item, item);
        if (vh.tv != null) {
            vh.tv.setTag(R.id.text_view_title, displayItem.title);
            vh.tv.setText(displayItem.title);
        }

        onBindViewImageOnly(vh, item);
    }

    public void onBindViewImageOnly(ViewHolder viewHolder, Object item) {
        DisplayItem displayItem = (DisplayItem) item;
        final VH vh = (VH) viewHolder;
        if (vh.iv != null) {
            RequestOptions myOptions = new RequestOptions()
                    .override(vh.iv.getWidth(), vh.iv.getHeight())
                    .error(R.drawable.ic_launcher);

            Glide.with(viewHolder.view.getContext())
                    .applyDefaultRequestOptions(myOptions)
                    .load(displayItem.src)
                    .into(vh.iv);
            vh.iv.setVisibility(View.VISIBLE);
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
        if (vh.tv != null) {
            vh.tv.setTag(R.id.view_image_url, null);
        }
    }

    public class OnFocusChangeListener implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                //触发父View重绘，以便调用到TwoWayView.getChildDrawingOrder，让获取焦点view位于最上层
                view.getParent().requestLayout();
            }
        }
    }
}
