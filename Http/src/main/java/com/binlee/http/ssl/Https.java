package com.binlee.http.ssl;

import android.annotation.SuppressLint;
import com.binlee.http.OkUtils;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created on 18-9-15.
 * </br>
 * from <a href='https://github.com/hongyangAndroid/okhttputils/blob/master/
 * okhttputils/src/main/java/com/zhy/http/okhttp/https/HttpsUtils.java'>张鸿洋</a>
 *
 * @author 张鸿洋
 * @author leebin
 */
public final class Https {

  private static final HostnameVerifier TRUST_ALL = (hostname, session) -> true;
  private static final X509TrustManager UNSAFE_TRUST_MANAGER = new X509TrustManager() {
    @SuppressLint("TrustAllX509TrustManager")
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    @SuppressLint("TrustAllX509TrustManager")
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new java.security.cert.X509Certificate[] {};
    }
  };

  public static HostnameVerifier trustAll() {
    return TRUST_ALL;
  }

  public static X509TrustManager unsafeTrustManager() {
    return UNSAFE_TRUST_MANAGER;
  }

  // public static SSLParams getSSLParams(InputStream[] certificates, InputStream bksFile, String password) {
  public static SSLParams getSSLParams(InputStream certificate, String caAlias) {
    final SSLParams sslParams = new SSLParams();
    try {
      // 鸿洋的
      // TrustManager[] trustManagers = prepareTrustManager(certificates);
      // 敏行实现
      TrustManager[] trustManagers = prepareTrustManager(certificate, caAlias);

      // KeyManager
      // KeyManager[] keyManagers = prepareKeyManager(bksFile, password);
      SSLContext sslContext = SSLContext.getInstance("TLS");
      X509TrustManager trustManager;
      if (trustManagers != null) {
        trustManager = new InternalTrustManager(chooseTrustManager(trustManagers));
      } else {
        trustManager = unsafeTrustManager();
      }
      sslContext.init(/*keyManagers*/null, new TrustManager[] { trustManager }, null);
      sslParams.sslSocketFactory = sslContext.getSocketFactory();
      sslParams.trustManager = trustManager;
      return sslParams;
    } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
      throw new AssertionError(e);
    }
  }

  // 敏行实现
  private static TrustManager[] prepareTrustManager(InputStream caInput, String caAlias) {
    if (caInput == null) return null;
    try {
      final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
      final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(null);
      keyStore.setCertificateEntry(caAlias, certificateFactory.generateCertificate(caInput));
      OkUtils.closeSilently(caInput);
      final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
        TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(keyStore);
      return trustManagerFactory.getTrustManagers();
    } catch (Exception e) {
      return null;
    }
  }

  // 鸿洋的
  // @SuppressWarnings("unused")
  // private static TrustManager[] prepareTrustManager(InputStream... certificates) {
  //     if (certificates == null || certificates.length <= 0) return null;
  //     try {
  //         CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
  //         KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
  //         keyStore.load(null);
  //         int index = 0;
  //         for (InputStream certificate : certificates) {
  //             String certificateAlias = Integer.toString(index++);
  //             keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
  //             OkUtils.closeSilently(certificate);
  //         }
  //         final TrustManagerFactory trustManagerFactory = TrustManagerFactory.
  //                 getInstance(TrustManagerFactory.getDefaultAlgorithm());
  //         trustManagerFactory.init(keyStore);
  //         return trustManagerFactory.getTrustManagers();
  //     } catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException ignored) {
  //     }
  //     return null;
  // }
  //
  // @SuppressWarnings("unused")
  // private static KeyManager[] prepareKeyManager(InputStream bksFile, String password) {
  //     try {
  //         if (bksFile == null || password == null) return null;
  //         KeyStore clientKeyStore = KeyStore.getInstance("BKS");
  //         clientKeyStore.load(bksFile, password.toCharArray());
  //         KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
  //         keyManagerFactory.init(clientKeyStore, password.toCharArray());
  //         return keyManagerFactory.getKeyManagers();
  //     } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | IOException ignored) {
  //     }
  //     return null;
  // }

  private static X509TrustManager chooseTrustManager(TrustManager[] trustManagers) {
    for (TrustManager trustManager : trustManagers) {
      if (trustManager instanceof X509TrustManager) {
        return (X509TrustManager) trustManager;
      }
    }
    return null;
  }

  private static class InternalTrustManager implements X509TrustManager {

    private final X509TrustManager defaultTrustManager;
    private final X509TrustManager localTrustManager;

    InternalTrustManager(X509TrustManager localTrustManager) throws NoSuchAlgorithmException, KeyStoreException {
      TrustManagerFactory var4 = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      var4.init((KeyStore) null);
      defaultTrustManager = chooseTrustManager(var4.getTrustManagers());
      this.localTrustManager = localTrustManager;
    }

    @SuppressLint("TrustAllX509TrustManager")
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
      try {
        defaultTrustManager.checkServerTrusted(chain, authType);
      } catch (CertificateException ce) {
        localTrustManager.checkServerTrusted(chain, authType);
      }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }
  }

  public static class SSLParams {
    public SSLSocketFactory sslSocketFactory;
    public X509TrustManager trustManager;
  }
}

