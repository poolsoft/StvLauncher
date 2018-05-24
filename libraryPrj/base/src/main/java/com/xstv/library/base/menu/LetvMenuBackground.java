
package com.xstv.library.base.menu;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import com.xstv.library.base.R;

public class LetvMenuBackground extends Dialog {

    private LetvMenu mMenuMain;
    private ImageView mIcon = null;
    private ImageView mFocus = null;

    public LetvMenuBackground(Context context, LetvMenu main) {
        super(context, R.style.Theme_Dialog_NoFrame);
        this.mMenuMain = main;
        getWindow().setGravity(Gravity.RIGHT);
        getWindow().setWindowAnimations(R.style.DialogMenuBg);
        setContentView(R.layout.letv_menu_bg);
        initView();
    }

    private void initView() {
        mIcon = (ImageView) findViewById(R.id.letv_menu_bg_icon);
        mFocus = (ImageView) findViewById(R.id.letv_menu_bg_focus);
        mFocus.setVisibility(View.INVISIBLE);
    }

    @Override
    public void show() {
        super.show();
        mMenuMain.show();
        mFocus.setVisibility(View.VISIBLE);
    }

    public void setBgIcon(int visibility) {
        if (mIcon != null)
            mIcon.setVisibility(visibility);
    }

}
