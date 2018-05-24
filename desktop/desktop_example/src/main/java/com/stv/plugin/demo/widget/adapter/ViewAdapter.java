package com.stv.plugin.demo.widget.adapter;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stv.plugin.demo.DemoApplication;
import com.stv.plugin.demo.data.common.Poster;
import com.stv.plugin.demo.widget.ItemView;
import com.xstv.library.base.Logger;
import com.xstv.desktop.R;

import java.util.ArrayList;

public class ViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private static class NormalViewHolder extends RecyclerView.ViewHolder {

        TextView posterTv;
        ImageView posterIv;

        NormalViewHolder(View itemView) {
            super(itemView);
            posterIv = (ImageView) itemView.findViewById(R.id.image);
            posterTv = (TextView) itemView.findViewById(R.id.tv);
        }
    }

    private static class AttachParams {
        int defaultViewW = 474;
        int defaultViewH = 225;
        boolean fadeEnable = true;

        AttachParams() {
            if (Build.VERSION.SDK_INT < 21) {
                defaultViewW = defaultViewW * 2 / 3;
                defaultViewH = defaultViewH * 2 / 3;
                fadeEnable = false;
            }
        }
    }

    private Logger mLogger = Logger.getLogger(DemoApplication.PLUGINTAG, "ViewAdapter");
    private AttachParams mAttachParams = new AttachParams();
    private OnItemClickListener mOnItemClickListener;
    private ArrayList<Poster> mData = new ArrayList<Poster>();

    public void setOnItemClickListener(OnItemClickListener l) {
        mOnItemClickListener = l;
    }

    public void bindData(ArrayList<Poster> data) {
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    private int getRealPosition(RecyclerView.ViewHolder holder) {
        return holder.getPosition();
    }

    private void onBindDataToView(RecyclerView.ViewHolder viewHolder, Poster poster) {
        if (viewHolder instanceof NormalViewHolder) {
            NormalViewHolder myViewHolder = (NormalViewHolder) viewHolder;
            myViewHolder.posterTv.setText(poster.abs);

            RequestOptions myOptions = new RequestOptions()
                    .override(mAttachParams.defaultViewW, mAttachParams.defaultViewH)
                    .error(R.drawable.ic_placeholder)
                    .placeholder(R.drawable.ic_placeholder);

            if (!TextUtils.isEmpty(poster.placeholder)) {

            } else {
            }
            Glide.with(myViewHolder.posterIv.getContext())
                    .applyDefaultRequestOptions(myOptions)
                    .load(poster.image_url)
                    .into(myViewHolder.posterIv);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new NormalViewHolder(createNormalItemView(viewGroup));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final int pos = getRealPosition(viewHolder);
        onBindDataToView(viewHolder, mData.get(pos));
        if (mOnItemClickListener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, pos);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private ItemView createNormalItemView(ViewGroup viewGroup) {
        return (ItemView) LayoutInflater.from(DemoApplication.sWidgetApplicationContext).inflate( //
                R.layout.demo_fragment_view_item, viewGroup, false);
    }
}
