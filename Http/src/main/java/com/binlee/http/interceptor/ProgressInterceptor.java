package com.binlee.http.interceptor;

import androidx.collection.ArrayMap;
import com.binlee.http.body.ProgressResponseBody;
import com.binlee.http.callback.ProgressCallback;
import java.io.IOException;
import java.util.Map;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created on 18-9-3.
 *
 * @author leebin
 */
public final class ProgressInterceptor implements Interceptor {

  private static final Map<String, ProgressCallback> PROGRESS_CALLBACK_MAP = new ArrayMap<>();

  public static ProgressInterceptor newInstance() {
    return new ProgressInterceptor();
  }

  private ProgressInterceptor() {
  }

  public static void addCallback(String tag, ProgressCallback callback) {
    PROGRESS_CALLBACK_MAP.put(tag, callback);
  }

  public static ProgressCallback getCallback(String tag) {
    return PROGRESS_CALLBACK_MAP.get(tag);
  }

  public static void removeCallback(String tag) {
    PROGRESS_CALLBACK_MAP.remove(tag);
  }

  public static void removeAll() {
    PROGRESS_CALLBACK_MAP.clear();
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    final Request request = chain.request();
    // RANGE: bytes=123456-654321
    // 取出请求头中的断点信息
    final String range = request.header("RANGE");
    long breakPoint = 0L;
    final int index = range != null ? range.indexOf('-') : -1;
    if (index > 0) {
      breakPoint = Long.parseLong(range.substring(0, index));
    }
    final Response response = chain.proceed(request);
    final String url = request.url().toString();
    final ResponseBody body = response.body();
    return response.newBuilder()
      .body(new ProgressResponseBody(url, body, breakPoint))
      .build();
  }
}
