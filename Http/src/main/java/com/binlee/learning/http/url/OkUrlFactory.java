package com.binlee.learning.http.url;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import okhttp3.OkHttpClient;

/**
 * Created on 18-10-5.
 *
 * @author leebin
 */
public final class OkUrlFactory implements URLStreamHandlerFactory, Cloneable {

  private final OkHttpClient mClient;

  //    public OkUrlFactory() {
  //        this(HttpClient.getInstance().getOkHttpClient());
  //    }

  public OkUrlFactory(OkHttpClient client) {
    mClient = client;
  }

  public OkHttpClient getClient() {
    return mClient;
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    return new OkUrlFactory(mClient);
  }

  @Override
  public URLStreamHandler createURLStreamHandler(String protocol) {
    if (!"http".equals(protocol) && !"https".equals(protocol)) return null;
    return new URLStreamHandler() {

      @Override
      protected URLConnection openConnection(URL url) throws IOException {
        return open(url);
      }

      @Override
      protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        return open(url, proxy);
      }

      @Override
      protected int getDefaultPort() {
        if ("http".equals(protocol)) return 80;
        if ("https".equals(protocol)) return 443;
        throw new AssertionError();
      }
    };
  }

  public URLConnection open(URL url) {
    return open(url, mClient.proxy());
  }

  private URLConnection open(URL url, Proxy proxy) {
    final OkHttpClient copy = mClient.newBuilder()
      .proxy(proxy)
      .build();
    final String protocol = url.getProtocol();
    if ("http".equals(protocol)) return new OkHttpUrlConnection(url, copy);
    if ("https".equals(protocol)) return new OkHttpsUrlConnection(url, copy);
    throw new IllegalArgumentException("Unexpected protocol: " + protocol);
  }
}
