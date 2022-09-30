package com.binlee.dl.host.hook;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.SparseArray;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created on 2022/9/21
 *
 * @author binlee
 */
public final class DlHooks {

  private static final String TAG = "DlHooks";

  // 宿主 ActivityThread 对象
  private static Object sCurrentAt;
  private static boolean sAtFlag = false;

  // hooked instrumentation
  private static DlInstrumentation sInst;

  private DlHooks() {
    //no instance
  }

  // return the hooked instrumentation
  public static DlInstrumentation getInstrumentation() {
    return sInst;
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
    sInst = instrumentation;
  }

  public static void setHandlerCallback(DlHandlerCallback callback) {
    final Object currentAt = getActivityThread();
    try {
      final Field field = currentAt.getClass().getDeclaredField("mH");
      field.setAccessible(true);
      final Handler handler = (Handler) field.get(currentAt);
      callback.setWhats(parseWhats(handler));
      final Field mCallback = Handler.class.getDeclaredField("mCallback");
      mCallback.setAccessible(true);
      mCallback.set(handler, callback);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  private static SparseArray<String> parseWhats(Handler handler) {
    final SparseArray<String> array = new SparseArray<>();
    if (handler != null) {
      for (Field field : handler.getClass().getDeclaredFields()) {
        if (field.getType() == int.class && Modifier.isStatic(field.getModifiers())) {
          try {
            final Object what = field.get(handler);
            if (what != null) array.put((int) what, field.getName());
          } catch (IllegalAccessException ignored) {
          }
        }
      }
    }
    return array;
  }
}
