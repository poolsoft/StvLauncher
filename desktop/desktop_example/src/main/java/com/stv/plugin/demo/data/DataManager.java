
package com.stv.plugin.demo.data;

import android.os.Message;

import com.google.gson.Gson;
import com.stv.plugin.demo.DemoApplication;
import com.stv.plugin.demo.data.common.OnDataChangedListener;
import com.stv.plugin.demo.data.common.PosterHolder;
import com.stv.plugin.demo.util.LooperTaskSupplier;
import com.xstv.library.base.Logger;
import com.xstv.library.base.WeakHandler;
import com.xstv.library.base.http.HttpCallback;
import com.xstv.library.base.http.HttpManager;
import com.xstv.library.base.http.HttpNameValuePair;
import com.xstv.library.base.http.HttpRequest;
import com.xstv.library.base.http.HttpResponse;
import com.xstv.library.base.http.HttpURL;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataManager {

    private Logger mLogger = Logger.getLogger(DemoApplication.PLUGINTAG, "DataManager");

    private volatile static DataManager sDataManager;

    private Gson mGson = new Gson();
    private LocalDataReader mDataReader;
    private HttpRequest mLastHttpRequest;
    private Object mDataLock = new Object();
    private PosterHolder mMemoryPosterHolder;
    private LooperTaskSupplier mLooperTaskSupplier;
    private OnDataChangedListener mDataChangedListener;

    private int mFactor;
    private int mImgClassIndex = 0;
    private boolean mTimerRunning;
    private boolean mHasCallbackDefaultData;
    private int sRefreshThreshold = 180;

    private DataManager() {
        /**
         * for test
         */
        debugRefreshTime();

        mDataReader = new LocalDataReader();
        mLooperTaskSupplier = new LooperTaskSupplier();

        verifyDefaultData();
    }

    public static DataManager getInstance() {
        if (sDataManager == null) {
            synchronized (DataManager.class) {
                if (sDataManager == null) {
                    sDataManager = new DataManager();
                }
            }
        }
        return sDataManager;
    }

    private WeakHandler<DataManager> mHandler = new WeakHandler<DataManager>(DataManager.this) {

        @Override
        protected void weakReferenceMessage(Message message) {
            if (!mTimerRunning) {
                return;
            }

            mFactor++;

            /**
             * Only for test: 通知UI距离刷新还有多久
             */
            notifyDataRefreshTime();

            /**
             * 通知刷新
             */
            if (mFactor >= sRefreshThreshold) {
                mFactor = -1;
                //refreshData();
            }
            sendEmptyMessageDelayed(0, 1000);
        }
    };

    public void startTimer2RefreshData() {
        if (!mTimerRunning) {
            mFactor = 0;
            mTimerRunning = true;
            mHandler.sendEmptyMessageDelayed(0, 1000 * 10);
        }
    }

    public void stopTimer2RefreshData() {
        mHandler.removeMessages(0);
        mTimerRunning = false;
        mFactor = 0;
    }

    public void setOnDataChangedListener(OnDataChangedListener l) {
        if (l != null) {
            mDataChangedListener = l;

            /**
             * Callback cache data if it has not callback before.
             */
            if (!mHasCallbackDefaultData) {
                notifyDataInitialize();
            }
        } else {
            mDataChangedListener = null;
            mHasCallbackDefaultData = false;
            stopTimer2RefreshData();
        }
    }

    /**
     * 校验本地缓存数据:
     * 1、如果SDCard中数据为空，则将Assets下默认数据迁移到sdcard中；
     * 2、如果SDCard中数据不为空，则比对Assets和SDCard的数据的版本，确保SDCard中数据为最新
     */
    private void verifyDefaultData() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                PosterHolder defaultHolder = mDataReader.verifyDefaultData();
                updateMemoryCache(defaultHolder);
                Runnable update = new Runnable() {
                    @Override
                    public void run() {
                        if (!mHasCallbackDefaultData) {
                            notifyDataInitialize();
                        }
                    }
                };
                mHandler.post(update);
            }
        };
        mLooperTaskSupplier.getLocalIOHandler().post(r);
    }

    private void updateMemoryCache(PosterHolder posterHolder) {
        synchronized (mDataLock) {
            mMemoryPosterHolder = posterHolder;
        }
    }

    private void refreshData() {
        mLogger.d("request refreshData");
        requestDataFromHttp();
    }

    private void updateDataCache(final PosterHolder holder) {
        mLogger.d("updateDataCache");
        if (holder != null && holder.data != null) {
            updateMemoryCache(holder);
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    mDataReader.updateLocalDataCache(holder);
                }
            };
            mLooperTaskSupplier.getLocalIOHandler().post(r);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyDataChanged();
                }
            });
        }
    }

    private void notifyDataRefreshTime() {
        if (mDataChangedListener != null) {
            mDataChangedListener.onRefreshTimeLess(sRefreshThreshold - mFactor);
        }
    }

    private void notifyDataInitialize() {
        if (mMemoryPosterHolder != null && mMemoryPosterHolder.isValid()) {
            if (mDataChangedListener != null) {
                mHasCallbackDefaultData = true;
                mDataChangedListener.onDataInitialize(mMemoryPosterHolder);
            }
        }
    }

    private void notifyDataChanged() {
        if (mMemoryPosterHolder != null && mMemoryPosterHolder.isValid()) {
            if (mDataChangedListener != null) {
                mDataChangedListener.onDataChange(mMemoryPosterHolder);
            }
        }
    }

    private void requestDataFromHttp() {
        if (mImgClassIndex > 3) {
            mImgClassIndex = 0;
        }
        if (mLastHttpRequest != null) {
            HttpManager.cancel(mLastHttpRequest);
        }


        String url = "http://image.baidu.com/channel/listjson";
        List<HttpNameValuePair> params = new ArrayList<HttpNameValuePair>();
        params.add(new HttpNameValuePair("pn", "10"));
        params.add(new HttpNameValuePair("rn", "100"));
        switch (mImgClassIndex) {
            case 0:
                params.add(new HttpNameValuePair("tag1", "壁纸"));
                params.add(new HttpNameValuePair("tag2", "全部"));
                break;
            case 1:
                params.add(new HttpNameValuePair("tag1", "宠物"));
                params.add(new HttpNameValuePair("tag2", "全部"));
                break;
            case 2:
                params.add(new HttpNameValuePair("tag1", "明星"));
                params.add(new HttpNameValuePair("tag2", "全部"));
                break;
            case 3:
                params.add(new HttpNameValuePair("tag1", "动漫"));
                params.add(new HttpNameValuePair("tag2", "全部"));
                break;
            case 4:
                params.add(new HttpNameValuePair("tag1", "美女"));
                params.add(new HttpNameValuePair("tag2", "全部"));
                break;
        }
        params.add(new HttpNameValuePair("ie", "utf8"));
        HttpURL.Builder builder = new HttpURL.Builder();
        builder.scheme("https").host("image.baidu.com").pathFragment("channel")
                .pathFragment("listjson").queryParameters(params);
        HttpRequest request = new HttpRequest.Builder().url(builder.build().getUrl()).tag(DemoApplication.PLUGINTAG).build();
        HttpManager.enqueue(request, new MyHttpCallback());

        HttpManager.enqueue(createHttpsRequest(), new HttpsCallback());

        mLastHttpRequest = request;
        mImgClassIndex++;
    }

    private HttpRequest createHttpsRequest() {
        addCertificate();
        String url = "https://kyfw.12306.cn/otn/leftTicket/init";
        HttpRequest.Builder builder = new HttpRequest.Builder();
        builder.url(url).tag(DemoApplication.PLUGINTAG);
        return builder.build();
    }

    private boolean certificateAdded;
    private void addCertificate() {
        if (certificateAdded) {
            return;
        }
        try {
            InputStream is = DemoApplication.sWidgetApplicationContext.getAssets().open("srca.cer");
            boolean ret = HttpManager.addCertificates(is);
            if (ret) {
                certificateAdded = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class MyHttpCallback extends HttpCallback {

        @Override
        public void onHttpResponse(HttpResponse httpResponse) throws IOException {
            if (httpResponse != null && httpResponse.isSuccessful() && mTimerRunning) {
                try {
                    PosterHolder posterHolder = mGson.fromJson(httpResponse.body().string(), PosterHolder.class);
                    mLogger.d("onHttpResponse -> " + (posterHolder != null));
                    if (posterHolder != null) {
                        posterHolder.checkValid();
                        if (posterHolder.data != null) {
                            /** 测试: 随机打乱顺序 */
                            Collections.shuffle(posterHolder.data);
                        }
                        updateDataCache(posterHolder);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onHttpFailure(HttpRequest httpRequest, IOException e) {
            mLogger.e("onHttpFailure httpRequest=" + httpRequest + " e=" + e.getMessage());
        }
    }

    private class HttpsCallback extends HttpCallback {
        @Override
        public void onHttpFailure(HttpRequest request, IOException e) {
            mLogger.d("HttpsCallback onHttpFailure" + "e=" + e.getMessage());
        }

        @Override
        public void onHttpResponse(HttpResponse response) throws IOException {
            mLogger.d("HttpsCallback onHttpResponse" + response.code());
            String res = response.body().string();
            mLogger.d(res.substring(0, 100));

        }
    };


    public void destroy() {
        mLooperTaskSupplier.quit();
        if (mLastHttpRequest != null) {
            HttpManager.cancel(mLastHttpRequest);
        }
        sDataManager = null;
        mMemoryPosterHolder = null;
        mDataChangedListener = null;
        mLooperTaskSupplier = null;
    }


    /**
     * 随机生成一个定时刷新值
     */
    private void debugRefreshTime() {
        sRefreshThreshold = 10;
    }

    public int getRefreshThreshold() {
        return sRefreshThreshold;
    }
}
