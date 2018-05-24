package com.xstv.library.base.http;


import com.xstv.library.base.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import static com.xstv.library.base.http.HttpManager.SDK_HTTP_LOG_TAG;

class CertificateUtil {

    private static final String TAG = "CertificateUtil";

    private static ArrayList<X509Certificate> sCertificates;

    private static SSLSocketFactory sSslSocketFactory;

    boolean addCertificates(InputStream... is) {
        if (sCertificates == null) {
            sCertificates = new ArrayList<X509Certificate>();
        }
        try {
            KeyStore sKeyStore = newEmptyKeyStore();
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            for (InputStream in : is) {
                X509Certificate ret = (X509Certificate) certificateFactory.generateCertificate(in);
                sCertificates.add(ret);
                in.close();
            }
            if (sCertificates.isEmpty()) {
                throw new IllegalArgumentException("expected non-empty set of trusted certificates");
            }
            for (int i = 0; i < sCertificates.size(); i++) {
                Certificate c = sCertificates.get(i);
                String certificateAlias = Integer.toString(i);
                sKeyStore.setCertificateEntry(certificateAlias, c);
            }

            // Use it to build an X509 trust manager.
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(sKeyStore, null);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(sKeyStore);

            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, new SecureRandom());
            sSslSocketFactory = sslContext.getSocketFactory();

            return true;
        } catch (KeyStoreException e) {
            Logger.getLogger(SDK_HTTP_LOG_TAG, TAG).d("KeyStoreException", e);
        } catch (CertificateException e) {
            Logger.getLogger(SDK_HTTP_LOG_TAG, TAG).d("CertificateException", e);
        } catch (IOException e) {
            Logger.getLogger(SDK_HTTP_LOG_TAG, TAG).d("IOException", e);
        } catch (UnrecoverableKeyException e) {
            Logger.getLogger(SDK_HTTP_LOG_TAG, TAG).d("UnrecoverableKeyException", e);
        } catch (NoSuchAlgorithmException e) {
            Logger.getLogger(SDK_HTTP_LOG_TAG, TAG).d("NoSuchAlgorithmException", e);
        } catch (KeyManagementException e) {
            Logger.getLogger(SDK_HTTP_LOG_TAG, TAG).d("KeyManagementException", e);
        } finally {
            for (InputStream i : is) {
                try {
                    i.close();
                } catch (Exception e) {
                    Logger.getLogger(SDK_HTTP_LOG_TAG, TAG).d("close", e);
                }
            }
        }
        return false;
    }

    SSLSocketFactory getSSLSocketFactory() {
        if (sSslSocketFactory == null) {
            throw new RuntimeException("TrustManager has not been init");
        }
        return sSslSocketFactory;
    }

    private KeyStore newEmptyKeyStore() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            return keyStore;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

}
