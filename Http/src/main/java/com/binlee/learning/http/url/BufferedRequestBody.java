package com.binlee.learning.http.url;

import java.io.IOException;
import okhttp3.Request;
import okio.Buffer;
import okio.BufferedSink;

/**
 * Created on 18-10-5.
 *
 * @author leebin
 */
final class BufferedRequestBody extends OutputStreamRequestBody {

  private final Buffer buffer = new Buffer();
  private long contentLength = -1L;

  BufferedRequestBody(long expectedContentLength) {
    initOutputStream(buffer, expectedContentLength);
  }

  @Override
  public long contentLength() throws IOException {
    return contentLength;
  }

  @Override
  public void writeTo(BufferedSink sink) throws IOException {
    buffer.copyTo(sink.buffer(), 0, buffer.size());
  }

  @Override
  public Request prepareToSendRequest(Request request) throws IOException {
    if (request.header("Content-Length") != null) return request;
    outputStream().close();
    contentLength = buffer.size();
    return request.newBuilder()
      .removeHeader("Content-Length")
      .header("Content-Length", Long.toString(contentLength))
      .build();
  }
}
