package com.binlee.learning;

import android.util.Log;
import dalvik.system.PathClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2022-08-08.
 *
 * @author binlee
 */
public final class PluginLoader {

  static final String TAG = "PluginLoader";

  private static final Map<String, Boolean> sClassLoaders = new HashMap<>();

  public static synchronized ClassLoader proxy(String pluginPath, ClassLoader parent) {
    if (pluginPath == null || pluginPath.trim().length() == 0) return parent;

    Log.d(TAG, "proxy() pluginPath: " + pluginPath);

    if (sClassLoaders.containsKey(pluginPath)) return parent;
    // 1、使用 apk/dex/zip 路径构建 ClassLoader
    final ClassLoader classLoader = new PathClassLoader(pluginPath, null, parent);
    // 2、标记一下，下次不处理了
    sClassLoaders.put(pluginPath, true);
    // 3、返回新的 ClassLoader， JVM 类加载委托机制会先从父加载器查找 Class，最后通过我们的加载器查找
    return classLoader;
  }
}
