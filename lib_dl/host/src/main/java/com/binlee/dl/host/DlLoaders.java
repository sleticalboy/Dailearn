package com.binlee.dl.host;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.binlee.dl.host.util.FileUtils;
import dalvik.system.PathClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created on 2022-08-08.
 *
 * @author binlee
 */
final class DlLoaders {

  private static final String TAG = "PluginLoaders";

  private static class ClassLoaderCache {
    private final List<String> mRegistry = new ArrayList<>();
    private ClassLoader mParent;
    private PluginClassLoader mClassLoader;

    public boolean contains(String pluginPath) {
      return mRegistry.contains(pluginPath);
    }

    public void register(String pluginPath, ClassLoader parent) {
      if (mParent == null) mParent = parent;

      if (mClassLoader == null) mClassLoader = new PluginClassLoader(pluginPath, mParent);

      mClassLoader.linkLast(new PluginClassLoader(pluginPath, parent));
      mRegistry.add(pluginPath);
    }

    public void unregister(String pluginPath) {
      mClassLoader.unlink(pluginPath);
      mRegistry.remove(pluginPath);
    }
  }

  private static final ClassLoaderCache sCache = new ClassLoaderCache();

  static synchronized void install(Context host, String pluginPath) {
    if (pluginPath == null || pluginPath.trim().length() == 0) return;

    if (sCache.contains(pluginPath)) {
      Log.d(TAG, "proxy() hit on: " + pluginPath);
      return;
    }

    if (!FileUtils.validateFile(pluginPath)) return;

    Log.d(TAG, "proxy() start construct class loader");

    // 使用 apk/dex/zip 路径构建 ClassLoader
    sCache.register(pluginPath, host.getClassLoader());

    Log.d(TAG, "proxy() pluginPath: " + pluginPath + ", loader chain -> " + sCache.mClassLoader);
    // 返回新的 ClassLoader， JVM 类加载委托机制会先从父加载器查找 Class，最后通过我们的加载器查找
  }

  static ClassLoader peek(ClassLoader fallback) {
    return sCache.mClassLoader == null ? fallback : sCache.mClassLoader;
  }

  static void remove(String pluginPath) {
    sCache.unregister(pluginPath);
  }

  static List<String> collectAll() {
    return Collections.unmodifiableList(sCache.mRegistry);
  }

  private static final class PluginClassLoader extends PathClassLoader {

    private final String mPluginPath;
    private PluginClassLoader mChild;

    private PluginClassLoader(final String pluginPath, final ClassLoader parent) {
      super(pluginPath, parent);
      mPluginPath = pluginPath;
    }

    private void linkLast(final PluginClassLoader loader) {
      PluginClassLoader child = mChild;
      while (child != null) {
        if (child.mChild == null) {
          child.mChild = loader;
          break;
        }
        child = child.mChild;
      }
    }

    public void unlink(String pluginPath) {
      PluginClassLoader child = mChild;
      while (child != null) {
        if (child.mPluginPath.equals(pluginPath)) {
          if (child.mChild != null) {
            mChild = child.mChild;
            break;
          }
        }
        child = child.mChild;
      }
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
      try {
        return super.findClass(name);
      } catch (ClassNotFoundException cnf) {
        ClassNotFoundException failed = null;
        for (PluginClassLoader child = mChild; child != null; child = child.mChild) {
          try {
            return child.findClass(name);
          } catch (ClassNotFoundException e) {
            failed = e;
          }
        }
        if (failed == null) {
          failed = new ClassNotFoundException(name);
        }
        throw failed;
      }
    }

    @NonNull @Override public String toString() {
      final StringBuilder buffer = new StringBuilder();
      // root loader
      buffer.append("\nroot: null\n");

      final List<ClassLoader> parentChain = new ArrayList<>();
      for (ClassLoader parent = getParent(); parent != null;parent = parent.getParent()) {
        parentChain.add(parent);
      }
      // parent loaders
      for (int index = parentChain.size() - 1; index >= 0; index--) {
        buffer.append("parent: ").append(parentChain.get(index)).append('\n');
      }
      // current loader
      buffer.append("current: ").append(super.toString()).append('\n');
      // child loaders
      for (PluginClassLoader child = mChild; child != null; child = child.mChild) {
        buffer.append("child: ").append(child).append('\n');
      }
      return buffer.toString();
    }
  }
}
