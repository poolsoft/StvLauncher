
package com.xstv.desktop.app.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.xstv.desktop.app.R;


/**
 * Created by zhangguanhua on 18-2-5.
 */

public class MainContent extends BaseContent {

    public MainContent(Context context) {
        super(context);
    }

    public MainContent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainContent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void initView(Context context) {
        setOrientation(LinearLayout.HORIZONTAL);
        View.inflate(context, R.layout.main_content_layout, this);
    }
}
