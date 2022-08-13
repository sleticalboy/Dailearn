package com.binlee.learning.plugin;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.PathClassLoader;

/**
 * Created on 2022-08-08.
 *
 * @author binlee
 */
public final class PluginLoader {

  static final String TAG = "PluginLoader";

  private static final List<String> PLUGIN_CLASS_LOADERS = new ArrayList<>();

  public static synchronized ClassLoader proxy(String pluginPath, ClassLoader parent) {
    if (pluginPath == null || pluginPath.trim().length() == 0) return parent;

    if (PLUGIN_CLASS_LOADERS.contains(pluginPath)) return parent;

    if (!new File(pluginPath).exists()) return parent;

    Log.d(TAG, "proxy() start construct class loader");

    // 1、使用 apk/dex/zip 路径构建 ClassLoader
    final ClassLoader classLoader = new PathClassLoader(pluginPath, parent) {
      @NonNull @Override public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("\ncurrent: ").append(super.toString()).append('\n');
        for (ClassLoader parent = getParent(); ;parent = parent.getParent()) {
          if (parent == null) {
            buffer.append("parent: root");
            break;
          } else {
            buffer.append("parent: ").append(parent).append('\n');
          }
        }
        return buffer.toString();
      }
    };
    // 2、标记一下，下次不处理了
    PLUGIN_CLASS_LOADERS.add(pluginPath);
    // 3、返回新的 ClassLoader， JVM 类加载委托机制会先从父加载器查找 Class，最后通过我们的加载器查找

    Log.d(TAG, "proxy() pluginPath: " + pluginPath + ", loader chain -> " + classLoader);
    return classLoader;
  }
}
