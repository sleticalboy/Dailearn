package com.binlee.learning.http;

import android.util.Log;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created on 18-9-14.
 * <p>
 * okhttp 网络请求过程监听
 *
 * @author leebin
 */
final class DefaultEventListener extends EventListener {

  private static final String TAG = "DefaultEventListener";

  @Override
  public void callStart(final Call call) {
    Log.d(TAG, "callStart() called with: call = [" + call + "]");
  }

  @Override
  public void dnsStart(final Call call, final String domainName) {
    Log.d(TAG, "dnsStart() called with: call = [" + call + "], domainName = [" + domainName + "]");
  }

  @Override
  public void dnsEnd(final Call call, final String domainName,
    final List<InetAddress> inetAddressList) {
    Log.d(TAG, "dnsEnd() called with: call = [" + call + "], domainName = [" + domainName
      + "], inetAddressList = [" + inetAddressList + "]");
  }

  @Override
  public void connectStart(final Call call, final InetSocketAddress inetSocketAddress,
    final Proxy proxy) {
    Log.d(TAG, "connectStart() called with: call = [" + call + "], inetSocketAddress = [" + inetSocketAddress
      + "], proxy = [" + proxy + "]");
  }

  @Override
  public void secureConnectStart(final Call call) {
    Log.d(TAG, "secureConnectStart() called with: call = [" + call + "]");
  }

  @Override
  public void secureConnectEnd(final Call call, final Handshake handshake) {
    Log.d(TAG, "secureConnectEnd() called with: call = [" + call + "], handshake = [" + handshake + "]");
  }

  @Override
  public void connectEnd(final Call call, final InetSocketAddress inetSocketAddress,
    final Proxy proxy, final Protocol protocol) {
    Log.d(TAG, "connectEnd() called with: call = [" + call + "], inetSocketAddress = [" + inetSocketAddress
      + "], proxy = [" + proxy + "], protocol = [" + protocol + "]");
  }

  @Override
  public void connectFailed(final Call call, final InetSocketAddress inetSocketAddress,
    final Proxy proxy, final Protocol protocol, final IOException ioe) {
    Log.d(TAG, "connectFailed() called with: call = [" + call + "], inetSocketAddress = [" + inetSocketAddress
      + "], proxy = [" + proxy + "], protocol = [" + protocol + "], ioe = [" + ioe + "]");
  }

  @Override
  public void connectionAcquired(final Call call, final Connection connection) {
    Log.d(TAG, "connectionAcquired() called with: call = [" + call + "], connection = [" + connection + "]");
  }

  @Override
  public void connectionReleased(final Call call, final Connection connection) {
    Log.d(TAG, "connectionReleased() called with: call = [" + call + "], connection = [" + connection + "]");
  }

  @Override
  public void requestHeadersStart(final Call call) {
    Log.d(TAG, "requestHeadersStart() called with: call = [" + call + "]");
  }

  @Override
  public void requestHeadersEnd(final Call call, final Request request) {
    Log.d(TAG, "requestHeadersEnd() called with: call = [" + call + "], request = [" + request + "]");
  }

  @Override
  public void requestBodyStart(final Call call) {
    Log.d(TAG, "requestBodyStart() called with: call = [" + call + "]");
  }

  @Override
  public void requestBodyEnd(final Call call, final long byteCount) {
    Log.d(TAG, "requestBodyEnd() called with: call = [" + call + "], byteCount = [" + byteCount + "]");
  }

  @Override
  public void responseHeadersStart(final Call call) {
    Log.d(TAG, "responseHeadersStart() called with: call = [" + call + "]");
  }

  @Override
  public void responseHeadersEnd(final Call call, final Response response) {
    Log.d(TAG, "responseHeadersEnd() called with: call = [" + call + "], response = [" + response + "]");
  }

  @Override
  public void responseBodyStart(final Call call) {
    Log.d(TAG, "responseBodyStart() called with: call = [" + call + "]");
  }

  @Override
  public void responseBodyEnd(final Call call, final long byteCount) {
    Log.d(TAG, "responseBodyEnd() called with: call = [" + call + "], byteCount = [" + byteCount + "]");
  }

  @Override
  public void callEnd(final Call call) {
    Log.d(TAG, "callEnd() called with: call = [" + call + "]");
  }

  @Override
  public void callFailed(final Call call, final IOException ioe) {
    Log.d(TAG, "callFailed() called with: call = [" + call + "], ioe = [" + ioe.getMessage() + "]");
  }
}
