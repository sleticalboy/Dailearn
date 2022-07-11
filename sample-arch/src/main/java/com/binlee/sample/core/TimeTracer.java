package com.binlee.sample.core;

import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import com.binlee.sample.util.Glog;

/**
 * Created on 21-2-24.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class TimeTracer implements Handler.Callback {

  private static final String TAG = "TimeTracer";

  private final Handler mHandler;

  public TimeTracer(Handler handler) {
    if (handler instanceof InjectableHandler) {
      ((InjectableHandler) handler).injectCallback(this);
    }
    mHandler = handler;
  }

  @Override
  public boolean handleMessage(@NonNull Message msg) {
    Glog.i(TAG, "handleMessage() " + mHandler.getMessageName(msg));
    return false;
  }

  private void finish() {
    mHandler.obtainMessage(IWhat.TRACE_RESULT, new Result()).sendToTarget();
  }

  public static final class Result {
  }
}
