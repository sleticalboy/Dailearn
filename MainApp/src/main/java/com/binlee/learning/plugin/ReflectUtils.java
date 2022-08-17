package com.binlee.learning.plugin;

import android.util.Log;

import java.lang.reflect.Field;

import dalvik.system.BaseDexClassLoader;

/**
 * Created on 19-4-5.
 *
 * @author leebin
 */
public final class ReflectUtils {

  private static final String TAG = "ReflectUtils";

  public static void setSystemDexElements(ClassLoader cl, Object value) {
    final Object pathList = getPathList(cl);
    if (pathList != null) {
      try {
        final Field dexElements = pathList.getClass().getDeclaredField("dexElements");
        if (!dexElements.isAccessible()) {
          dexElements.setAccessible(true);
        }
        dexElements.set(pathList, value);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        Log.e(TAG, "set dexElements to pathList cl: " + cl + ", value: " + value, e);
      }
    }
  }

  private static Object getPathList(ClassLoader cl) {
    try {
      final Field pathList = BaseDexClassLoader.class.getDeclaredField("pathList");
      if (!pathList.isAccessible()) {
        pathList.setAccessible(true);
      }
      return pathList.get(cl);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      Log.e(TAG, "get pathList cl: " + cl + ", error" + e.getMessage(), e);
      return null;
    }
  }

  public static Object getDexElements(ClassLoader cl) {
    final Object pathList = getPathList(cl);
    if (pathList != null) {
      try {
        final Field dexElements = pathList.getClass().getDeclaredField("dexElements");
        if (!dexElements.isAccessible()) {
          dexElements.setAccessible(true);
        }
        return dexElements.get(pathList);
      } catch (IllegalAccessException | NoSuchFieldException e) {
        return null;
      }
    }
    return null;
  }
}
