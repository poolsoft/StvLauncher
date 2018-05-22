
package com.xstv.desktop.app.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.xstv.desktop.app.R;


public class VipContent extends BaseContent {
    private static final String TAG = VipContent.class.getSimpleName();

    public VipContent(Context context) {
        super(context);
    }

    public VipContent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VipContent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void initView(Context context) {
        setOrientation(LinearLayout.HORIZONTAL);
        View.inflate(context, R.layout.vip_content_layout, this);
    }
}
