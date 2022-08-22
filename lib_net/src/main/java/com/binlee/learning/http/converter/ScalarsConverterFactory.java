/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.binlee.learning.http.converter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * A {@linkplain Converter.Factory converter} for strings and both primitives and their boxed types
 * to {@code text/plain} bodies.
 */
public final class ScalarsConverterFactory extends Converter.Factory {
  public static ScalarsConverterFactory create() {
    return new ScalarsConverterFactory();
  }

  private ScalarsConverterFactory() {
  }

  @Override
  public Converter<?, RequestBody> requestBodyConverter(Type type,
    Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
    if (type == String.class
      || type == boolean.class
      || type == Boolean.class
      || type == byte.class
      || type == Byte.class
      || type == char.class
      || type == Character.class
      || type == double.class
      || type == Double.class
      || type == float.class
      || type == Float.class
      || type == int.class
      || type == Integer.class
      || type == long.class
      || type == Long.class
      || type == short.class
      || type == Short.class) {
      return ScalarRequestBodyConverter.INSTANCE;
    }
    return null;
  }

  @Override
  public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
    Retrofit retrofit) {
    if (type == String.class) {
      return ScalarResponseBodyConverters.StringResponseBodyConverter.INSTANCE;
    }
    if (type == Boolean.class || type == boolean.class) {
      return ScalarResponseBodyConverters.BooleanResponseBodyConverter.INSTANCE;
    }
    if (type == Byte.class || type == byte.class) {
      return ScalarResponseBodyConverters.ByteResponseBodyConverter.INSTANCE;
    }
    if (type == Character.class || type == char.class) {
      return ScalarResponseBodyConverters.CharacterResponseBodyConverter.INSTANCE;
    }
    if (type == Double.class || type == double.class) {
      return ScalarResponseBodyConverters.DoubleResponseBodyConverter.INSTANCE;
    }
    if (type == Float.class || type == float.class) {
      return ScalarResponseBodyConverters.FloatResponseBodyConverter.INSTANCE;
    }
    if (type == Integer.class || type == int.class) {
      return ScalarResponseBodyConverters.IntegerResponseBodyConverter.INSTANCE;
    }
    if (type == Long.class || type == long.class) {
      return ScalarResponseBodyConverters.LongResponseBodyConverter.INSTANCE;
    }
    if (type == Short.class || type == short.class) {
      return ScalarResponseBodyConverters.ShortResponseBodyConverter.INSTANCE;
    }
    return null;
  }

  private static final class ScalarRequestBodyConverter<T> implements Converter<T, RequestBody> {
    static final ScalarRequestBodyConverter<Object> INSTANCE = new ScalarRequestBodyConverter<>();
    private static final MediaType MEDIA_TYPE = MediaType.parse("text/*; charset=UTF-8");

    private ScalarRequestBodyConverter() {
    }

    @Override
    public RequestBody convert(T value) throws IOException {
      return RequestBody.create(MEDIA_TYPE, String.valueOf(value));
    }
  }

  private static final class ScalarResponseBodyConverters {
    private ScalarResponseBodyConverters() {
    }

    static final class StringResponseBodyConverter implements Converter<ResponseBody, String> {
      static final ScalarResponseBodyConverters.StringResponseBodyConverter INSTANCE =
        new ScalarResponseBodyConverters.StringResponseBodyConverter();

      @Override
      public String convert(ResponseBody value) throws IOException {
        return value.string();
      }
    }

    static final class BooleanResponseBodyConverter implements Converter<ResponseBody, Boolean> {
      static final ScalarResponseBodyConverters.BooleanResponseBodyConverter INSTANCE =
        new ScalarResponseBodyConverters.BooleanResponseBodyConverter();

      @Override
      public Boolean convert(ResponseBody value) throws IOException {
        return Boolean.valueOf(value.string());
      }
    }

    static final class ByteResponseBodyConverter implements Converter<ResponseBody, Byte> {
      static final ScalarResponseBodyConverters.ByteResponseBodyConverter INSTANCE =
        new ScalarResponseBodyConverters.ByteResponseBodyConverter();

      @Override
      public Byte convert(ResponseBody value) throws IOException {
        return Byte.valueOf(value.string());
      }
    }

    static final class CharacterResponseBodyConverter implements Converter<ResponseBody, Character> {
      static final ScalarResponseBodyConverters.CharacterResponseBodyConverter INSTANCE =
        new ScalarResponseBodyConverters.CharacterResponseBodyConverter();

      @Override
      public Character convert(ResponseBody value) throws IOException {
        String body = value.string();
        if (body.length() != 1) {
          throw new IOException("Expected body of length 1 for Character conversion but was " + body.length());
        }
        return body.charAt(0);
      }
    }

    static final class DoubleResponseBodyConverter implements Converter<ResponseBody, Double> {
      static final ScalarResponseBodyConverters.DoubleResponseBodyConverter INSTANCE =
        new ScalarResponseBodyConverters.DoubleResponseBodyConverter();

      @Override
      public Double convert(ResponseBody value) throws IOException {
        return Double.valueOf(value.string());
      }
    }

    static final class FloatResponseBodyConverter implements Converter<ResponseBody, Float> {
      static final ScalarResponseBodyConverters.FloatResponseBodyConverter INSTANCE =
        new ScalarResponseBodyConverters.FloatResponseBodyConverter();

      @Override
      public Float convert(ResponseBody value) throws IOException {
        return Float.valueOf(value.string());
      }
    }

    static final class IntegerResponseBodyConverter implements Converter<ResponseBody, Integer> {
      static final ScalarResponseBodyConverters.IntegerResponseBodyConverter INSTANCE =
        new ScalarResponseBodyConverters.IntegerResponseBodyConverter();

      @Override
      public Integer convert(ResponseBody value) throws IOException {
        return Integer.valueOf(value.string());
      }
    }

    static final class LongResponseBodyConverter implements Converter<ResponseBody, Long> {
      static final ScalarResponseBodyConverters.LongResponseBodyConverter INSTANCE =
        new ScalarResponseBodyConverters.LongResponseBodyConverter();

      @Override
      public Long convert(ResponseBody value) throws IOException {
        return Long.valueOf(value.string());
      }
    }

    static final class ShortResponseBodyConverter implements Converter<ResponseBody, Short> {
      static final ScalarResponseBodyConverters.ShortResponseBodyConverter INSTANCE =
        new ScalarResponseBodyConverters.ShortResponseBodyConverter();

      @Override
      public Short convert(ResponseBody value) throws IOException {
        return Short.valueOf(value.string());
      }
    }
  }
}
