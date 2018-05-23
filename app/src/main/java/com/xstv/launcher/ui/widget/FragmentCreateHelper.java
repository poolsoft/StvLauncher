
package com.xstv.launcher.ui.widget;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.stv.plugin.demo.fragment.DemoFragment;
import com.xstv.base.BaseFragment;
import com.xstv.desktop.app.fragment.AppFragment;
import com.xstv.launcher.provider.db.ScreenInfo;
import com.xstv.launcher.ui.fragment.EmptyFragment;

public class FragmentCreateHelper {


    public static BaseFragment createVirtualFragment(Context ctx, FragmentPresenter fp) {
        BaseFragment fragment = (BaseFragment) Fragment.instantiate(ctx, EmptyFragment.class.getName());
        fragment.tag = fp.info.getPackageName() + "-lock";
        return fragment;
    }

    /**
     * {@link ScreenInfo#mark1 is the native fragment class name}
     *
     * @param context
     * @param info
     * @return
     */
    public static BaseFragment createNativeFragment(Context context, ScreenInfo info) {
        BaseFragment fragment;
        if ("com.xstv.desktop.app".equals(info.getPackageName())) {
            fragment = (BaseFragment) Fragment.instantiate(context, AppFragment.class.getName());
        } else if ("com.xstv.desktop.example".equals(info.getPackageName())) {
            fragment = (BaseFragment) Fragment.instantiate(context, DemoFragment.class.getName());
        } else {
            fragment = (BaseFragment) Fragment.instantiate(context, EmptyFragment.class.getName());
        }
        fragment.tag = info.getPackageName();
        return fragment;
    }

}
