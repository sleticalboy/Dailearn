package com.sleticalboy.http;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

/**
 * Created on 21-3-19.
 *
 * @author bnli@grandstream.cn
 */
public final class ListCallAdapterFactory extends CallAdapter.Factory {

  private ListCallAdapterFactory() {
  }

  public static ListCallAdapterFactory create() {
    return new ListCallAdapterFactory();
  }

  @Override
  public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
    // service 方法返回值类型
    Class<?> rawType = getRawType(returnType);
    if (rawType != List.class) return null;
    // 判断是否为参数化类型
    if (!(returnType instanceof ParameterizedType)) {
      throw new IllegalArgumentException(
        "Call return type must be parameterized as List<Foo> or List<? extends Foo>");
    }
    // 获取参数化类型的第一个参数类型
    final Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);
    return new CallAdapter<String, List<String>>() {
      @Override
      public Type responseType() {
        return responseType;
      }

      @Override
      public List<String> adapt(Call<String> call) {
        try {
          return Collections.singletonList(call.execute().body());
        } catch (IOException e) {
          return Collections.emptyList();
        }
      }
    };
  }
}
