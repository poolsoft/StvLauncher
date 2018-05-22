
package com.xstv.desktop.app.bean;

import android.graphics.Bitmap;

public class AppIconBean {

    /**
     * 应用icon
     */
    private Bitmap iconBitmap;
    /**
     * 高斯模糊后的背景图
     */
    private Bitmap blurBgBitmap;

    public Bitmap getIconBitmap() {
        return iconBitmap;
    }

    public void setIconBitmap(Bitmap iconBitmap) {
        this.iconBitmap = iconBitmap;
    }

    public Bitmap getBlurBgBitmap() {
        return blurBgBitmap;
    }

    public void setBlurBgBitmap(Bitmap blurBgBitmap) {
        this.blurBgBitmap = blurBgBitmap;
    }
}
