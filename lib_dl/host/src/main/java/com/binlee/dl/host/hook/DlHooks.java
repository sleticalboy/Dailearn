package com.binlee.dl.host.hook;

import android.annotation.SuppressLint;
import android.app.AppComponentFactory;
import android.app.IActivityManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.util.Singleton;
import com.binlee.dl.DlConst;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

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
  private static DlInstrumentation sInstrumentation;

  private DlHooks() {
    //no instance
  }

  // return the hooked instrumentation
  public static DlInstrumentation getInstrumentation() {
    return sInstrumentation;
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
    sInstrumentation = instrumentation;
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

  @SuppressWarnings("unchecked")
  public static void setActivityManager(DlActivityManager handler) {
    try {
      // android.app.ActivityManager#IActivityManagerSingleton
      final Class<?> clazz = Class.forName("android.app.ActivityManager");
      Field field = clazz.getDeclaredField("IActivityManagerSingleton");
      field.setAccessible(true);
      final Singleton<IActivityManager> singleton = (Singleton<IActivityManager>) field.get(null);
      final IActivityManager am = singleton.get();
      handler.setDelegate(am);
      Log.d(TAG, "setActivityManager() original am: " + am);
      // startService/stopService/bindService/unbindService/
      final Object proxyAm = Proxy.newProxyInstance(/*loader*/handler.getClassLoader(),
        /*interfaces*/new Class[] { Class.forName("android.app.IActivityManager") },
        /*handler*/(proxy, method, args) -> {
          final Object invoked = handler.invoke(proxy, method, args);
          return invoked == DlConst.AM_METHOD_RESULT_MISSED ? method.invoke(am, args) : invoked;
        });
      Log.d(TAG, "setActivityManager() proxy am: " + proxyAm);
      final Field mInstance = Singleton.class.getDeclaredField("mInstance");
      mInstance.setAccessible(true);
      mInstance.set(singleton, proxyAm);
    } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  public static void setComponentFactory(Context hostContext) {
    final Object currentAt = getActivityThread();
    try {
      // android.app.ActivityThread#mPackages
      Field field = currentAt.getClass().getDeclaredField("mPackages");
      field.setAccessible(true);
      final Map<?, ?> packages = (Map<?, ?>) field.get(currentAt);
      if (packages == null) {
        return;
      }
      final WeakReference<?> weakRef = (WeakReference<?>) packages.get(hostContext.getPackageName());
      if (weakRef == null) {
        return;
      }
      final Object loadedApk = weakRef.get();
      if (loadedApk == null) {
        return;
      }
      // android.app.LoadedApk#mAppComponentFactory
      field = loadedApk.getClass().getDeclaredField("mAppComponentFactory");
      field.setAccessible(true);
      final AppComponentFactory delegate = (AppComponentFactory) field.get(loadedApk);
      field.set(loadedApk, new DlComponentFactory());
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
