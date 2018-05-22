
package com.xstv.desktop.app.util;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.xstv.base.LetvLog;
import com.xstv.desktop.app.bean.AppIconBean;

public class BitmapCache {

    private static final String TAG = BitmapCache.class.getSimpleName();

    private LruCache<Long, AppIconBean> mMemoryCache;

    private static BitmapCache sInstance;
    private boolean isRelease = false;

    public static BitmapCache getInstance() {
        if (sInstance == null) {
            synchronized (BitmapCache.class) {
                if (sInstance == null) {
                    sInstance = new BitmapCache();
                }
            }
        }
        return sInstance;
    }

    /**
     * 是否要释放bitmap
     * @param isRelease
     */
    public void setIsRelease(boolean isRelease){
        this.isRelease = isRelease;
    }

    private BitmapCache() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // 使用最大可用内存值的1/10作为缓存的大小。
        int cacheSize = maxMemory / 10;
        LetvLog.d(TAG, "BitmapCache maxMemory = " + maxMemory + " cacheSize = " + cacheSize);
        mMemoryCache = new LruCache<Long, AppIconBean>(cacheSize) {
            @Override
            protected int sizeOf(Long key, AppIconBean bean) {
                // 重写此方法来衡量每张图片的大小，默认返回图片数量。
                int size = 0;
                Bitmap iconBitmap = bean.getIconBitmap();
                Bitmap blurBgBitmap = bean.getBlurBgBitmap();
                if (iconBitmap != null) {
                    size += iconBitmap.getByteCount();
                }
                if (blurBgBitmap != null) {
                    size += blurBgBitmap.getByteCount();
                }

                //LetvLog.d(TAG, "sizeOf size/1024 = " + size / 1024);
                return size / 1024;
            }

            @Override
            protected void entryRemoved(boolean evicted, Long key, AppIconBean oldValue, AppIconBean newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
                //LetvLog.d(TAG, "entryRemoved key = " + key + " evicted = " + evicted + " oldValue = " + oldValue);
                if(isRelease){
                    if (evicted && oldValue != null) {
                        Bitmap iconBitmap = oldValue.getIconBitmap();
                        if (iconBitmap != null) {
                            iconBitmap.recycle();
                        }
                        Bitmap blurBitmap = oldValue.getBlurBgBitmap();
                        if (blurBitmap != null) {
                            blurBitmap.recycle();
                        }
                    }
                }

            }
        };
    }

    public void addBitmapToMemoryCache(long key, AppIconBean bean) {
        if (getBitmapFromMemCache(key) == null) {
            try {
                mMemoryCache.put(key, bean);
            } catch (Exception ex) {
                LetvLog.d(TAG, "addBitmapToMemoryCache key = " + key + " bean = " + bean + " getSize = " + getSize(), ex);
            }
        }
    }

    public AppIconBean getBitmapFromMemCache(long key) {
        return mMemoryCache.get(key);
    }

    /**
     * @return 已经存储的大小
     */
    public int getSize() {
        return mMemoryCache.size();
    }

    /**
     * @return 最大存储空间
     */
    public int getMaxSize() {
        return mMemoryCache.maxSize();
    }

    /**
     * @return 命中的次数
     */
    public int getHitCount() {
        return mMemoryCache.hitCount();
    }

    /**
     * @return 丢失的次数
     */
    public int getMissCount() {
        return mMemoryCache.missCount();
    }

    public void releaseCache(){
        LetvLog.d(TAG, "releaseCache size = " + getSize());
        try {
            mMemoryCache.evictAll();
        }catch (Exception ex){
            LetvLog.e(TAG, "releaseCache error", ex);
        }

    }
}
