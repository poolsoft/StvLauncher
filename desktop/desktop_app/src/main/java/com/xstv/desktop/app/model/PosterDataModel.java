
package com.xstv.desktop.app.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.xstv.library.base.LetvLog;
import com.xstv.desktop.app.AppPluginActivator;
import com.xstv.desktop.app.bean.ContentBean;
import com.xstv.desktop.app.bean.ContentPosBean;
import com.xstv.desktop.app.bean.JumpParamBean;
import com.xstv.desktop.app.bean.ParamBean;
import com.xstv.desktop.app.bean.PosterInfo;
import com.xstv.desktop.app.bean.QqParamBean;
import com.xstv.desktop.app.db.ItemInfo;
import com.xstv.desktop.app.interfaces.PosterDataModelCallback;
import com.xstv.desktop.app.util.JsonUtil;
import com.xstv.desktop.app.util.UrlGenerator;
import com.xstv.desktop.app.util.Utilities;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PosterDataModel {
    private static final String TAG = PosterDataModel.class.getSimpleName();

    private static final String PREFERENCE_NAME = "poster_cache_data";

    private PosterDataModelCallback mPosterDataModelCallback;

    private PosterDataModel() {
    }

    public static PosterDataModel getInstance() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        private static final PosterDataModel sInstance = new PosterDataModel();
    }

    public void setCallback(PosterDataModelCallback posterDataModelCallback) {
        this.mPosterDataModelCallback = posterDataModelCallback;
    }

    public void fetchPosterData(final boolean isUpdate) {
        String url = UrlGenerator.getPosidStr();
        LetvLog.i(TAG, "fetchPosterData url = " + url);
        final Type type = new TypeToken<ContentPosBean>() {
        }.getType();
        loadDataFromServer(url, type, new ResultCallback<ContentPosBean>() {
            @Override
            public void requestSuccess(ContentPosBean contentPosBean, String jsonStr, int responseCode) {
                LetvLog.i(TAG, "fetchPosterData requestSuccess");
                if (contentPosBean.errno != UrlGenerator.POSID_SUCCESS_CODE || contentPosBean.data == null || contentPosBean.data.size() == 0) {
                    // 下发到的数据有错误
                    Log.e(TAG, "fetchPosterData errorno = " + contentPosBean.errno);
                    onFailure();
                } else {
                    handlePosterData(contentPosBean, isUpdate);
                    //备份数据
                    backupDataToLocal(PREFERENCE_NAME, jsonStr);
                }
            }

            @Override
            public void onFailure() {
                LetvLog.i(TAG, "fetchPosterData onFailure");
                if (!isUpdate) {
                    handleFailureData();
                }
            }
        });
    }

    private void handlePosterData(ContentPosBean contentPosBean, boolean isUpdate) {
        Map<String, List<ContentPosBean.Item>> data = contentPosBean.data;
        if (data == null) {
            LetvLog.i(TAG, "handlePosterData data is null.");
            return;
        }
        List<ContentBean> contentBeanList = new ArrayList<ContentBean>(2);
        for (Map.Entry<String, List<ContentPosBean.Item>> pair : data.entrySet()) {
            String key = pair.getKey();
            LetvLog.d(TAG, "handlePosterData key = " + key);
            List<ContentPosBean.Item> itemList = pair.getValue();
            ContentBean contentBean = new ContentBean();
            contentBeanList.add(contentBean);
            if (itemList != null && itemList.size() != 0) {
                List<ItemInfo> posterInfoList = new ArrayList<ItemInfo>();
                contentBean.setContentItemList(posterInfoList);
                for (ContentPosBean.Item item : itemList) {
                    PosterInfo posterInfo = new PosterInfo();
                    posterInfo.setPosid(key);
                    if (item != null) {
                        ContentPosBean.Item.Promotion promotion = item.promotion;
                        if (promotion != null) {
                            posterInfo.setReqid(promotion.reqid);
                            posterInfo.setPromoid(promotion.promoid);
                            ContentPosBean.Item.Promotion.Creative creative = promotion.creative;
                            if (creative != null) {
                                String id = creative.id;
                                posterInfo.setPosterId(id);
                                ContentPosBean.Item.Promotion.Creative.Material material = creative.material;
                                posterInfo.setPackageName(material.packagename);
                                posterInfo.setClassName(material.activity);
                                posterInfo.setIconUrl(material.picAD);
                                posterInfo.setLogoUrl(material.logoAD);
                                posterInfo.setFirstTitle(material.firsttitle);
                                posterInfo.setTitle(material.firsttitle);
                                posterInfo.setSecondTitle(material.secondtitle);

                                ContentPosBean.Item.Promotion.Creative.Jump jump = creative.jump;
                                if (jump != null) {
                                    ParamBean jumpParamBean = parsJumpParam(jump.jumpAD);
                                    posterInfo.setJumpParam(jumpParamBean);
                                    posterInfo.setPackageName(jumpParamBean.jumpPackage);
                                    posterInfo.setTitle(jumpParamBean.jumpName);
                                }
                                LetvLog.d(TAG, "handlePosterData posterInfo = " + posterInfo + " id = " + id);
                            }
                        }
                    }
                    posterInfoList.add(posterInfo);
                }
            }
        }

        // 数据准备完成,通知更新
        if (mPosterDataModelCallback != null) {
            mPosterDataModelCallback.onServerData(contentBeanList, isUpdate);
        }
    }

    private void handleFailureData() {
        ContentPosBean contentPosBean = getLocalData();
        if (contentPosBean == null) {
            return;
        }
        handlePosterData(contentPosBean, false);
    }

    private ContentPosBean getLocalData() {
        String jsonStr = recoverDataFromLocal(PREFERENCE_NAME);
        Type type = new TypeToken<ContentPosBean>() {
        }.getType();
        return JsonUtil.jsonToObject(jsonStr, type);
    }


    private ParamBean parsJumpParam(String jumpAd) {
        LetvLog.d(TAG, "parsJumpParam jumpAd = " + jumpAd);
        ParamBean paramBean = null;
        if (TextUtils.isEmpty(jumpAd)) {
            return paramBean;
        }

        Type type = new TypeToken<JumpParamBean>() {
        }.getType();
        try {
            JumpParamBean jumpParamBean = JsonUtil.jsonToObject(jumpAd, type);
            if (jumpParamBean != null) {
                JumpParamBean.ParamValue paramValue = jumpParamBean.paramValue;
                paramBean = new ParamBean();
                if (paramValue != null) {
                    String params = paramValue.params;
                    // LetvLog.d(TAG, "parsJumpParam params = " + params);
                    if (!TextUtils.isEmpty(params)) {
                        JSONObject obj = new JSONObject(params);
                        int type1 = obj.getInt("type");
                        String action1 = obj.getString("action");
                        JSONObject valueObj = obj.getJSONObject("value");
                        // String packageName = valueObj.getString("packageName");
                        // int callType = obj.getInt("callType");

                        // LetvLog.d(TAG, "parsJumpParam type1 = " + type1 + " action1 = " + action1 +
                        // " valueObj = " + valueObj.toString());
                        paramBean.type = type1;
                        paramBean.action = action1;
                        paramBean.value = valueObj.toString();
                        // paramBean.callType = callType;
                    }
                }
                // String paramType = jumpParamBean.paramType;
                // paramBean.paramType = paramType;

                JumpParamBean.JumpDetect jumpDetect = jumpParamBean.jumpDetect;
                if (jumpDetect != null) {
                    String jumpName = jumpDetect.jumpName;
                    String jumpPackage = jumpDetect.jumpPackage;
                    String jumpVersion = jumpDetect.jumpVersion;
                    paramBean.jumpName = jumpName;
                    paramBean.jumpPackage = jumpPackage;
                    paramBean.jumpVersion = jumpVersion;

                    if (Utilities.QQ_PACKAGE_NAME.equals(jumpPackage)) {
                        String value1 = paramBean.value;
                        if (!TextUtils.isEmpty(value1)) {
                            Type qqType = new TypeToken<QqParamBean>() {
                            }.getType();
                            QqParamBean qqParamBean = JsonUtil.jsonToObject(value1, qqType);
                            paramBean.qqParamBean = qqParamBean;
                            // LetvLog.d(TAG, "parsJumpParam qq = " + qqParamBean.m0 + " m1 = " + qqParamBean.m1);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.w(TAG, "parsJumpParam excption !!!", ex);
        }
        return paramBean;
    }

    public void crush() {
    }

    private boolean backupDataToLocal(String key, String jsonStr) {
        Context context = AppPluginActivator.getContext();
        SharedPreferences preference = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putString(key, jsonStr);
        return editor.commit();
    }

    private String recoverDataFromLocal(String key) {
        Context context = AppPluginActivator.getContext();
        SharedPreferences preference = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return preference.getString(key, null);
    }

    private <T> void loadDataFromServer(String url, Type type, ResultCallback<T> resultCallback) {
        // 没有网络
        if (!Utilities.isNetworkConnected(AppPluginActivator.getContext())) {
            LetvLog.i(TAG, "loadDataFromServerWithOldApi no network load data from local.");
            if (resultCallback != null) {
                resultCallback.onFailure();
            }
            return;
        }

        if (Utilities.isUserHttpManager()) {
            loadDataFromServerInNewApi(url, type, resultCallback);
        } else {
            loadDataFromServerInOldApi(url, type, resultCallback);
        }
    }

    private <T> void loadDataFromServerInNewApi(String url, final Type type, final ResultCallback<T> resultCallback) {
        /*String date = SignatureUtil.getDateHeader();
        final HttpRequest request = new HttpRequest.Builder().url(url)
                .tag(AppPluginActivator.getContext().getPackageName())
                .header("User-Agent", AppApplication.sHttpUserAgent)
                .addHeader("Date", date)
                .build();
        HttpManager.enqueue(request, new HttpCallback() {
            @Override
            public void onHttpResponse(HttpResponse httpResponse) throws IOException {
                LetvLog.i(TAG, "loadDataFromServerInNewApi response code = " + httpResponse.code());
                if (httpResponse.isSuccessful()) {
                    String jsonStr = httpResponse.body().string();
                    // LetvLog.d(TAG, "onHttpResponse jsonStr = " + jsonStr);
                    T t = JsonUtil.jsonToObject(jsonStr, type);
                    if (t == null) {
                        Log.e(TAG, "onHttpResponse json parse error.");
                        if (resultCallback != null) {
                            resultCallback.onFailure();
                        }
                    } else {
                        if (resultCallback != null) {
                            resultCallback.requestSuccess(t, jsonStr, httpResponse.code());
                        }
                    }
                } else {// 状态码不是200~300
                    if (resultCallback != null) {
                        resultCallback.onFailure();
                    }
                }
            }

            @Override
            public void onHttpFailure(HttpRequest httpRequest, IOException e) {
                LetvLog.w(TAG, " loadDataFromServerInNewApi onFailure request = " + request, e);
                if (resultCallback != null) {
                    resultCallback.onFailure();
                }
            }
        });*/
    }

    private <T> void loadDataFromServerInOldApi(String url, final Type type, final ResultCallback<T> resultCallback) {
        /*String date = SignatureUtil.getDateHeader();
        final Request request = new Request.Builder().url(url)
                .tag(AppPluginActivator.getContext().getPackageName())
                .header("User-Agent", AppApplication.sHttpUserAgent)
                .addHeader("Date", date)
                .build();
        OkHttpUtil.enqueue(request, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                LetvLog.i(TAG, "loadDataFromServerWithOldApi response code = " + response.code());
                if (response.isSuccessful()) {
                    String jsonStr = response.body().string();
                    // LetvLog.d(TAG, "onHttpResponse jsonStr = " + jsonStr);
                    T t = JsonUtil.jsonToObject(jsonStr, type);
                    if (t == null) {
                        Log.e(TAG, "onResponse json parse error.");
                        if (resultCallback != null) {
                            resultCallback.onFailure();
                        }
                    } else {
                        if (resultCallback != null) {
                            resultCallback.requestSuccess(t, jsonStr, response.code());
                        }
                    }
                } else {// 状态码不是200~300
                    if (resultCallback != null) {
                        resultCallback.onFailure();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                LetvLog.w(TAG, " loadDataFromServerWithOldApi onFailure request = " + request, e);
                if (resultCallback != null) {
                    resultCallback.onFailure();
                }
            }
        });*/
    }

    private interface ResultCallback<T> {
        void requestSuccess(T t, String jsonStr, int responseCode);

        void onFailure();
    }
}
