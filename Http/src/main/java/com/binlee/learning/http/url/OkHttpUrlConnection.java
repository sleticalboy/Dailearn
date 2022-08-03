package com.binlee.learning.http.url;

import com.binlee.learning.http.OkUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketPermission;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.Permission;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.Handshake;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Version;
import okhttp3.internal.http.HttpDate;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.http.HttpMethod;
import okhttp3.internal.http.StatusLine;
import okhttp3.internal.platform.Platform;

/**
 * Created on 18-10-5.
 *
 * @author leebin
 */
final class OkHttpUrlConnection extends HttpURLConnection implements Callback {

  /**
   * Synthetic response header: the selected {@link Protocol} ("spdy/3.1", "http/1.1",
   * etc).
   */
  public static final String SELECTED_PROTOCOL = Platform.get().getPrefix() + "-Selected-Protocol";

  /**
   * Synthetic response header: the location from which the response was loaded.
   */
  public static final String RESPONSE_SOURCE = Platform.get().getPrefix() + "-Response-Source";

  private static final Set<String> METHODS = new LinkedHashSet<>(
    Arrays.asList("OPTIONS", "GET", "POST", "PUT", "DELETE", "PATCH", "TRACE", "HEAD")
  );

  private final Object lock = new Object();
  private final NetworkInterceptor mNetworkInterceptor = new NetworkInterceptor();

  OkHttpClient mClient;
  Call mCall;
  Handshake mHandshake;
  private Throwable callFailure;
  private Headers.Builder requestHeaders = new Headers.Builder();
  private Headers responseHeaders;
  private boolean executed = false;
  private boolean connectPending = false;
  private Response mResponse;
  private Response networkResponse;
  private Proxy mProxy;
  private long fixedContentLength = -1L;

  public OkHttpUrlConnection(URL url, OkHttpClient client) {
    super(url);
    mClient = client;
  }

  @Override
  public void disconnect() {
    if (mCall == null) return;
    mNetworkInterceptor.proceed();
    mCall.cancel();
  }

  @Override
  public boolean usingProxy() {
    if (mProxy != null) return true;
    final Proxy clientProxy = mClient.proxy();
    return clientProxy != null && clientProxy.type() != Proxy.Type.DIRECT;
  }

  @Override
  public InputStream getErrorStream() {
    try {
      Response response = getResponse(true);
      if (HttpHeaders.hasBody(response) && response.code() >= HTTP_BAD_REQUEST) {
        return response.body().byteStream();
      }
      return null;
    } catch (IOException e) {
      return null;
    }
  }

  private Response getResponse(boolean networkResponseOnError) throws IOException {
    synchronized (lock) {
      if (mResponse != null) return mResponse;
      if (callFailure != null) {
        if (networkResponseOnError && networkResponse != null) return networkResponse;
        throw OkUtils.propagate(callFailure);
      }
    }
    final Call call = buildCall();
    mNetworkInterceptor.proceed();

    OutputStreamRequestBody requestBody = (OutputStreamRequestBody) call.request().body();
    if (requestBody != null) requestBody.outputStream().close();
    if (executed) {
      synchronized (lock) {
        try {
          while (mResponse != null && callFailure != null) {
            lock.wait();
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new InterruptedIOException();
        }
      }
    } else {
      executed = true;
      try {
        onResponse(call, call.execute());
      } catch (IOException e) {
        onFailure(call, e);
      }
    }
    synchronized (lock) {
      if (callFailure != null) throw OkUtils.propagate(callFailure);
      if (mResponse != null) return mResponse;
    }
    throw new AssertionError();
  }

  private Headers getHeaders() throws IOException {
    if (responseHeaders == null) {
      final Response response = getResponse(true);
      responseHeaders = response.headers().newBuilder()
        .add(SELECTED_PROTOCOL, response.protocol().toString())
        .add(RESPONSE_SOURCE, OkUtils.responseSourceHeader(response))
        .build();
    }
    return responseHeaders;
  }

  @Override
  public String getHeaderField(int position) {
    try {
      final Headers headers = getHeaders();
      if (position < 0 || position >= headers.size()) return null;
      return headers.value(position);
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public String getHeaderField(String fieldName) {
    try {
      return fieldName == null
        ? StatusLine.get(getResponse(true)).toString()
        : getHeaders().get(fieldName);
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public String getHeaderFieldKey(int position) {
    try {
      final Headers headers = getHeaders();
      if (position < 0 || position >= headers.size()) return null;
      return headers.name(position);
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public Map<String, List<String>> getHeaderFields() {
    try {
      return OkUtils.toMultimap(getHeaders(),
        StatusLine.get(getResponse(true)).toString());
    } catch (IOException e) {
      return Collections.emptyMap();
    }
  }

  @Override
  public Map<String, List<String>> getRequestProperties() {
    if (connected) {
      throw new IllegalStateException("Cannot access request header fields after connection is set");
    }
    return OkUtils.toMultimap(requestHeaders.build(), null);
  }

  @Override
  public InputStream getInputStream() throws IOException {
    if (!doOutput) {
      throw new ProtocolException("This protocol does not support input");
    }
    final Response response = getResponse(false);
    if (response.code() >= HTTP_BAD_REQUEST) {
      throw new FileNotFoundException(url.toString());
    }
    return response.body().byteStream();
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    OutputStreamRequestBody requestBody = (OutputStreamRequestBody) buildCall().request().body();
    if (requestBody == null) {
      throw new ProtocolException("method does not support a request body: " + method);
    }
    if (requestBody instanceof StreamedRequestBody) {
      connect();
      mNetworkInterceptor.proceed();
    }
    if (requestBody.isClosed()) {
      throw new ProtocolException("cannot write request body after response has been read");
    }
    return requestBody.outputStream();
  }

  @Override
  public Permission getPermission() throws IOException {
    final URL url = getURL();
    String hostname = url.getHost();
    int hostPort = url.getPort() != -1
      ? url.getPort()
      : HttpUrl.defaultPort(url.getProtocol());
    if (usingProxy()) {
      InetSocketAddress proxyAddress = (InetSocketAddress) mClient.proxy().address();
      hostname = proxyAddress.getHostName();
      hostPort = proxyAddress.getPort();
    }
    return new SocketPermission(hostname + ":" + hostPort, "connect, resolve");
  }

  @Override
  public String getRequestProperty(String key) {
    if (key == null) return null;
    return requestHeaders.get(key);
  }

  @Override
  public void setConnectTimeout(int timeout) {
    mClient = mClient.newBuilder()
      .connectTimeout(timeout, TimeUnit.MILLISECONDS)
      .build();
  }

  @Override
  public void setInstanceFollowRedirects(boolean followRedirects) {
    mClient = mClient.newBuilder()
      .followRedirects(followRedirects)
      .build();
  }

  @Override
  public boolean getInstanceFollowRedirects() {
    return mClient.followRedirects();
  }

  @Override
  public int getConnectTimeout() {
    return mClient.connectTimeoutMillis();
  }

  @Override
  public void setReadTimeout(int timeout) {
    mClient = mClient.newBuilder()
      .readTimeout(timeout, TimeUnit.MILLISECONDS)
      .build();
  }

  @Override
  public int getReadTimeout() {
    return mClient.readTimeoutMillis();
  }

  @Override
  public String getResponseMessage() throws IOException {
    return getResponse(true).message();
  }

  @Override
  public int getResponseCode() throws IOException {
    return getResponse(true).code();
  }

  @Override
  public void setRequestProperty(String field, String newValue) {
    if (connected) {
      throw new IllegalStateException("Cannot set request property after connection is made");
    }
    if (field == null) {
      throw new NullPointerException("field == null");
    }
    if (newValue == null) {
      // Silently ignore null header values for backwards compatibility with older
      // android versions as well as with other URLConnection implementations.
      //
      // Some implementations send a malformed HTTP header when faced with
      // such requests, we respect the spec and ignore the header.
      Platform.get().log(Platform.WARN, "Ignoring header " + field + " because its value was null.", null);
      return;
    }
    requestHeaders.set(field, newValue);
  }

  @Override
  public void setIfModifiedSince(long newValue) {
    super.setIfModifiedSince(newValue);
    if (ifModifiedSince != 0) {
      requestHeaders.set("If-Modify-Since", HttpDate.format(new Date(ifModifiedSince)));
    } else {
      requestHeaders.removeAll("If-Modify-Since");
    }
  }

  @Override
  public void addRequestProperty(String field, String value) {
    if (connected) {
      throw new IllegalStateException("Cannot add request property after connection is made");
    }
    if (field == null) {
      throw new NullPointerException("field == null");
    }
    if (value == null) {
      // Silently ignore null header values for backwards compatibility with older
      // android versions as well as with other URLConnection implementations.
      //
      // Some implementations send a malformed HTTP header when faced with
      // such requests, we respect the spec and ignore the header.
      Platform.get().log(Platform.WARN, "Ignoring header " + field + " because its value was null.", null);
      return;
    }

    requestHeaders.add(field, value);
  }

  @Override
  public void setRequestMethod(String method) throws ProtocolException {
    if (!METHODS.contains(method)) {
      throw new ProtocolException("Expected one of " + METHODS + " but was " + method);
    }
    this.method = method;
  }

  @Override
  public void setFixedLengthStreamingMode(int contentLength) {
    setFixedLengthStreamingMode(((long) contentLength));
  }

  @Override
  public void setFixedLengthStreamingMode(long contentLength) {
    if (super.connected) throw new IllegalStateException("Already connected");
    if (chunkLength > 0) throw new IllegalStateException("Already in chunked mode");
    if (contentLength < 0) throw new IllegalArgumentException("contentLength < 0");
    this.fixedContentLength = contentLength;
    super.fixedContentLength = (int) Math.min(contentLength, Integer.MAX_VALUE);
  }

  @Override
  public void connect() throws IOException {
    if (executed) return;
    final Call call = buildCall();
    executed = true;
    call.enqueue(this);
    synchronized (lock) {
      try {
        while (connectPending && mResponse == null && callFailure == null) {
          lock.wait();
        }
        if (callFailure != null) {
          throw OkUtils.propagate(callFailure);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new InterruptedIOException();
      }
    }
  }

  private Call buildCall() throws IOException {
    if (mCall != null) {
      return mCall;
    }
    connected = true;
    if (doOutput) {
      if ("GET".equals(method)) {
        // they are requesting a stream to write to. This implies a POST method
        method = "POST";
      } else if (!HttpMethod.permitsRequestBody(method)) {
        throw new ProtocolException(method + " does not support writing");
      }
    }
    if (requestHeaders.get("User-Agent") == null) {
      requestHeaders.add("User-Agent", defaultUserAgent());
    }
    OutputStreamRequestBody requestBody = null;
    if (HttpMethod.permitsRequestBody(method)) {
      String contentType = requestHeaders.get("Content-Type");
      if (contentType == null) {
        contentType = "application/x-www-form-urlencoded";
        requestHeaders.add("Content-Type", contentType);
      }
      boolean isStream = fixedContentLength != -1L || chunkLength > 0;
      long contentLength = -1L;
      final String contentLengthString = requestHeaders.get("Content-Length");
      if (fixedContentLength != -1) {
        contentLength = fixedContentLength;
      } else if (contentLengthString != null) {
        contentLength = Long.parseLong(contentLengthString);
      }
      requestBody = isStream
        ? new StreamedRequestBody(contentLength)
        : new BufferedRequestBody(contentLength);
      requestBody.timeout().timeout(mClient.writeTimeoutMillis(), TimeUnit.MILLISECONDS);
    }
    HttpUrl url;
    try {
      url = HttpUrl.get(getURL());
    } catch (IllegalArgumentException e) {
      if (e.getMessage().startsWith(OkUtils.INVALID_HOST)) {
        final UnknownHostException unknownHostException = new UnknownHostException();
        unknownHostException.initCause(e);
        throw unknownHostException;
      }
      MalformedURLException malformedURLException = new MalformedURLException();
      malformedURLException.initCause(e);
      throw malformedURLException;
    }
    final Request request = new Request.Builder()
      .url(url)
      .headers(requestHeaders.build())
      .method(method, requestBody)
      .build();
    final OkHttpClient.Builder clientBuilder = mClient.newBuilder();
    clientBuilder.interceptors().clear();
    clientBuilder.interceptors().add(UnexpectedException.INTERCEPTOR);
    clientBuilder.networkInterceptors().clear();
    clientBuilder.networkInterceptors().add(mNetworkInterceptor);
    clientBuilder.dispatcher(new Dispatcher(mClient.dispatcher().executorService()));
    if (!getUseCaches()) {
      clientBuilder.cache(null);
    }
    return mCall = clientBuilder.build().newCall(request);
  }

  private String defaultUserAgent() {
    final String agent = System.getProperty("http.agent");
    return agent != null ? OkUtils.toHumanReadableAscii(agent) : Version.userAgent();
  }

  @Override
  public void onFailure(Call call, IOException e) {
    synchronized (lock) {
      callFailure = e instanceof UnknownHostException ? e.getCause() : e;
      lock.notifyAll();
    }
  }

  @Override
  public void onResponse(Call call, Response response) throws IOException {
    synchronized (lock) {
      mResponse = response;
      mHandshake = response.handshake();
      url = response.request().url().url();
      lock.notifyAll();
    }
  }

  final static class UnexpectedException extends IOException {

    static final Interceptor INTERCEPTOR = new Interceptor() {
      @Override
      public Response intercept(Chain chain) throws IOException {
        try {
          return chain.proceed(chain.request());
        } catch (Error | RuntimeException e) {
          throw new UnexpectedException(e);
        }
      }
    };

    UnexpectedException(Throwable cause) {
      super(cause);
    }
  }

  final class NetworkInterceptor implements Interceptor {

    private boolean proceed;

    public void proceed() {
      synchronized (lock) {
        proceed = true;
        lock.notifyAll();
      }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
      Request request = chain.request();
      synchronized (lock) {
        connectPending = false;
        mProxy = chain.connection().route().proxy();
        mHandshake = chain.connection().handshake();
        lock.notifyAll();
        try {
          while (!proceed) {
            lock.wait();
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new InterruptedIOException();
        }
      }
      if (request.body() instanceof OutputStreamRequestBody) {
        request = ((OutputStreamRequestBody) request.body()).prepareToSendRequest(request);
      }
      final Response response = chain.proceed(request);
      synchronized (lock) {
        networkResponse = response;
        url = response.request().url().url();
      }
      return response;
    }
  }
}
