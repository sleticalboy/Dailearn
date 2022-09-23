package com.binlee.learning.http.url;

import androidx.annotation.NonNull;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Timeout;

/**
 * Created on 18-10-5.
 *
 * @author leebin
 */
abstract class OutputStreamRequestBody extends RequestBody {

  private Timeout mTimeout;
  private long expectedContentLength;
  private OutputStream mOutputStream;
  private boolean closed;

  void initOutputStream(final BufferedSink sink, final long expectedContentLength) {
    mTimeout = sink.timeout();
    this.expectedContentLength = expectedContentLength;
    mOutputStream = new OutputStream() {

      private long bytesReceived = -1;

      @Override
      public void write(int b) throws IOException {
        write(new byte[] { (byte) b }, 0, 1);
      }

      @Override
      public void write(@NonNull byte[] source, int offset, int byteCount) throws IOException {
        if (closed) throw new IOException("closed");
        if (expectedContentLength != -1L && bytesReceived + byteCount > expectedContentLength) {
          throw new ProtocolException("expected " + expectedContentLength
            + " bytes but received " + bytesReceived + byteCount);
        }
        bytesReceived += byteCount;
        try {
          sink.write(source, offset, byteCount);
        } catch (InterruptedIOException e) {
          throw new SocketTimeoutException(e.getMessage());
        }
      }

      @Override
      public void flush() throws IOException {
        if (closed) return;
        sink.flush();
      }

      @Override
      public void close() throws IOException {
        closed = true;
        if (expectedContentLength != -1L && bytesReceived < expectedContentLength) {
          throw new ProtocolException("expected " + expectedContentLength
            + " bytes but received " + bytesReceived);
        }
        sink.close();
      }
    };
  }

  @Override
  public long contentLength() throws IOException {
    return expectedContentLength;
  }

  @Override
  public MediaType contentType() {
    return null;
  }

  public Timeout timeout() {
    return mTimeout;
  }

  public OutputStream outputStream() {
    return mOutputStream;
  }

  public boolean isClosed() {
    return closed;
  }

  public Request prepareToSendRequest(Request request) throws IOException {
    return request;
  }
}
