
package com.xstv.desktop.app.widget;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.RectF;
import android.provider.Settings;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.xstv.base.LetvLog;
import com.xstv.desktop.app.AppPluginActivator;
import com.xstv.desktop.app.R;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.interfaces.IAppFragment;
import com.xstv.desktop.app.model.AppDataModel;
import com.xstv.desktop.app.util.LauncherState;
import com.xstv.desktop.app.util.Utilities;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;

public abstract class BaseCellView<T extends ItemInfo> extends RelativeLayout implements View.OnFocusChangeListener, View.OnClickListener {
    private static final String TAG = BaseCellView.class.getSimpleName();

    private static final String GOTO_STORE_DETAIL_ACTION = "com.letv.tvos.appstore.external.new";
    private static final String PRIME_SENSE = "prime_sense";

    private static final float PAINT_STROKE_WIDTH = 4;

    private static final float SCALE_OUT = 1.2f;
    private static final float SCALE_IN = 1.0f;

    public static final float SHADOWZ_OUT = 30.0f;
    public static final float SHADOWZ_NORMAL = 8.0f;

    protected BaseWorkspace.State mState = BaseWorkspace.State.STATE_NORMAL;

    protected int mDuration;
    protected int mAppCorners;
    protected int mCellViewWith;
    protected int mCellViewHeight;

    protected Context mContext;

    private boolean mIsAttached;
    protected boolean mHasFocus;

    protected RectF mInnerRect = new RectF();
    protected Paint mPaint;
    // anti alias when scale view
    private static DrawFilter mDrawFilter;
    private float mScaleOut = SCALE_OUT;

    protected T mItemInfo;

    // for download loading
    protected boolean isShowLoading;
    private float mLastSweepAngle;
    public Dialog mCancleDialog;

    //notify framgent
    protected WeakReference<IAppFragment> fragmentRef;

    public BaseCellView(Context context) {
        this(context, null);
    }

    public BaseCellView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseCellView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
        initData(context, attrs, defStyle);
    }

    public abstract void initView(Context context);

    public void initData(Context context, AttributeSet attrs, int defStyle) {
        mContext = context;

        int width = getResources().getDimensionPixelSize(R.dimen.poster_workspace_item_width1);
        int height = getResources().getDimensionPixelSize(R.dimen.poster_workspace_item_height2);

        //TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CellView, defStyle, 0);
        mCellViewWith = width;//(int) a.getDimension(R.styleable.CellView_cellWidth, width);
        mCellViewHeight = height;//(int) a.getDimension(R.styleable.CellView_cellHeight, height);
        //a.recycle();

        mDrawFilter = new PaintFlagsDrawFilter(Paint.DITHER_FLAG, Paint.FILTER_BITMAP_FLAG);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(PAINT_STROKE_WIDTH);

        mDuration = getResources().getInteger(R.integer.app_scale_duration);
        mAppCorners = getResources().getDimensionPixelSize(R.dimen.app_corners);

        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    public void bindData(T t) {
        this.mItemInfo = t;
        if (t != null) {
            setLabel(t);
        }
    }

    public void setLabel(T t) {

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setFocusable(true);
        setFocusableInTouchMode(false);
        setClipToPadding(false);
        setClipChildren(false);
        setOnFocusChangeListener(this);
        setOnClickListener(this);
        mIsAttached = true;
        setState();
    }

    /**
     * 设置cellView的状态
     */
    public void setState() {
        //LetvLog.d(TAG, " setState state = " + mState + " AppWorkspace.mState = " + AppWorkspace.mState);
        mState = AppWorkspace.mState;
        if (mState == BaseWorkspace.State.STATE_DELETE) {
            setDeleteState(hasFocus());
        } else if (mState == BaseWorkspace.State.STATE_NEW_FOLDER) {
            setNewFolderState(false);
        } else if (mState == BaseWorkspace.State.STATE_ADD) {
            setAddState();
        } else if (mState == BaseWorkspace.State.STATE_MOVE) {
            if (hasFocus()) {
                setMoveState(true, true);
            } else {
                setMoveState(false, false);
            }
        } else {
            resetState();
        }
    }


    public void setDeleteState(boolean isFocus) {
        mState = BaseWorkspace.State.STATE_DELETE;
    }

    public void resetDeleteState() {
        mState = BaseWorkspace.State.STATE_DELETE;
    }

    public void setNewFolderState(boolean isFocus) {
        mState = BaseWorkspace.State.STATE_NEW_FOLDER;
    }

    public void resetState() {
        mState = BaseWorkspace.State.STATE_NORMAL;
    }

    public void setAddState() {
        mState = BaseWorkspace.State.STATE_ADD;
    }

    public void setMoveState(boolean isFocus, boolean canMove) {
        mState = BaseWorkspace.State.STATE_MOVE;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsAttached = false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        this.mHasFocus = hasFocus;
        if (hasFocus) {
            if (Utilities.isChangeDrawOrder()) {
                final ViewParent parent = this.getParent();
                if (parent instanceof ViewGroup) {
                    ((ViewGroup) parent).invalidate();
                }
            } else {
                Utilities.setShadowZ(this, SHADOWZ_OUT);
                if (getParent() instanceof BaseContent) {
                    Utilities.setShadowZ((BaseContent) getParent(), SHADOWZ_OUT);
                }
            }
            if (null != this) {
                zoomOut();
            }
            recordFocusPos();
        } else {
            if (!Utilities.isChangeDrawOrder()) {
                Utilities.setShadowZ(this, SHADOWZ_NORMAL);
                if (getParent() instanceof BaseContent) {
                    Utilities.setShadowZ((BaseContent) getParent(), SHADOWZ_NORMAL);
                }
            }
            if (null != this) {
                zoomIn();
            }
        }
    }

    void zoomOut() {
        computerScaleOut();
        ViewCompat.animate(this).scaleX(mScaleOut)
                .scaleY(mScaleOut).setDuration(mDuration)
                /*.setInterpolator(new DecelerateInterpolator())*/.start();
    }

    void zoomIn() {
        ViewCompat.animate(this).scaleX(SCALE_IN)
                .scaleY(SCALE_IN).setDuration(mDuration)
                /*.setInterpolator(new DecelerateInterpolator())*/.start();
    }

    private void computerScaleOut() {
        ViewGroup parent = (ViewGroup) getParent();
        if (parent instanceof MainContent) {
            mScaleOut = 1.25F;
        } else {
            mScaleOut = 1.20F;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (canvas.getDrawFilter() != mDrawFilter) {
            canvas.setDrawFilter(mDrawFilter);
        }
        super.dispatchDraw(canvas);

        drawProgress(canvas);

        if (isFocused()) {
            mInnerRect.left = 2;
            mInnerRect.top = 2;
            mInnerRect.right = getWidth() - 2;
            mInnerRect.bottom = getHeight() - 2;
            canvas.drawRoundRect(mInnerRect, 8, 8, mPaint);
        }
    }

    private void drawProgress(Canvas canvas) {
        /*if (mItemInfo == null) {
            return;
        }

        DownloadStatusBean downloadStatusBean = mItemInfo.getDownloadStatusBean();
        if(downloadStatusBean == null){
            return;
        }
        String downloadStatus = downloadStatusBean.getDownloadStatus();
        float sweepAngle = downloadStatusBean.getSweepAngle();
        //LetvLog.d(TAG, "drawProgress downstate = " + downloadStatus + " title = " + mItemInfo.getTitle() + " sweepangle = " + sweepAngle);
        if(TextUtils.isEmpty(downloadStatus) || (DownloadAppPresenter.STATE_INSTALLED.equals(downloadStatus) && sweepAngle == 0)){
            downloadStatusBean.setDownloadStatus(null);
            isShowLoading = false;
            mLastSweepAngle = 0;
            return;
        }

        if (downloadStatus == DownloadAppPresenter.STATE_INSTALLED) {
            // 说明是安装完成
            isShowLoading = false;
            mLastSweepAngle = 0;
            downloadStatusBean.setSweepAngle(0);
            downloadStatusBean.setDownloadStatus(null);
            DownloadAppPresenter.getInstance().removePreloadByPkg(mItemInfo.getPackageName());
            onLoadingFinished();
            onInstallFinished(mHasFocus);
            return;
        }

        if (downloadStatus == DownloadAppPresenter.STATE_RESET) {
            // 下载/安装失败(重置状态)
            isShowLoading = false;
            mLastSweepAngle = 0;
            onLoadingFinished();
            downloadStatusBean.setDownloadStatus(null);
            downloadStatusBean.setSweepAngle(0);
            DownloadAppPresenter.getInstance().removePreloadByPkg(mItemInfo.getPackageName());
            return;
        }

        if (downloadStatus == DownloadAppPresenter.STATE_INSTALLING) {
            // 下载成功
            mLastSweepAngle = 0;
            LetvLog.d(TAG, "drawProgress download success = " + sweepAngle);
            if (mCancleDialog != null && mCancleDialog.isShowing()) {
                mCancleDialog.dismiss();
                Toast.makeText(mContext, AppPluginActivator.getContext().getResources().getString(
                        R.string.please_wait_install), Toast.LENGTH_SHORT).show();
            }
        }

        if(downloadStatus == DownloadAppPresenter.STATE_INSTALLING || downloadStatus == DownloadAppPresenter.STATE_DOWNLOADING){
            isShowLoading = true;
            if(mLastSweepAngle > sweepAngle){
                sweepAngle = mLastSweepAngle;
            }
            Bitmap bitmap = getLoadingBitmap(sweepAngle);
            if (bitmap != null && !bitmap.isRecycled()) {
                canvas.drawBitmap(bitmap, 0, 0, null);
                bitmap.recycle();
            }
            mLastSweepAngle = sweepAngle;
        }*/
    }

    /**
     * 应用下载并安装完成的回调
     */
    protected void onLoadingFinished() {

    }

    protected void onInstallFinished(boolean hasFocus) {

    }

    /**
     * 下载文字的位置
     *
     * @return
     */
    protected abstract Point getLoadingTextPoint();

    /**
     * 下载文字的大小
     *
     * @return
     */
    protected abstract float getLoadingTextSize();

    /**
     * 创建下载时显示的进度的bitmap
     *
     * @param sweepAngle
     * @return
     */
    protected Bitmap getLoadingBitmap(float sweepAngle) {
        return null;
    }

    @Override
    public void onClick(View v) {
        LetvLog.d(TAG, "onClick isShown = " + isShown());
        if (isShown()) {
            actionClickOrEnterKey();
            recordFocusPos();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean consumed = false;
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    consumed = actionClickOrEnterKey();
                    break;
            }
        }
        if (!consumed) {
            super.dispatchKeyEvent(event);
        }
        return consumed;
    }

    /**
     * item响应点击事件
     *
     * @return
     */
    public boolean actionClickOrEnterKey() {
        return false;
    }

    public T getItemInfo() {
        return mItemInfo;
    }

    public boolean startActivitySafely() {
        LetvLog.d(TAG, "startActivitySafely mItemInfo = " + mItemInfo);
        String pluginName = AppPluginActivator.getContext().getPackageName();
        String desktopParam = Utilities.getDesktopParam(mItemInfo);
        Toast.makeText(getContext(), "" + mItemInfo, Toast.LENGTH_SHORT).show();
        if (mItemInfo.getType() == AppDataModel.ITEM_TYPE_APPLICATION) {
            Intent intent = mItemInfo.intent;
            LetvLog.d(TAG, "startActivitySafely intent = " + intent);
            if (intent == null) {
                intent = mItemInfo.makeLaunchIntent();
            }
            ComponentName cpName = intent.getComponent();
            if (cpName != null) {

                if ("com.jiajia.kiss".equals(cpName.getPackageName())
                        || "com.jiajia.sports3".equals(cpName.getPackageName())) {
                    try {
                        Intent mIntent = new Intent("com.letv.action.primesense");
                        mIntent.setPackage("com.stv.openni");
                        getContext().stopService(mIntent);
                        // Settings.System.putInt(getContext().getContentResolver(), Settings.System.PRIME_SENSE, 0);
                        Settings.System.putInt(getContext().getContentResolver(), PRIME_SENSE, 0);
                    } catch (Exception e) {
                        LetvLog.d(TAG, " catch error, startActivitySafely ", e);
                    }
                }
            }
            try {
                if (mItemInfo.getPackageName().equals("com.stv.globalsetting") ||
                        mItemInfo.getPackageName().equals("com.stv.t2.globalsetting")) {
//                    SettingUtil settingUtil = SettingUtil.getInstance();
//                    settingUtil.startSettingMain(getContext()); // 调出设置
                } else {
                    intent.putExtra("desktopSource", pluginName);
                    intent.putExtra("desktopParam", desktopParam);
                    if (!Utilities.startAdManagerInPlugin(intent, mContext, null)) {
                        LauncherState.getInstance().getHostContext().startActivity(intent);
                        if (fragmentRef != null && fragmentRef.get() != null) {
                            fragmentRef.get().startAppAnim();
                        }
                    }
                }
                return true;
            } catch (ActivityNotFoundException e) {
                LetvLog.d(TAG, " catch error,ActivityNotFoundException !!,startActivitySafely Unable to launch intent=" + intent, e);
            } catch (SecurityException e) {
                LetvLog.d(TAG, " catch error,startActivitySafely Launcher does not have the permission to launch " + intent
                        + ". Make sure to create a MAIN intent-filter for the corresponding activity "
                        + "or use the exported attribute for this activity. intent = " + intent, e);
            } catch (Exception e) {
                LetvLog.d(TAG, " catch error,startActivitySafely catch an Exception... intent = " + intent + "  ", e);
            }
        } else if (mItemInfo.getType() == AppDataModel.ITEM_TYPE_PRELOADED) {
            if (Utilities.checkApkExist(mContext, mItemInfo.getPackageName())) {
                LetvLog.d(TAG, "startActivitySafely package = " + mItemInfo.getPackageName());
                PackageManager packageManager = mContext.getPackageManager();
                Intent mainIntent = packageManager.getLaunchIntentForPackage(mItemInfo.getPackageName());
                ComponentName componentName = mainIntent.getComponent();
                if (componentName != null) {
                    String className = componentName.getClassName();
                    mItemInfo.setType(AppDataModel.ITEM_TYPE_APPLICATION);
                    mItemInfo.setClassName(className);
                    mItemInfo.setComponentNameStr(componentName.flattenToString());
                    mItemInfo.init();
                    Intent intent = mItemInfo.intent;
                    intent.putExtra("desktopSource", pluginName);
                    intent.putExtra("desktopParam", desktopParam);
                    if (!Utilities.startAdManagerInPlugin(intent, mContext, null)) {
                        LauncherState.getInstance().getHostContext().startActivity(intent);
                        if (fragmentRef != null && fragmentRef.get() != null) {
                            fragmentRef.get().startAppAnim();
                        }
                    }
                }
                return true;
            }

            /*if (DownloadAppPresenter.getInstance().isDownloadDirectly()) {
                String downloadStatus = null;
                long downloadId = -1;
                float sweepAngle = 0;
                DownloadStatusBean downloadStatusBean = mItemInfo.getDownloadStatusBean();
                if(downloadStatusBean != null){
                    downloadStatus = downloadStatusBean.getDownloadStatus();
                     downloadId = downloadStatusBean.getDownloadId();
                     sweepAngle = downloadStatusBean.getSweepAngle();
                }
                if (downloadId != -1 && downloadStatus == DownloadAppPresenter.STATE_DOWNLOADING &&  sweepAngle != 0) {
                    // 取消下载
                    LetvLog.d(TAG, "actionClickOrEnterKey cancle download");
                    mCancleDialog = DialogFactory.createCancleDownloadDialog(mItemInfo);
                }else if(downloadStatus == null || sweepAngle == 0){
                    LetvLog.d(TAG, "actionClickOrEnterKey----install");
                    DownloadAppPresenter.getInstance().installApp(mItemInfo);
                }
            } else {
                gotoPreLoadAppDetailInStore(mItemInfo);
                DownloadAppPresenter.getInstance().putPreloadApp(mItemInfo);
            }*/
        } else if (mItemInfo.getType() == AppDataModel.ITEM_TYPE_SHORTCUT) {
            try {
                Intent launcherIntent = Intent.parseUri(mItemInfo.getShortcutIntentUrl(), 0);
                launcherIntent.putExtra("desktopSource", pluginName);
                launcherIntent.putExtra("desktopParam", desktopParam);
                LauncherState.getInstance().getHostContext().startActivity(launcherIntent);
                if (fragmentRef != null && fragmentRef.get() != null) {
                    fragmentRef.get().startAppAnim();
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                LetvLog.d(TAG, "startActivitySafely open shortcut fail", ex);
            }
        }
        return false;
    }

    public void gotoPreLoadAppDetailInStore(T preloadInfo) {
        /*DownloadAppPresenter.getInstance().bindService();
        String downStatus = null;
        String loadingTitle = null;
        DownloadStatusBean downloadStatusBean = preloadInfo.getDownloadStatusBean();
        if(downloadStatusBean != null){
            downStatus = downloadStatusBean.getDownloadStatus();
            loadingTitle = downloadStatusBean.getLoadingTitle();
        }
        boolean is = DownloadAppPresenter.getInstance().isShowShade(downStatus);
        LetvLog.d(TAG, "gotoPreLoadAppDetailInStore is = " + is);
        if (is) {
            if (!TextUtils.isEmpty(loadingTitle)) {
                Toast.makeText(mContext, loadingTitle, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        List<ItemInfo> existList = DownloadAppPresenter.getInstance().getExistPreload(preloadInfo.getPackageName());
        if(existList != null && existList.size() > 0){
            ItemInfo info = existList.get(0);
            if(info != null){
                DownloadStatusBean statusBean = info.getDownloadStatusBean();
                if( statusBean != null && (DownloadAppPresenter.STATE_DOWNLOADING.equals(statusBean.getDownloadStatus()) ||
                        DownloadAppPresenter.STATE_INSTALLING.equals(statusBean.getDownloadStatus())) ){
                    return;
                }
            }
        }
        try {
            Intent intent = new Intent(GOTO_STORE_DETAIL_ACTION);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            Bundle extras = new Bundle();
            extras.putInt("type", 3);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", 0);
            jsonObject.put("pkg", preloadInfo.getPackageName());
            jsonObject.put("from", AppPluginActivator.getContext().getPackageName());
            String value = jsonObject.toString();
            LetvLog.d(TAG, "gotoPreLoadAppDetailInStore value = " + value);
            extras.putString("value", value);
            intent.putExtras(extras);
            // LetvLog.d(TAG, "gotoPreLoadAppDetailInStore extras = " + extras);
            LauncherState.getInstance().getHostContext().startActivity(intent);
            if(fragmentRef != null && fragmentRef.get() != null){
                fragmentRef.get().startAppAnim();
            }
        } catch (Exception ex) {
            LetvLog.d(TAG, "gotoPreLoadAppDetailInStore", ex);
        }*/
    }

    private void recordFocusPos() {
        Object obj = getTag();
        if (obj instanceof String) {
            String tag = (String) obj;
            if (mItemInfo != null) {
                Long container = mItemInfo.getContainer();
                if (container != null && container > 0) {// 在文件夹中
                    LauncherState.getInstance().setAppInFolderFocusTag(tag);
                    // LetvLog.d(TAG, "recordFocusPos in folder tag = " + tag);
                } else {
                    LauncherState.getInstance().setAppFocusTag(tag);
                    // LetvLog.d(TAG, "recordFocusPos app tag = " + tag);
                }
            }
        }
    }

    public void setAppFragment(WeakReference<IAppFragment> fragmentRef) {
        this.fragmentRef = fragmentRef;
    }
}
