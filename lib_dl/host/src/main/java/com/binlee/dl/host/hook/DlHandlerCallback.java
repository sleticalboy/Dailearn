package com.binlee.dl.host.hook;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import androidx.annotation.NonNull;

/**
 * Created on 2022/9/21
 *
 * @author binlee
 */
public final class DlHandlerCallback implements Handler.Callback {

  private static final String TAG = "DlHandlerCallback";

  @Override public boolean handleMessage(@NonNull Message msg) {
    Log.d(TAG, "handleMessage() called with: msg = [" + msg + "]");
    return false;
  }
}
