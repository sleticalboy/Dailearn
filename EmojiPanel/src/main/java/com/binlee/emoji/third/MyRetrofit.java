package com.binlee.emoji.third;

import android.util.Log;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Created on 20-9-21.
 *
 * @author Ben binli@grandstream.cn
 */
public final class MyRetrofit {

  private static final String TAG = "MyRetrofit";
  private final Retrofit mRetrofit;

  private MyRetrofit() {
    final OkHttpClient.Builder builder = new OkHttpClient.Builder();
    builder.connectTimeout(30L, TimeUnit.SECONDS);
    builder.readTimeout(30L, TimeUnit.SECONDS);
    builder.writeTimeout(30L, TimeUnit.SECONDS);
    final Retrofit.Builder b = new Retrofit.Builder();
    b.client(builder.build());
    b.baseUrl("https://www.github.com/");
    mRetrofit = b.build();
  }

  private static class SingletonHolder {
    private static final MyRetrofit RETROFIT = new MyRetrofit();
  }

  public static MyRetrofit get() {
    return SingletonHolder.RETROFIT;
  }

  public <T> T create(Class<T> service) {
    try {
      analyse(service);
    } catch (Exception e) {
      Log.e(TAG, "before create, analyse error", e);
    }
    return mRetrofit.create(service);
  }

  public <T> void analyse(Class<T> service) throws Exception {
    final Method method = service.getDeclaredMethod("auth");
  }
}
