package com.sleticalboy.http;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created on 21-3-22.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class ObjConverterFactory extends Converter.Factory {

  private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=UTF-8");
  private static final Charset UTF_8 = StandardCharsets.UTF_8;

  public static ObjConverterFactory create() {
    return new ObjConverterFactory(new Gson());
  }

  private final Gson mGson;

  private ObjConverterFactory(Gson gson) {
    mGson = gson;
  }

  @Override
  public ResponseConverter<?> responseBodyConverter(Type type, Annotation[] annotations,
    Retrofit retrofit) {
    return new ResponseConverter<>(mGson.getAdapter(TypeToken.get(type)));
  }

  @Override
  public Converter<?, RequestBody> requestBodyConverter(Type type,
    Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
    return new RequestConverter<>(mGson.getAdapter(TypeToken.get(type)));
  }

  private final class ResponseConverter<T> implements Converter<ResponseBody, T> {

    private final TypeAdapter<T> mAdapter;

    public ResponseConverter(TypeAdapter<T> adapter) {
      mAdapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
      JsonReader reader = mGson.newJsonReader(value.charStream());
      try {
        T obj = mAdapter.read(reader);
        if (reader.peek() != JsonToken.END_DOCUMENT) {
          throw new JsonIOException("JSON document was not fully consumed.");
        }
        return obj;
      } finally {
        value.close();
      }
    }
  }

  private final class RequestConverter<T> implements Converter<T, RequestBody> {

    private final TypeAdapter<T> mAdapter;

    public RequestConverter(TypeAdapter<T> adapter) {
      mAdapter = adapter;
    }

    @Override
    public RequestBody convert(T value) throws IOException {
      Buffer buffer = new Buffer();
      Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
      JsonWriter jsonWriter = mGson.newJsonWriter(writer);
      mAdapter.write(jsonWriter, value);
      jsonWriter.close();
      return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
    }
  }
}
