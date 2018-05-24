
package com.xstv.desktop.app.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xstv.desktop.app.AppPluginActivator;
import com.xstv.desktop.app.R;
import com.xstv.desktop.app.adapter.BaseSpaceAdapter;
import com.xstv.desktop.app.interfaces.DataChangeObserver;
import com.xstv.desktop.app.interfaces.IAppFragment;
import com.xstv.desktop.app.interfaces.IAppMenu;
import com.xstv.desktop.app.util.Utilities;

import java.lang.ref.WeakReference;

public abstract class BaseWorkspace<T> extends FrameLayout implements DataChangeObserver {

    private static final String TAG = BaseWorkspace.class.getSimpleName();

    public enum State {
        STATE_NORMAL, STATE_DELETE, STATE_NEW_FOLDER, STATE_MOVE, STATE_FOLDER_OPENED, STATE_FOLDER_CLOSED, STATE_ADD
    }

    protected Context context;

    protected IAppMenu appMenu;

    protected WeakReference<IAppFragment> fragmentRef;

    private View mLoadingView;

    protected Handler mainHandler = new Handler();

    protected boolean isVisibleToUser;

    public BaseWorkspace(Context context) {
        this(context, null);
    }

    public BaseWorkspace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseWorkspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;

        initView(context);
        initData();
        initEvent();
    }

    public abstract void initView(Context context);

    public void initData() {

        appMenu = new AppMenuImpl(context);

        RecyclerView recyclerView = getRecyclerView();
        recyclerView.setLayoutManager(getLayoutManager());
        recyclerView.setAdapter(getAdapter());
    }

    public void initEvent() {
    }

    public abstract BaseSpaceAdapter getAdapter();

    public abstract RecyclerView.LayoutManager getLayoutManager();

    public abstract BaseRecyclerView getRecyclerView();

    public void setAppFragment(IAppFragment iAppFragment) {
        if(fragmentRef == null){
            this.fragmentRef = new WeakReference<IAppFragment>(iAppFragment);
        }
    }

    /**
     * 切换到当前桌面
     * 
     * @param isVisibleToUser
     */
    public void onUserVisibleHint(final boolean isVisibleToUser) {
        this.isVisibleToUser = isVisibleToUser;
        if(getRecyclerView() != null){
            getRecyclerView().setUserVisible(isVisibleToUser);
        }
    }

    public void show(){
        setVisibility(View.VISIBLE);
    }

    public void show(T itemInfoList) {
        if(itemInfoList == null){
            Log.w(TAG, "show itemInfoList is null.");
            return;
        }
        getRecyclerView().setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        if(!isVisible()){
            show();
        }
        //先让recyclerView拿到焦点，防止没有view获取焦点，焦点会回到tab上。
        getRecyclerView().requestFocus();

        setData(itemInfoList, false);
    }

    public abstract void setData(T list, boolean isUpdate);

    public void appendData(T list){

    }

    public boolean isMainThread(){
        return Looper.getMainLooper() == Looper.myLooper();
    }

    public boolean isVisible(){
        return getVisibility() == View.VISIBLE;
    }

    public void hide() {
        setVisibility(View.GONE);
        if(appMenu != null && appMenu.isMenuShowing()){
            appMenu.hideMenu();
        }
    }

    public void showLoading() {
        if (mLoadingView == null) {
            Context context = AppPluginActivator.getContext();
            mLoadingView = View.inflate(context, R.layout.workspace_loading, null);
            TextView loadingTv = (TextView) mLoadingView.findViewById(R.id.loading_text);
            String loadingText = getLoadingText();
            if(!TextUtils.isEmpty(loadingText)){
                loadingTv.setText(loadingText);
            }
            mLoadingView.setFocusable(false);
            mLoadingView.setFocusableInTouchMode(false);
        }
        if (indexOfChild(mLoadingView) == -1) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mLoadingView.getLayoutParams();
            if (params == null) {
                params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER;
            }
            addView(mLoadingView, params);
        }
    }

    public String getLoadingText(){
        return null;
    }

    public void hideLoading() {
        if (mLoadingView != null && mLoadingView.getVisibility() == View.VISIBLE &&
                (indexOfChild(mLoadingView) != -1)) {
            mLoadingView.setVisibility(View.INVISIBLE);
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    removeView(mLoadingView);
                    mLoadingView = null;
                }
            });
        }
    }

    public boolean requestDefaultViewFocus() {
        return false;
    }

    /**
     * Call when home key event.
     *
     * @return if true , key event is consumed and not back to home.
     */
    public boolean backToHome() {
        return false;
    }

    public void resetScroll() {
        if(getRecyclerView() == null){
            return;
        }
        getRecyclerView().resetScroll();
    }

    // 处理体感
    public void scrollUpByHandDetect() {
        if(getRecyclerView() == null){
            return;
        }
        getRecyclerView().scrollUpByHandDetect();
    }

    public void scrollDownByHandDetect() {
        if(getRecyclerView() == null){
            return;
        }
        getRecyclerView().scrollDownByHandDetect();
    }

    /**
     * @return -1 是默认值，无效
     */
    public int getPageStatus() {
        return -1;
    }

    public void manageApp() {
        boolean isOpen = Utilities.openManagApp();
        if (isOpen) {
            if (fragmentRef != null && fragmentRef.get() != null) {
                fragmentRef.get().startAppAnim();
            }
        }
    }

    public void feedBack() {
        boolean isOpen = Utilities.openFeedback();
        if (isOpen) {
            if (fragmentRef != null && fragmentRef.get() != null) {
                fragmentRef.get().startAppAnim();
            }
        }
    }

    public void onRelease() {
        mLoadingView = null;
        mainHandler.removeCallbacksAndMessages(null);
    }

    public void onCrush() {

    }

    public void clearMemory() {
        if(getLayoutManager() != null){
            int childCount = getLayoutManager().getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = getLayoutManager().getChildAt(i);
                if (childView instanceof CellView) {
                    CellView cellView = (CellView) childView;
                    if(cellView instanceof AppFolderCellView){
                        cellView.recycle(true);
                    }else{
                        cellView.recycle(false);
                    }
                }
            }
        }
    }
}
