package com.binlee.learning.http;

import androidx.annotation.NonNull;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

/**
 * Created on 21-3-19.
 *
 * @author bnli@grandstream.cn
 */
public class ByteArrayCallAdapterFactory extends CallAdapter.Factory {

  public static ByteArrayCallAdapterFactory create() {
    return new ByteArrayCallAdapterFactory();
  }

  private ByteArrayCallAdapterFactory() {
  }

  @Override
  public CallAdapter<?, ?> get(@NonNull Type returnType, @NonNull Annotation[] annotations,
    @NonNull Retrofit retrofit) {
    Class<?> rawType = getRawType(returnType);
    if (rawType != byte[].class) return null;
    return new CallAdapter<ResponseBody, byte[]>() {
      @NonNull
      @Override
      public Type responseType() {
        return byte[].class;
      }

      @NonNull
      @Override
      public byte[] adapt(@NonNull Call<ResponseBody> call) {
        try {
          ResponseBody body = call.execute().body();
          if (body != null) return body.bytes();
        } catch (IOException ignored) {
        }
        return new byte[0];
      }
    };
  }
}
