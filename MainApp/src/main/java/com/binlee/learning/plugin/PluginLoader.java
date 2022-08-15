package com.binlee.learning.plugin;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.PathClassLoader;

/**
 * Created on 2022-08-08.
 *
 * @author binlee
 */
public final class PluginLoader {

  static final String TAG = "PluginLoader";

  private static final Map<String, ClassLoader> PLUGIN_LOADERS = new HashMap<>();

  public static synchronized ClassLoader proxy(String pluginPath, ClassLoader parent) {
    if (pluginPath == null || pluginPath.trim().length() == 0) return parent;

    ClassLoader loader = PLUGIN_LOADERS.get(pluginPath);
    if (loader != null) {
      Log.d(TAG, "proxy() hit on: " + pluginPath);
      return loader;
    }

    if (!new File(pluginPath).exists()) return parent;

    Log.d(TAG, "proxy() start construct class loader");

    // 1、使用 apk/dex/zip 路径构建 ClassLoader
    if (!(parent instanceof PluginClassLoader)) {
      loader = new PluginClassLoader(pluginPath, parent);
    } else {
      final ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
      return ((PluginClassLoader) parent).next(new PluginClassLoader(pluginPath, contextLoader));
    }
    // 2、标记一下，下次不处理了
    PLUGIN_LOADERS.put(pluginPath, loader);

    Log.d(TAG, "proxy() pluginPath: " + pluginPath + ", loader chain -> " + loader);
    // 3、返回新的 ClassLoader， JVM 类加载委托机制会先从父加载器查找 Class，
    // 最后通过我们的加载器查找
    return loader;
  }

  private static final class PluginClassLoader extends PathClassLoader {

    private PluginClassLoader mNext;

    private PluginClassLoader(final String dexPath, final ClassLoader parent) {
      super(dexPath, parent);
    }

    private ClassLoader next(final PluginClassLoader loader) {
      PluginClassLoader next = mNext;
      while (next != null) {
        if (next.mNext == null) {
          next.mNext = loader;
          break;
        }
        next = next.mNext;
      }
      return this;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
      Class<?> found = super.findClass(name);
      if (found == null) {
        for (PluginClassLoader next = mNext; next != null; next = next.mNext) {
          found = next.findClass(name);
          if (found != null) break;
          next = next.mNext;
        }
      }
      return found;
    }

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
  }
}
