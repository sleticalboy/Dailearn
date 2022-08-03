package com.binlee.learning.http.url;

import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import okhttp3.Handshake;
import okhttp3.OkHttpClient;

/**
 * Created on 18-10-5.
 *
 * @author leebin
 */
final class OkHttpsUrlConnection extends DelegateHttpsUrlConnection {

  private final OkHttpUrlConnection delegate;

  public OkHttpsUrlConnection(URL url, OkHttpClient client) {
    this(new OkHttpUrlConnection(url, client));
  }

  public OkHttpsUrlConnection(OkHttpUrlConnection delegate) {
    super(delegate);
    this.delegate = delegate;
  }

  @Override
  protected Handshake handShake() {
    if (delegate.mCall == null) {
      throw new IllegalStateException("Connection has not yet been established");
    }
    return delegate.mHandshake;
  }

  @Override
  public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
    delegate.mClient = delegate.mClient.newBuilder()
      .hostnameVerifier(hostnameVerifier)
      .build();
  }

  @Override
  public HostnameVerifier getHostnameVerifier() {
    return delegate.mClient.hostnameVerifier();
  }

  @Override
  public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
    if (sslSocketFactory == null) {
      throw new IllegalArgumentException("sslSocketFactory == null");
    }
    // This fails in JDK 9 because OkHttp is unable to extract the trust manager.
    delegate.mClient = delegate.mClient.newBuilder()
      .sslSocketFactory(sslSocketFactory)
      .build();
  }

  @Override
  public SSLSocketFactory getSSLSocketFactory() {
    return delegate.mClient.sslSocketFactory();
  }
}
