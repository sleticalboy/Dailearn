package com.binlee.hidden;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import java.lang.reflect.Method;

/**
 * Created on 2022/8/18
 *
 * @author binlee
 */
public final class Hidden {

  private static final String TAG = "Hidden";

  private static boolean sInitialized = false;
  private static Object sRuntime;

  private Hidden() {
    //no instance
  }

  /** 解除 hidden api 限制 */
  public static void relieve(Context context) {
    if (!sInitialized) {
      try {
        reflectRelieve(context);
      } catch (Throwable thr) {
        Log.w(TAG, "relieve() via reflect error, fallback to native. error is: " + thr);
        System.loadLibrary("unseal-hidden-api");
        nativeRelieve(context.getApplicationInfo().targetSdkVersion, Build.FINGERPRINT);
      }
      sInitialized = true;
    }
  }

  private static void reflectRelieve(Context context) throws Throwable {
    // 通过系统类去反射系统方法，从而绕过系统限制
    final Method forName = Class.class.getDeclaredMethod("forName", String.class);
    final Method raw = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

    final Class<?> runtimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
    final Method getRuntime = (Method) raw.invoke(runtimeClass, "getRuntime", null);
    if (getRuntime != null) {
      getRuntime.setAccessible(true);
      sRuntime = getRuntime.invoke(null);
    }

    final Method setHiddenApiExemptions = (Method) raw.invoke(runtimeClass, "setHiddenApiExemptions", new Class[] { String[].class });
    if (setHiddenApiExemptions != null) {
      setHiddenApiExemptions.setAccessible(true);
      // 通过设置豁免名单来绕过系统限制
      setHiddenApiExemptions.invoke(sRuntime, new Object[] { new String[] { "L" } });
    }

    Log.d(TAG, "reflectRelieve() getRuntime: " + getRuntime
      + ", setHiddenApiExemptions: " + setHiddenApiExemptions
      + ", sRuntime: " + sRuntime
    );
  }

  private static native void nativeRelieve(int targetSdkVersion, String fingerprint);
}
