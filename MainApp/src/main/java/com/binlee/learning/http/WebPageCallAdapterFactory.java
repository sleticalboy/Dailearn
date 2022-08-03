package com.binlee.learning.http;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

/**
 * Created on 21-3-19.
 *
 * @author bnli@grandstream.cn
 */
public final class WebPageCallAdapterFactory extends CallAdapter.Factory {

  public static WebPageCallAdapterFactory create() {
    return new WebPageCallAdapterFactory();
  }

  private WebPageCallAdapterFactory() {
  }

  @Override
  public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
    Class<?> rawType = getRawType(returnType);
    if (rawType != WebPage.class) return null;
    return new CallAdapter<String, WebPage>() {
      @Override
      public Type responseType() {
        return WebPage.class;
      }

      @Override
      public WebPage adapt(Call<String> call) {
        try {
          return new WebPage(call.execute().body());
        } catch (IOException e) {
          return new WebPage("error request: " + e);
        }
      }
    };
  }
}
