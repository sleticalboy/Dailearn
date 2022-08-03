package com.binlee.learning.http.url;

import java.io.IOException;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Pipe;

/**
 * Created on 18-10-5.
 *
 * @author leebin
 */
final class StreamedRequestBody extends OutputStreamRequestBody {

  private final Pipe mPipe = new Pipe(/*1 << 13*/ 8192);

  StreamedRequestBody(long expectedContentLength) {
    initOutputStream(Okio.buffer(mPipe.sink()), expectedContentLength);
  }

  @Override
  public void writeTo(BufferedSink sink) throws IOException {
    final Buffer buffer = new Buffer();
    while (mPipe.source().read(buffer, 8192) != -1L) {
      sink.write(buffer, buffer.size());
    }
  }
}
