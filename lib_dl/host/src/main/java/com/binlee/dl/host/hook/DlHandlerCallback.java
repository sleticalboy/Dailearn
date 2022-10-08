package com.binlee.dl.host.hook;

import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import com.binlee.dl.plugin.DlServices;
import java.lang.reflect.Field;

/**
 * Created on 2022/9/21
 *
 * @author binlee
 */
public final class DlHandlerCallback implements Handler.Callback {

  private static final String TAG = "DlHandlerCallback";

  private static final int CREATE_SERVICE = 114;
  private static final int SERVICE_ARGS = 115;
  private static final int STOP_SERVICE = 116;
  private static final int BIND_SERVICE = 121;
  private static final int UNBIND_SERVICE = 122;
  private SparseArray<String> mWhats;

  public void setWhats(SparseArray<String> whats) {
    mWhats = whats;
    Log.d(TAG, "setWhats() called with: whats = [" + whats + "]");
  }

  @Override public boolean handleMessage(@NonNull Message msg) {
    Log.d(TAG, "handleMessage() " + mWhats.get(msg.what) + msg);
    switch (msg.what) {
      case CREATE_SERVICE: {
        // android.app.ActivityThread.CreateServiceData
        // IBinder token;
        // ServiceInfo info; // 替换掉 info.name
        // CompatibilityInfo compatInfo;
        // Intent intent;
        try {
          final Field field = msg.obj.getClass().getDeclaredField("info");
          field.setAccessible(true);
          final ServiceInfo originalInfo = (ServiceInfo) field.get(msg.obj);
          if (originalInfo != null) {
            originalInfo.name = DlServices.currentName();
          }
        } catch (NoSuchFieldException | IllegalAccessException e) {
          e.printStackTrace();
        }
      }
      break;
      case BIND_SERVICE:
      case SERVICE_ARGS:
      case UNBIND_SERVICE:
      case STOP_SERVICE: {
        DlServices.scheduleNext();
      }
      break;
    }
    return false;
  }
}
