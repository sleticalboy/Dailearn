package com.binlee.learning.http.converter;

import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created on 18-8-18.
 * <p>
 * FastJsonConverterFactory for Retrofit support
 *
 * @author leebin
 */
public final class FastJsonConverterFactory extends Converter.Factory {

  public static FastJsonConverterFactory create() {
    return new FastJsonConverterFactory();
  }

  @Override
  public Converter<ResponseBody, ?> responseBodyConverter(Type type,
    Annotation[] annotations,
    Retrofit retrofit) {
    return new ResponseBodyConverter<>(type);
  }

  @Override
  public Converter<?, RequestBody> requestBodyConverter(Type type,
    Annotation[] parameterAnnotations,
    Annotation[] methodAnnotations,
    Retrofit retrofit) {
    return new RequestBodyConverter<>();
  }

  private static final class ResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private final Type mType;

    private ResponseBodyConverter(Type type) {
      mType = type;
    }

    @Override
    public T convert(ResponseBody responseBody) throws IOException {
      BufferedSource source = Okio.buffer(responseBody.source());
      final String responseResult = source.readUtf8();
      source.close();
      return JSON.parseObject(responseResult, mType);
    }
  }

  private static final class RequestBodyConverter<T> implements Converter<T, RequestBody> {

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=UTF-8");

    @Override
    public RequestBody convert(T requestBody) throws IOException {
      return RequestBody.create(MEDIA_TYPE_JSON, JSON.toJSONBytes(requestBody));
    }
  }
}
