
package com.xstv.desktop.app.holder;

import com.xstv.desktop.app.bean.ContentBean;
import com.xstv.desktop.app.widget.BaseContent;

public class ContentHolder extends BaseHolder<ContentBean, BaseContent> {
    private static final String TAG = ContentHolder.class.getSimpleName();
    public ContentHolder(BaseContent itemView) {
        super(itemView);
    }

    @Override
    public void bindData(ContentBean contentBean, int pos) {
        super.bindData(contentBean, pos);
        mItemVeiw.bindData(contentBean);
    }
}
