package com.binlee.learning;

import dalvik.system.PathClassLoader;
import java.io.File;
import java.io.IOException;
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

  static final String TAG = "PluginLoader";

  private static final Map<String, Boolean> sClassLoaders = new HashMap<>();

  public static synchronized ClassLoader proxy(String apkOrDexPath, ClassLoader parent) {
    if (sClassLoaders.containsKey(apkOrDexPath)) return parent;
    ClassLoader tailLoader = parent;
    final List<String> dexList = new ArrayList<>();
    // 1、收集所有的 dex 文
    if (apkOrDexPath.endsWith(".dex")) {
      // 本身就是 dex 文件，直接收集
      dexList.add(apkOrDexPath);
    } else if (apkOrDexPath.endsWith(".apk") || apkOrDexPath.endsWith(".zip")) {
      // 从 apk 中解压出所有 dex 文件
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
    try {
      final DexExtractor extractor = new DexExtractor(new File(archivePath), new File("dex_dir"));
      for (File file : extractor.extractDex()) {
        container.add(file.getAbsolutePath());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
