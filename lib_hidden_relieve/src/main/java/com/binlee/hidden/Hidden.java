package com.binlee.hidden;

import android.content.Context;
import java.lang.reflect.Method;

/**
 * Created on 2022/8/18
 *
 * @author binlee
 */
public final class Hidden {

  static {
    System.loadLibrary("unseal-hidden-api");
  }

  private static boolean sInitialized = false;
  private static Object sRuntime;

  private Hidden() {
    //no instance
  }

  /** 解除 hidden api 限制 */
  public static void relieve(Context context) {
    if (!sInitialized) {
      try {
        initOnce(context);
        nativeRelieve(context.getApplicationInfo().targetSdkVersion);
      } catch (Throwable thr) {
        thr.printStackTrace();
      }
      sInitialized = true;
    }
  }

  private static void initOnce(Context context) throws Throwable {
    // 通过系统类去反射系统方法，从而绕过系统限制
    final Method forName = Class.class.getDeclaredMethod("forName", String.class);
    final Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

    final Class<?> runtimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
    final Method getRuntime = (Method) getDeclaredMethod.invoke(runtimeClass, "getRuntime", null);
    sRuntime = getRuntime.invoke(null);

    final Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(runtimeClass, "setHiddenApiExemptions", new Class[] { String[].class });
    // 通过设置豁免名单来绕过系统限制
    setHiddenApiExemptions.invoke(sRuntime, new Object[] { new String[] {"L"} });
  }

  private static native void nativeRelieve(int targetSdkVersion);
}
