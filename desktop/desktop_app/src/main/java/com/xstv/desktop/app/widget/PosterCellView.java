
package com.xstv.desktop.app.widget;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.RectF;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xstv.base.LetvLog;
import com.xstv.desktop.app.AppPluginActivator;
import com.xstv.desktop.app.R;
import com.xstv.desktop.app.bean.ParamBean;
import com.xstv.desktop.app.bean.PosterInfo;
import com.xstv.desktop.app.bean.QqParamBean;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.model.AppDataModel;
import com.xstv.desktop.app.util.LauncherState;
import com.xstv.desktop.app.util.Utilities;

import java.net.URLEncoder;

public abstract class PosterCellView extends BaseCellView<ItemInfo> {

    private static final String TAG = PosterCellView.class.getSimpleName();

    private Point mLoadingTextPoint;
    private RectF mLogoRectF;
    private CircleProgress mCircleProgress;
    private PackageManager mPackageManager;

    public PosterCellView(Context context) {
        this(context, null);
    }

    public PosterCellView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PosterCellView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void initData(Context context, AttributeSet attrs, int defStyle) {
        super.initData(context, attrs, defStyle);
        mPackageManager = context.getPackageManager();
    }

    @Override
    public void bindData(ItemInfo itemInfo) {
        super.bindData(itemInfo);
        setShadeVisibility();
    }

    public void setEcoImageView(ImageView ecoImageView, String iconPath) {
        LetvLog.d(TAG, "setEcoImageView===>" + iconPath);
        RequestOptions myOptions = new RequestOptions()
                .override(ecoImageView.getWidth(), ecoImageView.getHeight())
                .error(R.drawable.pic_default);

        Glide.with(getContext())
                .applyDefaultRequestOptions(myOptions)
                .load(iconPath)
                .into(ecoImageView);

        ecoImageView.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    public void setPosterSimpleDraweeView(ImageView simpleDraweeView, String iconPath) {
        if (iconPath == null) {
            iconPath = "";
        }
        Glide.with(simpleDraweeView).load(iconPath).into(simpleDraweeView);
    }

    public void setLogoImageView(final ImageView ecoImageView, String iconPath) {
        Glide.with(ecoImageView).load(iconPath).into(ecoImageView);
    }

    public void setLogoSimpleDraweeView(final ImageView simpleDraweeView, String iconPath) {
        if (iconPath == null) {
            iconPath = "";
        }
        Glide.with(simpleDraweeView).load(iconPath).into(simpleDraweeView);
    }

    @Override
    protected Bitmap getLoadingBitmap(float sweepAngle) {
        // LetvLog.d(TAG, "drawProgress sweepAngle = " + sweepAngle);
        /*if (mLoadingTextPoint == null) {
            mLoadingTextPoint = getLoadingTextPoint();
        }
        if (mLogoRectF == null) {
            mLogoRectF = getLogoRect();
        }
        if (mCircleProgress == null) {
            mCircleProgress = new CircleProgress(getContext());
        }
        DownloadStatusBean statusBean = mItemInfo.getDownloadStatusBean();
        String loadingTitle = null;
        if (statusBean != null) {
            loadingTitle = statusBean.getLoadingTitle();
        }
        Bitmap bitmap = mCircleProgress.createProgress(sweepAngle, getWidth(), getHeight(),
                getLoadingTextSize(), mLoadingTextPoint, mLogoRectF, mItemInfo.getTitle(),
                loadingTitle);
        return bitmap;*/
        return null;
    }

    @Override
    protected void onInstallFinished(boolean hasFocus) {
        super.onInstallFinished(hasFocus);
        if (hasFocus && LauncherState.getInstance().isVisibleToUser()) {
            callOnClick();
        }
    }

    /**
     * logo的位置
     *
     * @return
     */
    protected abstract RectF getLogoRect();

    /**
     * 下载文字的大小
     *
     * @return
     */
    protected abstract float getLoadingTextSize();

    @Override
    public boolean actionClickOrEnterKey() {
        String tag = (String) getTag();
        Toast.makeText(getContext(), "tag=" + tag, Toast.LENGTH_SHORT).show();
        if (!Utilities.isNetworkConnected(mContext) && (mItemInfo == null || TextUtils.isEmpty(mItemInfo.getPackageName()) &&
                TextUtils.isEmpty(((PosterInfo) mItemInfo).getIconUrl()))) {
            String str = AppPluginActivator.getContext().getResources().getString(R.string.check_network_text);
            Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
            return true;
        }

        if (mItemInfo == null || mState != BaseWorkspace.State.STATE_NORMAL) {
            return true;
        }

        LetvLog.i(TAG, "actionClickOrEnterKey packageName = " + mItemInfo.getPackageName() + " title = " + mItemInfo.getTitle());

        if (Utilities.checkApkExist(mContext, mItemInfo.getPackageName())) {
            // 应用已经安装
            ParamBean paramBean = ((PosterInfo) mItemInfo).getJumpParam();
            LetvLog.d(TAG, "actionClickOrEnterKey paramBean = " + paramBean);
            if (paramBean != null && paramBean.action != null) {
                try {
                    if (mItemInfo.getVersionCode() == 0) {
                        PackageInfo packageInfo = LauncherState.getInstance().getHostContext().getPackageManager()
                                .getPackageInfo(mItemInfo.getPackageName(), 0);
                        mItemInfo.setVersionCode(packageInfo.versionCode);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                // 说明是打洞应用
                String jumpVersionStr = paramBean.jumpVersion;
                if (Utilities.QQ_PACKAGE_NAME.equals(mItemInfo.getPackageName())) {
                    /*if (DownloadAppPresenter.getInstance().isDownloadDirectly()) {
                        int versionCode = mItemInfo.getVersionCode();
                        if (!TextUtils.isEmpty(jumpVersionStr)) {
                            try {
                                int jumpVersion = Integer.parseInt(jumpVersionStr);
                                if (versionCode != 0 && jumpVersion > versionCode) {
                                    LetvLog.d(TAG, "actionClickOrEnterKey jumpVersion = " + jumpVersion + " versionCode = " + versionCode);
                                    DownloadAppPresenter.getInstance().updateApp(mItemInfo, versionCode);
                                    return true;
                                }
                            } catch (Exception ex) {
                                LetvLog.d(TAG, "actionClickOrEnterKey jumpVersion error!!!");
                            }
                        }
                    }*/
                    QqParamBean qqParamBean = paramBean.qqParamBean;
                    if (qqParamBean != null) {
                        try {
                            int action = Integer.parseInt(paramBean.action);
                            long m0 = Long.parseLong(qqParamBean.m0);
                            long pull_from = qqParamBean.pull_from;
                            String m1 = qqParamBean.m1;
                            if (!TextUtils.isEmpty(m1)) {
                                m1 = URLEncoder.encode(m1, "UTF-8");
                            } else {
                                m1 = "";
                            }
                            String mb = qqParamBean.mb;
                            if (TextUtils.isEmpty(mb)) {
                                mb = "true";
                            }
                            Intent intent = new Intent();
                            String uri = "musictv://?action=" + action + "&pull_from=" + pull_from + "&m0=" + m0 + "&m1=" + m1 + "&mb=" + mb;
                            LetvLog.d(TAG, "actionClickOrEnterKey uri = " + uri);
                            intent.setData(Uri.parse(uri));
                            LauncherState.getInstance().getHostContext().startActivity(intent);
                            if (fragmentRef != null && fragmentRef.get() != null) {
                                fragmentRef.get().startAppAnim();
                            }
                        } catch (Exception ex) {
                            LetvLog.d(TAG, "actionClickOrEnterKey qq jump param error !!!");
                        }
                        //saveStartTimestamp();
                        return true;
                    } else {
                        LetvLog.d(TAG, "actionClickOrEnterKey qqParamBean is null !!!");
                    }
                } else {
                    boolean isJump = true;
                    int versionCode = mItemInfo.getVersionCode();
                    if (!TextUtils.isEmpty(jumpVersionStr)) {
                        try {
                            int jumpVersion = Integer.parseInt(jumpVersionStr);
                            LetvLog.d(TAG, "actionClickOrEnterKey jumpVersion = " + jumpVersion + " versionCode = " + versionCode);
                            if (jumpVersion > versionCode) {
                                isJump = false;
                            }
                        } catch (Exception ex) {
                            LetvLog.d(TAG, "actionClickOrEnterKey jumpVersion error!!!");
                        }
                    }
                    if (isJump) {
                        String action = paramBean.action;
                        int type = paramBean.type;
                        String value = paramBean.value;
                        LetvLog.i(TAG, "actionClickOrEnterKey action = " + action + " type = " + type + " value = " + value);
                        String from = AppPluginActivator.getContext().getPackageName();
                        Intent intent = new Intent(action);
                        intent.putExtra("type", type);
                        intent.putExtra("from", from);
                        intent.putExtra("value", value);
                        try {
                            LauncherState.getInstance().getHostContext().startActivity(intent);
                            if (fragmentRef != null && fragmentRef.get() != null) {
                                fragmentRef.get().startAppAnim();
                            }
                        } catch (Exception ex) {
                            LetvLog.e(TAG, "actionClickOrEnterKey jump action", ex);
                        }
                        //saveStartTimestamp();
                        return true;
                    }
                }
            }
            // 正常应用
            if (TextUtils.isEmpty(mItemInfo.getClassName())) {
                String packageName = mItemInfo.getPackageName();
                Intent mainIntent = mPackageManager.getLaunchIntentForPackage(packageName);
                if (mainIntent != null) {
                    // 入口activity的className
                    String className = mainIntent.getComponent().getClassName();
                    mItemInfo.setClassName(className);
                    // LetvLog.d(TAG, "setAllHomeBean className = " + className + " className1 = " + className1);
                }
            }
            LetvLog.i(TAG, "actionClickOrEnterKey startActivitySafely");
            // 非打洞应用,直接打开应用
            // TODO: 17-6-19 是否需要设置type
            mItemInfo.setType(AppDataModel.ITEM_TYPE_APPLICATION);
            startActivitySafely();
            // 记录打开的时间
            //saveStartTimestamp();
        } else {// 应用未安装
            String packageName = mItemInfo.getPackageName();
            if (packageName != null) {
                /*if (DownloadAppPresenter.getInstance().isDownloadDirectly()) {
                    DownloadStatusBean downloadStatusBean = mItemInfo.getDownloadStatusBean();
                    String downloadStatus = null;
                    long downloadId = -1;
                    float sweepAngle = 0;
                    if (downloadStatusBean != null) {
                        downloadStatus = downloadStatusBean.getDownloadStatus();
                        downloadId = downloadStatusBean.getDownloadId();
                        sweepAngle = downloadStatusBean.getSweepAngle();
                    }
                    if (downloadId != -1 && downloadStatus == DownloadAppPresenter.STATE_DOWNLOADING && sweepAngle != 0) {
                        // 取消下载
                        LetvLog.d(TAG, "actionClickOrEnterKey cancle download");
                        mCancleDialog = DialogFactory.createCancleDownloadDialog(mItemInfo);
                    } else if (downloadStatus == null || sweepAngle == 0) {
                        LetvLog.d(TAG, "actionClickOrEnterKey----install");
                        DownloadAppPresenter.getInstance().installApp(mItemInfo);
                    }
                } else {
                    gotoPreLoadAppDetailInStore(mItemInfo);
                    DownloadAppPresenter.getInstance().putPreloadApp(mItemInfo);
                    if (fragmentRef != null && fragmentRef.get() != null) {
                        fragmentRef.get().startAppAnim();
                    }
                }*/
            } else {
                LetvLog.d(TAG, "actionClickOrEnterKey packageName is null");
            }
        }
        return true;
    }

    @Override
    public boolean startActivitySafely() {
        if (mItemInfo == null) {
            return false;
        }
        LetvLog.d(TAG, "startActivitySafely className = " + mItemInfo.getClassName() + " packageName = " + mItemInfo.getPackageName());
        String pluginName = AppPluginActivator.getContext().getPackageName();
        try {
            Intent intent = mItemInfo.makeLaunchIntent();
            intent.putExtra("desktopSource", pluginName);
            intent.putExtra("desktopParam", Utilities.getDesktopParam(mItemInfo));
            if (!Utilities.startAdManagerInPlugin(intent, mContext, null)) {
                LauncherState.getInstance().getHostContext().startActivity(intent);
                if (fragmentRef != null && fragmentRef.get() != null) {
                    fragmentRef.get().startAppAnim();
                }
            }
        } catch (ActivityNotFoundException e) {
            LetvLog.d(TAG, " catch error,ActivityNotFoundException !!,startActivitySafely Unable to launch intent=", e);
            PackageManager packageManager = getContext().getPackageManager();
            try {
                Intent mainIntent = packageManager.getLaunchIntentForPackage(mItemInfo.getPackageName());
                if (mainIntent != null) {
                    mainIntent.putExtra("desktopSource", pluginName);
                    mainIntent.putExtra("desktopParam", Utilities.getDesktopParam(mItemInfo));
                    if (!Utilities.startAdManagerInPlugin(mainIntent, mContext, null)) {
                        LauncherState.getInstance().getHostContext().startActivity(mainIntent);
                        if (fragmentRef != null && fragmentRef.get() != null) {
                            fragmentRef.get().startAppAnim();
                        }
                    }
                }
            } catch (Exception ex) {
                LetvLog.d(TAG, "startActivitySafely again error", ex);
            }
        } catch (SecurityException e) {
            LetvLog.d(TAG, "startActivitySafely exception ", e);
        } catch (Exception ex) {
            LetvLog.d(TAG, "startActivitySafely", ex);
        }
        return false;
    }

    /**
     * 设置海报图标题后边的阴影可见性
     */
    protected void setShadeVisibility() {
    }
}
