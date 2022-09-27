package com.binlee.dl.host.hook;

import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import androidx.annotation.NonNull;
import java.lang.reflect.Field;

/**
 * Created on 2022/9/21
 *
 * @author binlee
 */
public final class DlHandlerCallback implements Handler.Callback {

  private static final String TAG = "DlHandlerCallback";

  public static final int CREATE_SERVICE = 114;
  public static final int SERVICE_ARGS = 115;
  public static final int STOP_SERVICE = 116;
  public static final int BIND_SERVICE = 121;
  public static final int UNBIND_SERVICE = 122;
  public static final int DUMP_SERVICE = 123;

  @Override public boolean handleMessage(@NonNull Message msg) {
    Log.d(TAG, "handleMessage() what: " + what(msg.what) + ", " + msg);
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
            originalInfo.name = "com.example.plugin.PluginService";
          }
        } catch (NoSuchFieldException | IllegalAccessException e) {
          e.printStackTrace();
        }
      }
      case SERVICE_ARGS: {
        // android.app.ActivityThread.ServiceArgsData
        // Binder token;
        // boolean taskRemoved;
        // int startId;
        // int flags;
        // Intent args;
      }
    }
    return false;
  }

  static String what(int code) {
    switch (code) {
      case CREATE_SERVICE: return "CREATE_SERVICE";
      case SERVICE_ARGS: return "SERVICE_ARGS";
      case STOP_SERVICE: return "STOP_SERVICE";
      case BIND_SERVICE: return "BIND_SERVICE";
      case UNBIND_SERVICE: return "UNBIND_SERVICE";
      case DUMP_SERVICE: return "DUMP_SERVICE";
    }
    return Integer.toString(code);
  }
}
