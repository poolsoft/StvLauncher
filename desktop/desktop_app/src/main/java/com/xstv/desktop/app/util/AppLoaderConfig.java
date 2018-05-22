
package com.xstv.desktop.app.util;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.support.v4.util.ArrayMap;

import com.xstv.base.LetvLog;
import com.xstv.desktop.app.AppPluginActivator;
import com.xstv.desktop.app.bean.FolderInfo;
import com.xstv.desktop.app.db.ItemInfo;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class AppLoaderConfig {
    private static final String TAG = AppLoaderConfig.class.getSimpleName();
    public static final int TYPE_DEFAULT = 0x0001;
    public static final int TYPE_OUTER = 0x0010;
    public static final int TYPE_OUTER_HAS_LETV = 0x0100;
    public static int LOAD_TYPE = TYPE_DEFAULT;

    public static final String APP_LETV = "com.letv.tv";
    public static final String APP_GAMECENTER = "com.letv.tvos.gamecenter";

    private static ArrayMap<String, Integer> sortMap = new ArrayMap<String, Integer>();
    public static HashMap<String, Integer> forceSortMap = new LinkedHashMap<String, Integer>(2);


    public static void sort(List<ItemInfo> data, boolean isFirst) {
        if (isFirst) {
            setLoadType(data, false);
            if (sortMap.size() == 0) {
                loadSortMapFromXml(true);
            }
        }
        Collections.sort(data, new AppInfoComparator(isFirst));
    }

    private static class AppInfoComparator implements Comparator<ItemInfo> {
        private boolean mIsFirst;
       // HashMap<String, Integer> sortMap = new HashMap<String, Integer>();

        AppInfoComparator(boolean isFirst) {
            mIsFirst = isFirst;
            //sortMap = getOrderMap();
        }

        public final int compare(ItemInfo a, ItemInfo b) {
            Integer aIndex = null;
            Integer bIndex = null;
            if (mIsFirst) {
                if (a.getComponentNameStr() != null) {
                    aIndex = sortMap.get(a.getComponentNameStr());
                } else {
                    if (a instanceof FolderInfo) {
                        aIndex = sortMap.get(((FolderInfo) a).mSortID);
                    }
                }
                if (b.getComponentNameStr() != null) {
                    bIndex = sortMap.get(b.getComponentNameStr());
                } else {
                    if (b instanceof FolderInfo) {
                        bIndex = sortMap.get(((FolderInfo) b).mSortID);
                    }
                }
            } else {
                aIndex = a.getIndex();
                bIndex = b.getIndex();
            }
            if (aIndex != null && bIndex != null) {
                return aIndex.compareTo(bIndex);
            } else if (aIndex != null && bIndex == null) {
                return -1;
            } else if (aIndex == null && bIndex != null) {
                return 1;
            } else {
                // return sCollator.compare(a.title, b.title);
                Long t1 = a.getInstallTime();
                if (t1 == null) {
                    t1 = 0L;
                }
                Long t2 = b.getInstallTime();
                if (t2 == null) {
                    t2 = 0L;
                }
                return t1.compareTo(t2);
            }
        }
    }

    /**
     * @param data
     */
    public static void setLoadType(List<ItemInfo> data, boolean isForForceSort) {
        //LetvLog.d(TAG, " setLoadType begin ");
        ArrayList<String> packageNameList = new ArrayList<String>();
        for (ItemInfo itemInfo : data) {
            packageNameList.add(itemInfo.getPackageName());
            if(isForForceSort){
                if(itemInfo instanceof  FolderInfo){
                    FolderInfo folderInfo = (FolderInfo) itemInfo;
                    ArrayList<ItemInfo> itemInfosInFolder = folderInfo.getContents();
                    if(itemInfosInFolder != null && itemInfosInFolder.size() > 0){
                        for (ItemInfo info : itemInfosInFolder) {
                            packageNameList.add(info.getPackageName());
                        }
                    }
                }
            }
        }
        if (packageNameList.contains(APP_GAMECENTER)) {
            LOAD_TYPE |= TYPE_DEFAULT;
        } else {
            LOAD_TYPE |= TYPE_OUTER;
            if (packageNameList.contains(APP_LETV)) {
                LOAD_TYPE |= TYPE_OUTER_HAS_LETV;
            }
        }
        printType();
        //LetvLog.d(TAG, " setLoadType end ");
    }

    private static void printType() {
        switch (LOAD_TYPE) {
            case TYPE_DEFAULT:
                LetvLog.i(TAG, " printType LOAD_TYPE  is TYPE_DEFAULT ");
                break;
            case TYPE_OUTER:
                LetvLog.i(TAG, " printType LOAD_TYPE  is TYPE_OUTER ");
                break;
            case TYPE_OUTER_HAS_LETV:
                LetvLog.i(TAG, " printType LOAD_TYPE  is TYPE_HAS_LETV ");
                break;
        }
    }

    /**
     * @param isFirst if db no data isFirst = true
     */
    public static void loadSortMapFromXml(boolean isFirst) {
        long startTime = System.currentTimeMillis();

        sortMap.clear();
        forceSortMap.clear();

        Context context = AppPluginActivator.getContext();
        int saveXmlVersionCode = PreferencesUtils.getInt(context, "app_order_version", 0);
        XmlResourceParser parser = null;
        try {
            parser = null;//AppPluginActivator.getContext().getResources().getXml(getSortXmlResId());
            if (parser == null) {
                return;
            }
            int eventType = parser.getEventType();
            int index = -1;
            boolean isVersionChanged = false;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String tag = parser.getName();
                        if ("applist".equals(tag)) {
                            int versionCode = parser.getAttributeIntValue(0, 0);
                            LetvLog.d(TAG, "loadSortMapFromXml versionCode = " + versionCode +
                                    " saveXmlVersionCode = " + saveXmlVersionCode);
                            if (versionCode > 0) {
                                PreferencesUtils.putInt(context, "app_order_version", versionCode);
                            }
                            if (!isFirst) {
                                if (versionCode > saveXmlVersionCode) {
                                    isVersionChanged = true;
                                }
                            }
                        } else if ("app".equals(tag)) {
                            index++;
                            if (isFirst) {
                                String componentName = parser.nextText();
                                /*LetvLog.d(TAG, "loadSortMapFromXml componentName:" + componentName
                                        + "index = " + index);*/
                                if (componentName != null) {
                                    sortMap.put(componentName.trim(), index);
                                }
                            } else {
                                int forceOrderIndex = parser.getAttributeIntValue(0, -1);
                                String componentName = parser.nextText();
                                /*LetvLog.d(TAG, "loadSortMapFromXml componentName:"
                                        + componentName + ",forceOrderIndex:" + forceOrderIndex
                                        + ",isVersionChanged = " + isVersionChanged);*/
                                if (isVersionChanged) {
                                    if (componentName != null) {
                                        if (forceOrderIndex != -1 && forceOrderIndex >= 0) {
                                            forceSortMap.put(componentName.trim(), forceOrderIndex);
                                        }
                                    }
                                } else {
                                    return;
                                }
                            }
                        }
                        break;
                }

                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
        LetvLog.d(TAG, "loadSortMapFromXml sortMap.size = " + sortMap.size() + " forceSortMap.size = " + forceSortMap.size());
    }


    private static int getSortXmlResId() {
        return -1;
    }

    public static void release(){
        sortMap.clear();
    }
}
