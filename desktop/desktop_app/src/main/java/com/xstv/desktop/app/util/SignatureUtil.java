
package com.xstv.desktop.app.util;

import com.xstv.library.base.LetvLog;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by panfeng on 16-3-2.
 */
public class SignatureUtil {
    private static final char HEX_DIGITS[] = "0123456789abcdef".toCharArray();

    protected static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(HEX_DIGITS[(b & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b & 0x0f]);
        }
        return sb.toString();
    }

    protected static String join(Iterable<String> strings, String sep) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : strings) {
            if (first)
                first = false;
            else
                sb.append(sep);
            sb.append(item);
        }
        return sb.toString();
    }

    public static String sign(String key, String method, String path, byte[] body, String date, Map<String, String> params) {
        try {
            String bodyMD5 = "";
            if (body != null && body.length != 0) {
                MessageDigest digest;
                digest = MessageDigest.getInstance("MD5");
                digest.update(body);
                bodyMD5 = toHexString(digest.digest());
            }
            String paramString = "";
            if (params != null) {
                SortedSet<String> set = new TreeSet<String>();
                for (String k : params.keySet()) {
                    String values = params.get(k);
                    set.add(k + "=" + values);
                }
                paramString = join(set, "&");
            }
            String stringToSign = method + "\n" + path + "\n" + bodyMD5 + "\n" + date + "\n" + paramString;
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(stringToSign.getBytes());
            return toHexString(rawHmac);
        } catch (NoSuchAlgorithmException e) {
            LetvLog.d("NoSuchAlgorithmException " + e.getMessage());
        } catch (InvalidKeyException e) {
            LetvLog.d("InvalidKeyException " + e.getMessage());
        }
        return "";
    }

    public static Map<String, String> getSignHeader(String api, String url, String secretkey, String akey) {
        if (secretkey == null || secretkey.isEmpty()) {
            return null;
        }
        if (akey == null || akey.isEmpty()) {
            return null;
        }
        Map paramMap = new HashMap();
        if (url.contains("&")) {
            String params = url.substring(url.indexOf("?") + 1);
            String paramsList[] = params.split("&");
            if (paramsList != null && paramsList.length > 0) {
                for (int i = 0; i < paramsList.length; i++) {
                    String valueKey[] = paramsList[i].split("=");
                    if (valueKey.length == 2) {
                        paramMap.put(valueKey[0], valueKey[1]);
                        LetvLog.d("value " + valueKey[0] + " key: " + valueKey[1]);
                    }
                }
            }
        } else {
            paramMap = null;
        }
        Date date = new Date();
        SimpleDateFormat fm = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss 'GMT'", Locale.US);
        String strDate = fm.format(date);
        String sig = SignatureUtil.sign(secretkey, "GET", api, null, strDate, paramMap);
        LetvLog.d("sig " + sig);
        String authorization = String.format("LETV %s %s", akey, sig);
        Map<String, String> headerMap = new HashMap<String, String>();
        headerMap.put("Date", strDate);
        headerMap.put("Authorization", authorization);
        LetvLog.d("authorization " + authorization);
        return headerMap;
    }

    public static String getDateHeader() {
        Date date = new Date();
        SimpleDateFormat fm = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss 'GMT'", Locale.US);
        String strDate = fm.format(date);
        LetvLog.d("strDate: " + strDate);
        return strDate;
    }

    public static String getAuthorizationHeader(String api, String url, String secretkey, String akey, String strDate) {
        if (secretkey == null || secretkey.isEmpty()) {
            return "";
        }
        if (akey == null || akey.isEmpty()) {
            return "";
        }
        LetvLog.d("url " + url);
        Map paramMap = new HashMap();
        if (url.contains("&")) {
            String params = url.substring(url.indexOf("?") + 1);
            String paramsList[] = params.split("&");
            if (paramsList != null && paramsList.length > 0) {
                for (int i = 0; i < paramsList.length; i++) {
                    String valueKey[] = paramsList[i].split("=");
                    if (valueKey.length == 2) {
                        paramMap.put(valueKey[0], valueKey[1]);
                        LetvLog.d("value " + valueKey[0] + " key: " + valueKey[1]);
                    }
                }
            }
        } else {
            paramMap = null;
        }
        //Date date = new Date();
        //SimpleDateFormat fm = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss 'GMT'", Locale.US);
        //String strDate = fm.format(date);
        String sig = SignatureUtil.sign(secretkey, "GET", api, null, strDate, paramMap);
        String authorization = String.format("LETV %s %s", akey, sig);
        return authorization;
    }
}
