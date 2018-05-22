
package com.xstv.desktop.app.presenter;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.xstv.base.LetvLog;
import com.xstv.desktop.app.AppPluginActivator;
import com.xstv.desktop.app.bean.FolderInfo;
import com.xstv.desktop.app.bean.PosterInfo;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.util.LauncherState;
import com.xstv.desktop.app.util.Utilities;
import com.xstv.desktop.app.widget.BaseCellView;
import com.xstv.desktop.app.widget.BaseContent;
import com.xstv.desktop.app.widget.PosterCellView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ViewExposureReportPresenter {

    private static final String TAG = ViewExposureReportPresenter.class.getSimpleName();

    public static final String EXPOSURE_ACTION = "appdPosterExpose";
    public static final String CLICK_ACTION = "appdesPosterClick";

    /**
     * 海报类型：1代表应用
     */
    private static final String TYPE_APP = "1";

    /**
     * 海报类型：2代表文件夹
     */
    private static final String TYPE_FOLDER = "2";

    /**
     * 海报类型：3代表应用在文件夹中
     */
    private static final String TYPE_APP_IN_FOLDER = "3";

    /**
     * 海报类型: 首页海报
     */
    private static final String TYPE_POSTER = "4";

    private ArrayList<ItemInfo> appReportList = new ArrayList<ItemInfo>();
    private ArrayList<String> posterReportList = new ArrayList<String>();

    private String mac = "";

    public ViewExposureReportPresenter() {
        mac = "";
    }

    public void doExposureViewReport(WeakReference<ViewGroup> viewGroupRef) {
        if (viewGroupRef == null || viewGroupRef.get() == null) {
            Log.w(TAG, "doExposureViewReport weak recycle.");
            return;
        }

        ViewGroup childViewGroup = viewGroupRef.get();
        if (childViewGroup == null || childViewGroup.getVisibility() != View.VISIBLE) {
            return;
        }
        int childCount = childViewGroup.getChildCount();
        if (childCount == 0) {
            return;
        }

        for (int i = 0; i < childCount; i++) {
            View child = childViewGroup.getChildAt(i);
            if (!isInVisibleRect(childViewGroup, child)) {// 在可视区域
                continue;
            }

            if (child instanceof BaseContent) {
                BaseContent content = (BaseContent) child;
                Object obj = content.getTag();
                if (obj instanceof String) {
                    String tag = (String) obj;
                    if (!posterReportList.contains(tag)) {
                        posterReportList.add(tag);
                    } else {
                        continue;
                    }
                }

                int contentChildCount = content.getChildCount();
                for (int i1 = 0; i1 < contentChildCount; i1++) {
                    View contentChild = content.getChildAt(i1);
                    if (contentChild instanceof PosterCellView) {
                        PosterCellView posterCellView = (PosterCellView) contentChild;
                        ItemInfo itemInfo = posterCellView.getItemInfo();
                        if (itemInfo instanceof PosterInfo) {
                            PosterInfo posterInfo = (PosterInfo) itemInfo;
                            // 开始上报
                            String tag = (String) posterCellView.getTag();
                            String reportStr = getReportStr(posterInfo, -1, tag, EXPOSURE_ACTION);
                        }
                    }
                }
            } else if (child instanceof BaseCellView) {
                int pos = -1;
                if (childViewGroup instanceof RecyclerView) {
                    pos = ((RecyclerView) childViewGroup).getChildPosition(child);
                }
                BaseCellView baseCellView = (BaseCellView) child;
                ItemInfo itemInfo = baseCellView.getItemInfo();
                String tag = (String) baseCellView.getTag();
                if (itemInfo != null) {
                    if (!appReportList.contains(itemInfo)) {
                        appReportList.add(itemInfo);
                        // 开始上报
                        String reportStr = getReportStr(itemInfo, pos, tag, EXPOSURE_ACTION);
                    }
                }
            }
        }
    }

    private boolean isInVisibleRect(ViewGroup viewGroup, View view) {
        if(view == null || viewGroup == null){
            return false;
        }
        int[] location = new int[2];
        view.getLocationInWindow(location);

        Rect r = new Rect();
        viewGroup.getGlobalVisibleRect(r);

        // LetvLog.d(TAG, "viewIsVisible pos[1] = " + location[1] + ",height = " + view.getHeight()
        // + ",r.bottom = " + r.bottom + "r.top = " + r.top);
        int viewLocationY = location[1] + view.getHeight();
        return viewLocationY <= r.bottom && location[1] >= r.top;
    }

    public String getReportStr(ItemInfo itemInfo, int appPos, String tag, String action) {
        if (itemInfo == null) {
            return null;
        }
        int pos = getPosition(itemInfo, appPos, tag);
        if (pos == -1) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("action=" + action);
        builder.append("&posterPos=" + pos);
        builder.append("&posterType=" + getType(itemInfo));
        if (!(itemInfo instanceof FolderInfo)) {
            builder.append("&appName=" + itemInfo.getTitle());
            String packageName = itemInfo.getPackageName();
            builder.append("&packageName=" + packageName);
            int versionCode = itemInfo.getVersionCode();
            String versionName = itemInfo.getVersionName();
            // LetvLog.d(TAG, "getReportStr versionName = " + versionName + "versionCode = " + versionCode);
            if (versionCode == 0 || TextUtils.isEmpty(versionName)) {
                try {
                    PackageInfo packageInfo = LauncherState.getInstance().getHostContext().getPackageManager().getPackageInfo(packageName, 0);
                    versionCode = packageInfo.versionCode;
                    versionName = packageInfo.versionName;
                    itemInfo.setVersionCode(versionCode);
                    itemInfo.setVersionName(versionName);
                } catch (PackageManager.NameNotFoundException e) {
                }
            }
            builder.append("&appVersionName=" + versionName);
            builder.append("&appVersionCode=" + versionCode);

            if (itemInfo instanceof PosterInfo) {
                PosterInfo posterInfo = (PosterInfo) itemInfo;
                builder.append("&promoid=" + posterInfo.getPromoid());
                if (CLICK_ACTION.equals(action)) {
                    builder.append("&poptype=1");
                } else {
                    builder.append("&poptype=0");
                }
                builder.append("&mac=" + mac);
                builder.append("&position=" + posterInfo.getPosid());
                builder.append("&reqid=" + posterInfo.getReqid());
            }
        }
        builder.append("&appdVersionName=" + Utilities.getVersionCode(AppPluginActivator.getContext()));
        LetvLog.d(TAG, "getReportStr toString= " + builder.toString());
        return builder.toString();
    }

    private int getPosition(ItemInfo itemInfo, int appPos, String tag) {
        int pos = -1;
        if (itemInfo instanceof PosterInfo) {
            try {
                if (tag != null) {
                    String[] tags = tag.split(",");
                    if (tags.length == 2) {
                        String pos1Str = tags[0];
                        String pos2Str = tags[1];
                        int pos1 = Integer.parseInt(pos1Str);
                        if (pos1 == 0) {
                            pos1 = 0;
                        } else if (pos1 == 1) {
                            pos1 = 5;
                        }
                        int pos2 = Integer.parseInt(pos2Str);
                        pos = pos1 + (pos2 + 1);
                    }
                }
            } catch (Exception ex) {
            }
        } else {
            pos = appPos;
        }
        return pos;
    }

    private String getType(ItemInfo itemInfo) {
        String type = TYPE_APP;
        if (itemInfo instanceof PosterInfo) {
            type = TYPE_POSTER;
        } else {
            if (itemInfo instanceof FolderInfo) {
                type = TYPE_FOLDER;
            } else {
                Long container = itemInfo.getContainer();
                // String containerName = itemInfo.getContainerName();
                // TODO 因为创建的工具文件夹中的AppCellView没有保存containerName到数据库中，
                // 导致条件判断失败，所以不能加containerName != null
                // if (container != 0 && containerName != null) {
                if (container != 0) {
                    type = TYPE_APP_IN_FOLDER;
                } else {
                    type = TYPE_APP;
                }
            }
        }
        return type;
    }

    public void clearCache(){
        appReportList.clear();
        posterReportList.clear();
    }

}
