package com.binlee.dl.host.hook;

import android.annotation.SuppressLint;
import android.os.Handler;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created on 2022/9/21
 *
 * @author binlee
 */
public final class DlHooks {

  private static Object sCurrentAt;
  private static boolean sAtFlag = false;

  private DlHooks() {
    //no instance
  }

  public static Object getActivityThread() {
    // 已经反射过了，不再反射
    if (sAtFlag) return sCurrentAt;

    if (sCurrentAt == null) {
      try {
        @SuppressLint("PrivateApi")
        final Class<?> clazz = Class.forName("android.app.ActivityThread");
        @SuppressLint("DiscouragedPrivateApi")
        final Method method = clazz.getDeclaredMethod("currentActivityThread");
        method.setAccessible(true);
        sCurrentAt = method.invoke(null);
      } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        e.printStackTrace();
      }
      sAtFlag = true;
    }
    return sCurrentAt;
  }

  public static void setInstrumentation(DlInstrumentation instrumentation) {
    final Object currentAt = getActivityThread();
    try {
      final Field field = currentAt.getClass().getDeclaredField("mInstrumentation");
      field.setAccessible(true);
      instrumentation.setDelegate(field.get(currentAt));
      field.set(currentAt, instrumentation);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  public static void setHandlerCallback(Handler.Callback callback) {
    final Object currentAt = getActivityThread();
    try {
      final Field field = currentAt.getClass().getDeclaredField("mH");
      field.setAccessible(true);
      final Handler handler = (Handler) field.get(currentAt);
      final Field mCallback = Handler.class.getDeclaredField("mCallback");
      mCallback.setAccessible(true);
      mCallback.set(handler, callback);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
