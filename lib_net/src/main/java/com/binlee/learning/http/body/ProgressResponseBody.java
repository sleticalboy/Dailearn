package com.binlee.learning.http.body;

import androidx.annotation.NonNull;
import com.binlee.learning.http.callback.ProgressCallback;
import com.binlee.learning.http.interceptor.ProgressInterceptor;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created on 18-9-3.
 *
 * @author leebin
 */
public final class ProgressResponseBody extends ResponseBody {

  private final ResponseBody mResponseBody;
  private final long mBreakPoint;
  private final ProgressCallback mCallback;
  private BufferedSource mSource;

  public ProgressResponseBody(String url, ResponseBody body, long breakPoint) {
    mResponseBody = body;
    mCallback = ProgressInterceptor.getCallback(url);
    mBreakPoint = breakPoint;
    if (mCallback != null) {
      // 断点续传时是剩余文件的长度
      // contentLength() + mBreakPoint 是文件的总长度
      mCallback.onPreExecute(contentLength());
    }
  }

  @Override
  public MediaType contentType() {
    return mResponseBody.contentType();
  }

  @Override
  public long contentLength() {
    return mResponseBody.contentLength();
  }

  @NonNull @Override
  public BufferedSource source() {
    if (mSource == null) {
      mSource = Okio.buffer(new ProgressSource(mResponseBody.source(), mCallback, mBreakPoint, contentLength()));
    }
    return mSource;
  }

  private static class ProgressSource extends ForwardingSource {

    final long mContentLength;
    long bytesTotalRead;
    float currentProgress;
    ProgressCallback mCallback;

    ProgressSource(Source delegate, ProgressCallback callback, long breakPoint, long remaining) {
      super(delegate);
      bytesTotalRead = breakPoint;
      mCallback = callback;
      mContentLength = remaining + breakPoint;
    }

    @Override
    public long read(@NonNull Buffer sink, long byteCount) throws IOException {
      final long bytesRead = super.read(sink, byteCount);
      if (bytesRead == -1) {
        bytesTotalRead = mContentLength;
      } else {
        bytesTotalRead += bytesRead;
      }
      float progress = 100f * bytesTotalRead / mContentLength;
      // Log.d("ProgressResponseBody", "progress is " + progress + ", total read is "
      //         + bytesTotalRead + ", total is " + mContentLength);
      if (mCallback != null && currentProgress != progress) {
        mCallback.onProgress(progress, bytesTotalRead);
      }
      if (mCallback != null && bytesTotalRead == mContentLength) {
        mCallback = null;
      }
      currentProgress = progress;
      return bytesRead;
    }
  }
}
