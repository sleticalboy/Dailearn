package com.binlee.http.interceptor;

import androidx.annotation.NonNull;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

/**
 * Created on 18-10-8.
 *
 * @author leebin
 */
public final class GzipRequestInterceptor implements Interceptor {

  private GzipRequestInterceptor() {
    //no instance
  }

  public static GzipRequestInterceptor newInstance() {
    return new GzipRequestInterceptor();
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    final Request request = chain.request();
    final String gzip = request.header("Content-Encoding");
    if (request.body() != null && "gzip".equalsIgnoreCase(gzip)) {
      return chain.proceed(gzipRequest(request));
    } else {
      return chain.proceed(request);
    }
  }

  private Request gzipRequest(Request request) {
    final RequestBody requestBody = request.body();
    if (requestBody == null) return request;
    return request.newBuilder()
      .method(request.method(), new RequestBody() {

        @Override
        public MediaType contentType() {
          return requestBody.contentType();
        }

        @Override
        public long contentLength() {
          return -1L;
        }

        @Override
        public void writeTo(@NonNull BufferedSink sink) throws IOException {
          final BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
          requestBody.writeTo(gzipSink);
          gzipSink.close();
        }
      })
      .build();
  }
}
