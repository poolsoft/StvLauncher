/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.xstv.desktop.emodule.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of utility methods, all static.
 */
public class Utils {

    /*
     * Making sure public utility methods remain static
     */
    private Utils() {
    }

    /**
     * The RecyclerView is not currently scrolling.
     */
    public static final int SCROLL_STATE_IDLE = 0;

    /**
     * The RecyclerView is currently being dragged by outside input such as user touch input.
     */
    public static final int SCROLL_STATE_DRAGGING = 1;

    /**
     * The RecyclerView is currently animating to a final position while not under
     * outside control.
     */
    public static final int SCROLL_STATE_SETTLING = 2;

    public static interface OnScrollStateListener {
        /**
         * Callback method to be invoked when RecyclerView's scroll state changes.
         *
         * @param newState The updated scroll state. One of {@link #SCROLL_STATE_IDLE},
         *                 {@link #SCROLL_STATE_DRAGGING} or {@link #SCROLL_STATE_SETTLING}.
         */
        public void onScrollStateChanged(int newState);
    }

    static boolean scrolling = false;
    static List<OnScrollStateListener> mOnScrollStateListeners;

    public static boolean isScrolling() {
        return scrolling;
    }

    public static void setScrolling(boolean scroll) {
        scrolling = scroll;
    }

    public static void setScrollState(int newState) {
        if (mOnScrollStateListeners != null) {
            for (int i = 0; i < mOnScrollStateListeners.size(); i++) {
                OnScrollStateListener onScrollStateListener = mOnScrollStateListeners.get(i);
                onScrollStateListener.onScrollStateChanged(newState);
            }
        }
    }

    public static void addOnScrollStateListeners(OnScrollStateListener listener) {
        if (mOnScrollStateListeners == null) {
            mOnScrollStateListeners = new ArrayList<OnScrollStateListener>();
        }
        mOnScrollStateListeners.add(listener);
    }

    public static void removeOnScrollStateListeners(OnScrollStateListener listener) {
        if (mOnScrollStateListeners == null) {
            return;
        }
        mOnScrollStateListeners.remove(listener);
    }

    static boolean sLongPress = false;

    public static void longPress(int keycode) {
        if (keycode == KeyEvent.KEYCODE_DPAD_UP || keycode == KeyEvent.KEYCODE_DPAD_DOWN) {
            sLongPress = true;
        } else {
            sLongPress = false;
        }
    }

    public static boolean longPressScrolling() {
        return sLongPress && scrolling;
    }

    static boolean sFlyInAnimation = false;
    public final static long FlyInAnimDuration = 1000;

    public static boolean IsPlayingFlyInAnimation() {
        return sFlyInAnimation;
    }

    public static int getPackageVersion(Context context) {
        return getPackageVersion(context, context.getPackageName());
    }

    public static int getPackageVersion(Context context, String name) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(name, 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean shouldUseLowQualityBackground() {
        return true;
    }
}
