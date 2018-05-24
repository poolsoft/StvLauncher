
package com.xstv.library.base.menu;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.xstv.library.base.R;

import java.util.ArrayList;
import java.util.List;

// import com.android.internal.R;
// EUI START liuteng1 add for letvmenu at 2016-04-11
// EUI END

public class LetvMenu extends AutoCloseDialog implements View.OnFocusChangeListener,
        OnClickListener {
    private String TAG = "LetvMenu";
    protected ArrayList<LetvMenuItem> mLinear = new ArrayList<LetvMenuItem>();
    private int mKey = 0;
    private int mSelectPosition = 0;
    private ItemClickListener mListener = null;

    protected boolean mCancelForMenuKey = false;
    private int mItemHeight = 0;

    private boolean mFirstInit = true;
    // EUI START liuteng1 add for letvmenu at 2016-04-11
    private boolean aFinish = true;
    private int animaIndex = 0;
    private boolean isUp = true;
    private int aSelect = 0;
    private ArrayList<ItemInfo> mInfos = new ArrayList<ItemInfo>();
    private float mScale = (float) 0.03125;
    private float mAlpha = (float) 0.03125;
    protected ArrayList<LetvMenuItem> mList = new ArrayList<LetvMenuItem>();
    private AnimaThread mAnimaThread;
    private float mYLargeFloat = (float) 0.09375; //EUI: 1.5/16;
    private float mYNormalFloat = (float) 0.0628; //EUI: 1.0/16;
    // EUI END
    private Runnable mRefresh = new Runnable() {
        public void run() {
            initView();
        }
    };

    // EUI START liuteng1 add for letvmenu at 2016-04-11
    private Handler aHandler = new Handler() {
        public void handleMessage(Message msg) {
            int num = msg.what;
            if (num < 16) {
                for (int i = 0; i < mInfos.size(); i++) {
                    ItemInfo info = mInfos.get(i);
                    info.itemRefresh();
                    LetvMenuItem item = mList.get(i);
                    item.setY(info.mYPlace);
                    item.setScaleX(info.mXStartScale);
                    item.setScaleY(info.mYStartScale);
                    item.setAlpha(info.mStartAlpha);
                }
                if (num == 15) {
                    //int lastpos = 0;
                    if (isUp && (aSelect != 0)) {
                        //lastpos = aSelect;
                        aSelect--;
                    }
                    if (!isUp && (aSelect != (mInfos.size() - 1))) {
                        //lastpos = aSelect;
                        aSelect++;
                    }
                    recalculateY(aSelect);
                    aFinish = true;
                }
            }
        }

        ;
    };

    // EUI END
    public LetvMenu(Context context) {
        this(context, R.style.DialogMenu);
    }

    public LetvMenu(Context context, int theme) {
        super(context, theme);
        this.mItemHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.letv_menu_item_height);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setGravity(Gravity.RIGHT);

        // EUI START liuteng1 modify for letvmenu at 2016-04-11
        /*
        if (mHandler != null)
            mHandler.postDelayed(mRefresh, 100);
        */
        // EUI END
    }

    /**
     * init dialog layout
     *
     * @param layoutId dialog layout
     * @param itemIds  LetvMenuItem ids
     */
    public void initLayout(int layoutId, int[] itemIds) {
        setContentView(layoutId);
        for (int i = 0; i < itemIds.length; i++) {
            LetvMenuItem layout = (LetvMenuItem) findViewById(itemIds[i]);
            // EUI START liuteng1 modify for letvmenu at 2016-04-11
            /*
            layout.setOnFocusChangeListener(this);
            layout.setOnClickListener(this);
            */
            layout.setTag(i);
            /*
            if (i == mSelectPosition) {
                layout.setTranslationY(mContext.getResources().getDimension(
                        eui.tv.internal.R.dimen.letv_menu_item_select_translation_y));
            } else {
                layout.setTranslationY(mContext.getResources().getDimension(
                        eui.tv.internal.R.dimen.letv_menu_item_translation_y));
            }
            */
            // EUI END
            mLinear.add(layout);
        }
    }

    public void setItemHeight(int itemHeight) {
        this.mItemHeight = itemHeight;
    }

    /**
     * build dialog from dynamic data
     *
     * @param items
     */
    public void initLayout(List<LetvMenuItem> items) {
        if (items == null) {
            Log.e(TAG, "items is null");
            return;
        }
        setContentView(R.layout.letv_menu);
        LinearLayout root = (LinearLayout) findViewById(R.id.letv_menu_root);
        LetvMenuItem item = null;
        for (int i = 0; i < items.size(); i++) {
            item = items.get(i);
            // EUI START liuteng1 modify for letvmenu at 2016-04-11
            ViewGroup mGroup = (ViewGroup) item.getParent();
            if (mGroup != null) {
                mGroup.removeView(item);
            }
            root.addView(item, WindowManager.LayoutParams.MATCH_PARENT, mItemHeight);
            item.setId(i);
            /*
            item.setOnFocusChangeListener(this);
            item.setOnClickListener(this);
            */
            // EUI END
            item.setTag(i);
            mLinear.add(item);
        }
    }

    /**
     * process some items hide
     */
    public void checkItems() {
        // EUI START liuteng1 modify for letvmenu at 2016-04-11
        ArrayList<LetvMenuItem> items = new ArrayList<LetvMenuItem>();
        int i = 0;
        for (LetvMenuItem item : mLinear) {
            if (item.getVisibility() == View.VISIBLE) {
                item.setTag(i);
                items.add(item);
                i++;
            }
        }
        mLinear.clear();
        mLinear.addAll(items);
        updateLetvMenu();
        // EUI END
    }

    @Override
    public void show() {
        super.show();
        mCancelForMenuKey = false;
        // EUI START liuteng1 add for letvmenu at 2016-04-11
        updateLetvMenu();
        // EUI END
    }

    @Override
    public void cancel() {
        super.cancel();
        try {
            if (mHandler != null) {
                mHandler.removeCallbacks(mRefresh);
            }
        } catch (IllegalArgumentException e) {
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                cancel();
                return true;
            case KeyEvent.KEYCODE_MENU:
                if (isShowing()) {
                    mCancelForMenuKey = true;
                    cancel();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                // EUI START liuteng1 add for letvmenu at 2016-04-11
                if (aSelect != 0 && aFinish) {
                    aFinish = false;
                    isUp = true;
                    startMenuAnima();
                }
                // EUI END
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                // EUI START liuteng1 add for letvmenu at 2016-04-11
                int last = mList.size() - 1;
                if (aSelect != last && aFinish) {
                    aFinish = false;
                    isUp = false;
                    startMenuAnima();
                }
                // EUI END
                mKey = keyCode;
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                cancel();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                View focus = getCurrentFocus();
                if (focus != null)
                    onClick(focus);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // EUI START liuteng1 add for letvmenu at 2016-04-11
        /*
        if (hasFocus) {
            //Log.d(TAG, " onFocusChange v.getId() = " + v.getId());
            int index = 0;
            for (index = 0; index < mLinear.size(); index++) {
                if (v == mLinear.get(index))
                    break;
            }
            if (index >= mLinear.size()) {
                Log.d(TAG, " unfind v" + v);
                return;
            }
            if (mKey == 0) {

            } else {
                for (int i = 0; i < index; i++) {
                    if (mKey == KeyEvent.KEYCODE_DPAD_DOWN && (i == index - 1))
                        continue;
                    mLinear.get(i).move(LetvMenuItem.FOCUS_POS - index);
                }
                for (int i = mLinear.size() - 1; i > index; i--) {
                    if (mKey == KeyEvent.KEYCODE_DPAD_UP && (i == index + 1))
                        continue;
                    mLinear.get(i).move(4 - index);
                }
            }
        }
        */
        // EUI END
    }

    @Override
    public void onClick(View view) {
        // EUI START liuteng1 add for letvmenu at 2016-04-12
        mSelectPosition = aSelect;
        if (mListener != null) {
            mListener.onItemClick(view);
        }
        // EUI END
    }

    /**
     * for radio button item
     *
     * @param oldid
     * @param newid
     */
    public void setSelect(int oldid, int newid) {
        if (oldid >= 0 && oldid < mLinear.size())
            mLinear.get(oldid).setSelect(false);
        if (newid >= 0 && newid < mLinear.size())
            mLinear.get(newid).setSelect(true);
    }

    public int getSelectPosition() {
        return mSelectPosition;
    }

    public View getItemView(int position) {
        return mLinear.get(position);
    }

    public void setItemClickListener(ItemClickListener l) {
        mListener = l;
    }

    public interface ItemClickListener {
        void onItemClick(View item);
    }

    private void initView() {
        // EUI START liuteng1 add for letvmenu at 2016-04-11
        /*
        //Log.d(TAG, " initView mSelectItem= " + mSelectPosition);
        for (int i = 0; i < mSelectPosition; i++) {
            mLinear.get(i).setFocusable(true);
            mLinear.get(i).setPos(i + LetvMenuItem.FOCUS_POS - mSelectPosition);
            //Log.d(TAG, " onFocusChange index = " + mSelectPosition + " i = " + i);
        }
        for (int i = mLinear.size() - 1; i > mSelectPosition; i--) {
            mLinear.get(i).setPos(i - mSelectPosition + 4);
        }
        if(mFirstInit){
            mLinear.get(mSelectPosition).setPos(LetvMenuItem.FOCUS_POS + 0.5f);
            mFirstInit = false;
        }else {
            mLinear.get(mSelectPosition).refreshCurrent();
        }
        */
        // EUI END
    }

    /**
     * all menu dialog exit
     *
     * @return
     */
    public boolean isAllCancel() {
        return mCancelForMenuKey || mCancelForTimeout || mCancelForCloseSystemDialog;
    }

    public void moveTo(int position) {
        if (position < 0 || position >= mLinear.size()) {
            Log.e(TAG, "position error");
        }
        //mSelectPosition = position;
        // EUI START liuteng1 add for letvmenu at 2016-04-11
        /*
        if (mHandler != null)
            mHandler.post(mRefresh);
        */
        // EUI END
    }

    // EUI START liuteng1 add for letvmenu at 2016-04-11
    /*
    private void initLetvMenu(int select){
        getVisibleList();
        float mYPos ;
        LetvMenuItem item = null;
        for (int i = 0; i < mList.size(); i++) {
            item = mList.get(i);
            item.setOnClickListener(this);
            item.setFocusable(true);
            item.setFocusableInTouchMode(true);
            int diff = i - select;
            if( diff == 1){
                mYPos = (float)((4+diff)*mItemHeight);
            }else if(diff == -1){
                mYPos = (float)((3+diff)*mItemHeight);
            }else if(diff > 1){
                mYPos = (float)((4+diff)*mItemHeight);
            }else if(diff < -1){
                mYPos = (float)((3+diff)*mItemHeight);
            }else{
                mYPos = (float)(3.5*mItemHeight);
            }
            if(item.getHeight() == 0){
                if (i == select) {
                    item.setTranslationY(mContext.getResources().getDimension(eui.tv.internal.R.dimen.letv_menu_item_select_translation_y));
                } else {
                    item.setTranslationY(mContext.getResources().getDimension(eui.tv.internal.R.dimen.letv_menu_item_translation_y));
                }

            }else{
                item.setY(mYPos);
            }
        }
    }
    */

    public void updateLetvMenu() {
        getVisibleList();
        float mYPos;
        LetvMenuItem item = null;
        for (int i = 0; i < mList.size(); i++) {
            item = mList.get(i);
            item.setOnClickListener(this);
            item.setFocusable(true);
            item.setFocusableInTouchMode(true);
            int diff = i - aSelect;
            if (diff == 1) {
                mYPos = (float) ((4 + diff) * mItemHeight);
            } else if (diff == -1) {
                mYPos = (float) ((3 + diff) * mItemHeight);
            } else if (diff > 1) {
                mYPos = (float) ((4 + diff) * mItemHeight);
            } else if (diff < -1) {
                mYPos = (float) ((3 + diff) * mItemHeight);
            } else {
                mYPos = (float) (3.5 * mItemHeight);
            }
            if (item.getHeight() == 0) {
                item.setTranslationY(mYPos - i * mItemHeight);
            } else {
                item.setY(mYPos);
            }
            if (diff == 0) {
                item.setLargeAnimationEnd();
                item.requestFocus();
                item.setSelected(true);
            } else {
                item.setNormalAnimationEnd();
                item.setSelected(false);
            }
        }
    }

    private void recalculateY(int mSelect) {
        float mYPos;
        LetvMenuItem mitem;
        for (int i = 0; i < mList.size(); i++) {
            mitem = mList.get(i);
            int diff = i - mSelect;
            if (diff == 1) {
                mYPos = (float) ((4 + diff) * mItemHeight);
            } else if (diff == -1) {
                mYPos = (float) ((3 + diff) * mItemHeight);
            } else if (diff > 1) {
                mYPos = (float) ((4 + diff) * mItemHeight);
            } else if (diff < -1) {
                mYPos = (float) ((3 + diff) * mItemHeight);
            } else {
                mYPos = (float) (3.5 * mItemHeight);
            }
            mitem.setY(mYPos);
            if (diff == 0) {
                mitem.setLargeAnimationEnd();
                mitem.requestFocus();
                mitem.setSelected(true);
            } else {
                mitem.setNormalAnimationEnd();
                mitem.setSelected(false);
            }
        }
    }

    private void getVisibleList() {
        mList.clear();
        LetvMenuItem mitem;
        for (int i = 0; i < mLinear.size(); i++) {
            mitem = mLinear.get(i);
            if (mitem.getVisibility() == View.VISIBLE) {
                mList.add(mitem);
            }
        }
    }

    private void startMenuAnima() {
        mInfos.clear();
        LetvMenuItem item;
        for (int i = 0; i < mList.size(); i++) {
            item = mList.get(i);
            ItemInfo info = new ItemInfo();
            info.height = item.getHeight();
            info.mYPlace = item.getY();
            info.mXStartScale = item.getScaleX();
            info.mYStartScale = item.getScaleY();
            info.mStartAlpha = item.getAlpha();
            if (isUp) {
                if (i == (aSelect - 1)) {
                    info.mYDistance = (float) (info.height * mYLargeFloat);
                    info.mScale = mScale;
                    info.mAlpha = mAlpha;
                } else if (i == aSelect) {
                    info.mYDistance = (float) (info.height * mYLargeFloat);
                    info.mScale = -mScale;
                    info.mAlpha = -mAlpha;
                } else {
                    info.mYDistance = (float) (info.height * mYNormalFloat);
                }
            } else {
                if (i == aSelect) {
                    info.mYDistance = (float) (info.height * -mYLargeFloat);
                    info.mScale = -mScale;
                    info.mAlpha = -mAlpha;
                } else if (i == (aSelect + 1)) {
                    info.mYDistance = (float) (info.height * -mYLargeFloat);
                    info.mScale = mScale;
                    info.mAlpha = mAlpha;
                } else {
                    info.mYDistance = (float) (info.height * -mYNormalFloat);
                }
            }
            mInfos.add(info);
        }
        animaIndex = 0;
        mAnimaThread = new AnimaThread();
        mAnimaThread.start();
    }

    class ItemInfo {
        public int height;
        public float mXStart;
        public float mYPlace;
        public float mYDistance;
        public float mScale = (float) 0.0;
        public float mAlpha = (float) 0.0;
        public float mStartAlpha = (float) 0.0;
        public float mXStartScale = (float) 1.0;
        public float mYStartScale = (float) 1.0;

        public void itemRefresh() {
            mYPlace += mYDistance;
            mXStartScale += mScale;
            mYStartScale += mScale;
            mStartAlpha += mAlpha;
        }
    }

    class AnimaThread extends Thread {
        public void run() {
            while (animaIndex < 16) {
                try {
                    Thread.sleep(18);
                    Message message = new Message();
                    message.what = animaIndex++;
                    aHandler.sendMessage(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // EUI END

}
