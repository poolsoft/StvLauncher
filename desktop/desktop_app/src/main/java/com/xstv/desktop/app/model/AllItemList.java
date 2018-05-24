
package com.xstv.desktop.app.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.xstv.library.base.LetvLog;
import com.xstv.desktop.app.db.ItemInfo;

import java.util.ArrayList;
import java.util.List;

class AllItemList {
    public static final int DEFAULT_APPLICATIONS_NUMBER = 42;

    private static final boolean DEBUG = true;

    private static final String TAG = AllItemList.class.getSimpleName();

    /**
     * The list off all apps.
     */
    public ArrayList<ItemInfo> data = new ArrayList<ItemInfo>(DEFAULT_APPLICATIONS_NUMBER);
    /**
     * The list of apps that have been added since the last notify() call.
     */
    public ArrayList<ItemInfo> added = new ArrayList<ItemInfo>(DEFAULT_APPLICATIONS_NUMBER);
    /**
     * The list of apps that have been removed since the last notify() call.
     */
    public ArrayList<ItemInfo> removed = new ArrayList<ItemInfo>();
    /**
     * The list of apps that have been modified since the last notify() call.
     */
    public ArrayList<ItemInfo> modified = new ArrayList<ItemInfo>();

    public AllItemList() {

    }

    /**
     * Query the package manager for MAIN/LAUNCHER activities in the supplied package.
     * 
     * @param context
     * @param packageName
     * @return A List<ResolveInfo> containing one entry for each matching Activity. These are ordered from best to worst match -- that is, the first item in the list is what is returned by
     *         resolveActivity. If there are no matching activities, an empty list is returned.
     */
    private static List<ResolveInfo> getActivitiyList(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);
        List<ResolveInfo> apps = null;
        try {
            apps = packageManager.queryIntentActivities(mainIntent, 0);
        } catch (Exception ex) {
            LetvLog.d(TAG, "getActivitiyList error", ex);
        }
        return apps;
    }

    /**
     * Returns whether <em>apps</em> contains <em>component</em>.
     */
    private static boolean findActivity(List<ResolveInfo> apps, ComponentName component) {
        if (component == null) {
            return false;
        }
        final String className = component.getClassName();
        for (ResolveInfo info : apps) {
            if (info.activityInfo.name.equals(className)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether <em>apps</em> contains <em>component</em>.
     */
    private static boolean findActivity(ArrayList<ItemInfo> apps, ComponentName component) {
        final int N = apps.size();
        boolean finded = false;
        for (int i = 0; i < N; i++) {
            final ItemInfo info = apps.get(i);
            if (info != null && info.componentName != null && info.componentName.equals(component)) {
                finded = true;
                break;
            }
        }
        return finded;
    }

    /**
     * Add the supplied ApplicationInfo objects to the list, and enqueue it into the list to broadcast when notify() is called.
     * <p/>
     * If the app is already in the list, doesn't add it.
     */
    public void add(ItemInfo info) {
        if (findActivity(data, info.componentName)) {
            return;
        }
        data.add(info);
        added.add(info);
    }

    public void clear() {
        data.clear();
        // TODO: do we clear these too?
        added.clear();
        removed.clear();
        modified.clear();
    }

    public int size() {
        return data.size();
    }

    public ItemInfo get(int index) {
        return data.get(index);
    }

    /**
     * Add the icons for the supplied apk called packageName.
     */
    public void addPackage(Context context, String packageName) {
        final List<ResolveInfo> matches = getActivitiyList(context, packageName);
        if (matches != null && matches.size() > 0) {
            for (ResolveInfo info : matches) {
                if (info.activityInfo.packageName.equals(packageName)) {
                    add(new ItemInfo(context.getPackageManager(), info));
                }
            }
        }
    }

    /**
     * Remove the apps for the given apk identified by packageName.
     */
    public void removePackage(String packageName) {
        final List<ItemInfo> data = this.data;
        for (int i = data.size() - 1; i >= 0; i--) {
            ItemInfo info = data.get(i);
            if (info != null) {
                final ComponentName component = info.componentName;
                if (component != null && packageName != null && packageName.equals(component.getPackageName())) {
                    removed.add(info);
                    data.remove(i);
                }
            }
        }
        // TODO icon cache flush
    }

    /**
     * Add and remove icons for this package which has been updated.
     */
    public void updatePackage(Context context, String packageName) {
        final List<ResolveInfo> matches = getActivitiyList(context, packageName);
        if (matches != null && matches.size() > 0) {
            // Find disabled/removed activities and remove them from data and add them
            // to the removed list.
            for (int i = data.size() - 1; i >= 0; i--) {
                final ItemInfo itemInfo = data.get(i);
                if (itemInfo.componentName == null) {
                    itemInfo.init();
                }
                if (itemInfo != null) {
                    final ComponentName component = itemInfo.componentName;
                    // Find package name equals but activity not,so remove it
                    if (packageName != null && packageName.equals(component.getPackageName())) {
                        if (!findActivity(matches, component)) {
                            removed.add(itemInfo);
                            data.remove(i);
                            // TODO rm from icon cache
                        }
                    }
                }
            }

            // Find enabled activities and add them to the adapter
            // Also updates existing activities with new labels/icons
            int count = matches.size();
            for (int i = 0; i < count; i++) {
                final ResolveInfo resolveInfo = matches.get(i);
                ItemInfo itemInfo = findApplicationInfoLocked(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                if (itemInfo == null) {
                    add(new ItemInfo(context.getPackageManager(), resolveInfo));
                } else {
                    // fix when uninstall but not update flags , can uninstall icon always show.
                    ItemInfo newShortcut = new ItemInfo(context.getPackageManager(), resolveInfo);
                    itemInfo.setFlags(newShortcut.getFlags());
                    itemInfo.setTitle(newShortcut.getTitle());
                    itemInfo.setComponentNameStr(newShortcut.getComponentNameStr());
                    itemInfo.setInstallTime(newShortcut.getInstallTime());
                    modified.add(itemInfo);
                    // TODO rm from icon cache
                }
            }
        } else {
            // Remove all data for this package.
            for (int i = data.size() - 1; i >= 0; i--) {
                final ItemInfo itemInfo = data.get(i);
                if (itemInfo.componentName == null) {
                    itemInfo.init();
                }
                if (itemInfo != null) {
                    final ComponentName component = itemInfo.componentName;
                    if (packageName != null && packageName.equals(component.getPackageName())) {
                        removed.add(itemInfo);
                        data.remove(i);
                        // TODO rm from icon cache
                    }
                }
            }
        }
    }

    /**
     * @param context
     * @param packageName
     * @return Returns a ResolveInfo containing the final activity intent that was determined to be the best action. Returns null if no matching activity was found. If multiple matching activities are
     *         found and there is no default set, returns a ResolveInfo containing something else, such as the activity resolver.
     */
    public ResolveInfo resolveActivity(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Intent newIntent = new Intent(Intent.ACTION_MAIN, null);
        newIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        newIntent.setPackage(packageName);
        ResolveInfo info = packageManager.resolveActivity(newIntent, 0);
        return info;
    }

    /**
     * Find an ApplicationInfo object for the given packageName and className.
     */
    private ItemInfo findApplicationInfoLocked(String packageName, String className) {
        for (ItemInfo info : data) {
            if (info != null && packageName.equals(info.getPackageName())
                    && className.equals(info.getClassName())) {
                return info;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return " data = " + data + " added = " + added + " removed = " + removed + " modified = " + modified;
    }
}
