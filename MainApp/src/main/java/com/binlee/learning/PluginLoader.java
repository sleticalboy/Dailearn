package com.binlee.learning;

import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import dalvik.system.PathClassLoader;

/**
 * Created on 2022-08-08.
 *
 * @author binlee
 */
public final class PluginLoader {

  static final String TAG = "PluginLoader";

  private static final Set<String> sClassLoaders = new HashSet<>();

  public static synchronized ClassLoader proxy(String pluginPath, ClassLoader parent) {
    if (pluginPath == null || pluginPath.trim().length() == 0) return parent;

    Log.d(TAG, "proxy() pluginPath: " + pluginPath);

    if (sClassLoaders.contains(pluginPath)) return parent;
    // 1、使用 apk/dex/zip 路径构建 ClassLoader
    final ClassLoader classLoader = new PathClassLoader(pluginPath, null, parent);
    // 2、标记一下，下次不处理了
    sClassLoaders.add(pluginPath);
    // 3、返回新的 ClassLoader， JVM 类加载委托机制会先从父加载器查找 Class，最后通过我们的加载器查找
    return classLoader;
  }
}
