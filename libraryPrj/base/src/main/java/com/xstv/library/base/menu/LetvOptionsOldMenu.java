
package com.xstv.library.base.menu;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LetvOptionsOldMenu extends LetvMenu implements ILetvOptionsMenu {
    private static final int ROOT_VIEW_WIDTH = 340;
    private static final int ROOT_VIEW_HEIGHT = 720;
    private static final int ITEM_MARGIN_LEFT = 70;
    private static final int ITEM_VIEW_HEIGHT = 74;

    private Context mContext;
    private LetvMenuBackground mBackground;
    private LinearLayout mRootView;
    private LetvOptionsMenu.OnItemClickListener mItemClickListener;

    public LetvOptionsOldMenu(Context hostContext) {
        super(hostContext);
        init(hostContext);
    }

    public LetvOptionsOldMenu(Context hostContext, int theme) {
        super(hostContext, theme);
        init(hostContext);
    }

    private void init(Context hostContext) {
        this.mContext = hostContext;
        this.mBackground = new LetvMenuBackground(mContext, this);
        mRootView = new LinearLayout(mContext);
        int width = dip2px(mContext, ROOT_VIEW_WIDTH);
        int height = dip2px(mContext, ROOT_VIEW_HEIGHT);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        mRootView.setOrientation(LinearLayout.VERTICAL);
        setContentView(mRootView, params);
    }

    /**
     * add menu item name
     *
     * @param names menu item name
     */
    @Override
    public void addItems(CharSequence... names) {
        if (names != null && names.length > 0) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dip2px(mContext, ITEM_VIEW_HEIGHT));
            params.leftMargin = dip2px(mContext, ITEM_MARGIN_LEFT);
            for (int i = 0; i < names.length; i++) {
                LetvMenuItem menuItem = new LetvMenuItem(mContext);
                menuItem.setLayoutParams(params);
                menuItem.setTag(i);
                mLinear.add(menuItem);
                TextView nameView = menuItem.getNameView();
                if (nameView != null) {
                    nameView.setText(names[i]);
                }
                mRootView.addView(menuItem);
            }
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        Integer position = (Integer) view.getTag();
        if (mItemClickListener != null) {
            mItemClickListener.onItemClick(view, position);
        } else {
            onItemClick(position);
        }
    }

    @Override
    public void showMenu() {
        if (mBackground != null) {
            mBackground.show();
        }
    }

    @Override
    public void hideMenu() {
        cancel();
    }

    @Override
    public void hideMenu(boolean isImmediately) {
        cancel();
    }

    /**
     * cancel a showing menu
     */
    @Override
    public void cancel() {
        super.cancel();
        if (mBackground != null) {
            mBackground.cancel();
        }
    }

    @Override
    public boolean isMenuShowing() {
        return super.isShowing();
    }

    /**
     * set item click listener
     * @param itemClickListener
     */
    public void setOnItemClickListener(LetvOptionsMenu.OnItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    @Override
    public void onItemClick(int positon) {

    }

    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
