package com.binlee.learning;

import dalvik.system.PathClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2022-08-08.
 *
 * @author binlee
 */
public final class PluginLoader {

  private static final String TAG = "PluginLoader";

  private static final Map<String, Boolean> sClassLoaders = new HashMap<>();

  public static synchronized ClassLoader proxy(String apkOrDexPath, ClassLoader parent) {
    if (sClassLoaders.containsKey(apkOrDexPath)) return parent;
    ClassLoader tailLoader = parent;
    final List<String> dexList = new ArrayList<>();
    // 1、从 apk 中解析到所有的 dex 文件
    if (apkOrDexPath.endsWith(".dex")) {
      dexList.add(apkOrDexPath);
    } else if (apkOrDexPath.endsWith(".apk")) {
      extractDexFromArchive(apkOrDexPath, dexList);
    } else if (apkOrDexPath.endsWith(".zip")) {
      extractDexFromArchive(apkOrDexPath, dexList);
    }
    // 2、使用 dex 文件构造 PathClassLoader 链
    if (dexList.size() > 0) {
      for (String dexPath : dexList) {
        tailLoader = new PathClassLoader(dexPath, tailLoader);
      }
    }
    // 3、标记一下，下次不处理了
    sClassLoaders.put(apkOrDexPath, true);
    // 4、返回链尾
    return tailLoader;
  }

  private static void extractDexFromArchive(String archivePath, List<String> container) {
    // FIXME: 2022/8/8 从 apk 或 zip 包中提取 dex 文件
  }
}
