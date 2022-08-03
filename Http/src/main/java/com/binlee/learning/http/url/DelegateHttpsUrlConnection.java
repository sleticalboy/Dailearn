package com.binlee.learning.http.url;

import android.annotation.TargetApi;
import android.os.Build;
import androidx.annotation.RequiresApi;
import com.binlee.learning.http.OkUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.Permission;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import okhttp3.Handshake;

/**
 * Created on 18-10-5.
 *
 * @author leebin
 */
abstract class DelegateHttpsUrlConnection extends HttpsURLConnection {

  private final HttpURLConnection delegate;

  protected DelegateHttpsUrlConnection(HttpURLConnection delegate) {
    super(delegate.getURL());
    this.delegate = delegate;
  }

  @Override
  public String getCipherSuite() {
    final Handshake handshake = handShake();
    return handshake != null ? handshake.cipherSuite().javaName() : null;
  }

  protected abstract Handshake handShake();

  @Override
  public Certificate[] getLocalCertificates() {
    final Handshake handshake = handShake();
    if (handshake == null) return null;
    final List<Certificate> certificates = handshake.localCertificates();
    return !OkUtils.empty(certificates)
      ? certificates.toArray(new Certificate[certificates.size()])
      : null;
  }

  @Override
  public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
    final Handshake handshake = handShake();
    if (handshake == null) return null;
    final List<Certificate> certificates = handshake.peerCertificates();
    return !OkUtils.empty(certificates)
      ? certificates.toArray(new Certificate[certificates.size()])
      : null;
  }

  @Override
  public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
    final Handshake handshake = handShake();
    return handshake != null ? handshake.peerPrincipal() : null;
  }

  @Override
  public Principal getLocalPrincipal() {
    final Handshake handshake = handShake();
    return handshake != null ? handshake.localPrincipal() : null;
  }

  @Override
  public void connect() throws IOException {
    connected = true;
    delegate.connect();
  }

  @Override
  public int getContentLength() {
    return delegate.getContentLength();
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @Override // Should only be invoked on Java 7+.
  public long getContentLengthLong() {
    return delegate.getContentLengthLong();
  }

  @Override
  public String getContentType() {
    return delegate.getContentType();
  }

  @Override
  public long getDate() {
    return delegate.getDate();
  }

  @Override
  public boolean getDefaultUseCaches() {
    return delegate.getDefaultUseCaches();
  }

  @Override
  public boolean getDoInput() {
    return delegate.getDoInput();
  }

  @Override
  public boolean getDoOutput() {
    return delegate.getDoOutput();
  }

  @Override
  public long getExpiration() {
    return delegate.getExpiration();
  }

  @Override
  public String getHeaderField(String name) {
    return delegate.getHeaderField(name);
  }

  @Override
  public String getHeaderField(int pos) {
    return delegate.getHeaderField(pos);
  }

  @Override
  public Map<String, List<String>> getHeaderFields() {
    return delegate.getHeaderFields();
  }

  @Override
  public Map<String, List<String>> getRequestProperties() {
    return delegate.getRequestProperties();
  }

  @Override
  public void addRequestProperty(String key, String value) {
    delegate.addRequestProperty(key, value);
  }

  @TargetApi(Build.VERSION_CODES.N)
  @Override
  public long getHeaderFieldLong(String name, long defValue) {
    return delegate.getHeaderFieldLong(name, defValue);
  }

  @Override
  public String getHeaderFieldKey(int pos) {
    return delegate.getHeaderFieldKey(pos);
  }

  @Override
  public long getHeaderFieldDate(String name, long defValue) {
    return delegate.getHeaderFieldDate(name, defValue);
  }

  @Override
  public int getHeaderFieldInt(String name, int defValue) {
    return delegate.getHeaderFieldInt(name, defValue);
  }

  @Override
  public long getIfModifiedSince() {
    return delegate.getIfModifiedSince();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return delegate.getInputStream();
  }

  @Override
  public long getLastModified() {
    return delegate.getLastModified();
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return delegate.getOutputStream();
  }

  @Override
  public Permission getPermission() throws IOException {
    return delegate.getPermission();
  }

  @Override
  public void setRequestProperty(String key, String value) {
    delegate.setRequestProperty(key, value);
  }

  @Override
  public URL getURL() {
    return delegate.getURL();
  }

  @Override
  public boolean getUseCaches() {
    return delegate.getUseCaches();
  }

  @Override
  public void setAllowUserInteraction(boolean newValue) {
    delegate.setAllowUserInteraction(newValue);
  }

  @Override
  public void setDefaultUseCaches(boolean newValue) {
    delegate.setDefaultUseCaches(newValue);
  }

  @Override
  public void setDoInput(boolean newValue) {
    delegate.setDoInput(newValue);
  }

  @Override
  public void setDoOutput(boolean newValue) {
    delegate.setDoOutput(newValue);
  }

  @Override
  public void setFixedLengthStreamingMode(int contentLength) {
    delegate.setFixedLengthStreamingMode(contentLength);
  }

  @Override
  public void setFixedLengthStreamingMode(long contentLength) {
    delegate.setFixedLengthStreamingMode(contentLength);
  }

  @Override
  public void setIfModifiedSince(long newValue) {
    delegate.setIfModifiedSince(newValue);
  }

  @Override
  public void setUseCaches(boolean newValue) {
    delegate.setUseCaches(newValue);
  }

  @Override
  public void setConnectTimeout(int timeout) {
    delegate.setConnectTimeout(timeout);
  }

  @Override
  public int getConnectTimeout() {
    return delegate.getConnectTimeout();
  }

  @Override
  public void setReadTimeout(int timeout) {
    delegate.setReadTimeout(timeout);
  }

  @Override
  public int getReadTimeout() {
    return delegate.getReadTimeout();
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public void setChunkedStreamingMode(int chunkLength) {
    delegate.setChunkedStreamingMode(chunkLength);
  }

  @Override
  public String getContentEncoding() {
    return delegate.getContentEncoding();
  }

  @Override
  public Object getContent() throws IOException {
    return delegate.getContent();
  }

  @Override
  public Object getContent(Class[] classes) throws IOException {
    return delegate.getContent(classes);
  }

  @Override
  public void disconnect() {
    delegate.disconnect();
  }

  @Override
  public boolean getAllowUserInteraction() {
    return delegate.getAllowUserInteraction();
  }

  @Override
  public InputStream getErrorStream() {
    return delegate.getErrorStream();
  }

  @Override
  public String getRequestMethod() {
    return delegate.getRequestMethod();
  }

  @Override
  public int getResponseCode() throws IOException {
    return delegate.getResponseCode();
  }

  @Override
  public void setRequestMethod(String method) throws ProtocolException {
    delegate.setRequestMethod(method);
  }

  @Override
  public boolean usingProxy() {
    return delegate.usingProxy();
  }

  @Override
  public boolean getInstanceFollowRedirects() {
    return delegate.getInstanceFollowRedirects();
  }

  @Override
  public void setInstanceFollowRedirects(boolean followRedirects) {
    delegate.setInstanceFollowRedirects(followRedirects);
  }

  @Override
  public abstract void setHostnameVerifier(HostnameVerifier v);

  @Override
  public abstract HostnameVerifier getHostnameVerifier();

  @Override
  public abstract void setSSLSocketFactory(SSLSocketFactory sslSocketFactory);

  @Override
  public abstract SSLSocketFactory getSSLSocketFactory();
}
