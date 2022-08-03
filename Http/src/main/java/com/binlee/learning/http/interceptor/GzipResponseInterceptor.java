package com.binlee.learning.http.interceptor;

import androidx.annotation.NonNull;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.GzipSource;
import okio.Okio;

/**
 * Created on 18-10-8.
 *
 * @author leebin
 */
public final class GzipResponseInterceptor implements Interceptor {

  private GzipResponseInterceptor() {
    //no instance
  }

  public static GzipResponseInterceptor newInstance() {
    return new GzipResponseInterceptor();
  }

  @NonNull @Override
  public Response intercept(Chain chain) throws IOException {
    final Request request = chain.request();
    final String gzip = request.header("Accept-Encoding");
    if ("gzip".equalsIgnoreCase(gzip)) {
      return unGzipResponse(chain.proceed(request));
    } else {
      return chain.proceed(request);
    }
  }

  private Response unGzipResponse(Response response) {
    final ResponseBody responseBody = response.body();
    if (responseBody == null) {
      return response;
    }
    return response.newBuilder()
      .body(new ResponseBody() {
        @Override
        public MediaType contentType() {
          return responseBody.contentType();
        }

        @Override
        public long contentLength() {
          return 0;
        }

        @NonNull @Override
        public BufferedSource source() {
          return Okio.buffer(new GzipSource(responseBody.source()));
        }
      })
      .build();
  }
}
