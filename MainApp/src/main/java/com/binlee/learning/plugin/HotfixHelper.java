package com.binlee.learning.plugin;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * Created on 19-4-2.
 *
 * @author leebin
 */
public final class HotfixHelper {

  private static final String TAG = "HotfixHelper";

  public static final String DEX_DIR = "odex";
  private static final String DEX_SUFFIX = ".dex";
  private static final String DEX_MAIN = "classes.dex";
  /**
   * 存放需要修复的 dex 文件
   */
  private static final Set<File> dexFiles = new HashSet<>();

  private HotfixHelper() {
    throw new AssertionError();
  }

  public static void loadHotfixFiles(Context context) {
    dexFiles.clear();
    // 加载热修复文件
    final File dexDir = context.getDir(DEX_DIR, Context.MODE_PRIVATE);
    final File[] files = dexDir.listFiles();
    if (files != null && files.length > 0) {
      for (File file : files) {
        final String name = file.getName();
        if (name.endsWith(DEX_SUFFIX) && !DEX_MAIN.equals(name)) {
          dexFiles.add(file);
        }
      }
    }
    // 模拟类加载器加载修复文件
    loadDexFiles(context, dexDir);
  }

  private static void loadDexFiles(Context context, File dexDir) {
    // 解压目录
    final File extDir = new File(dexDir, "ext");
    if (!extDir.exists()) {
      if (extDir.mkdirs()) {
        Log.d(TAG, "create extract folder");
      }
    }
    // 遍历 dexFiles 并逐一加载 dex 中的类
    for (File dexFile : dexFiles) {
      // 创建类加载器，用于加载修复文件
      final DexClassLoader classLoader = new DexClassLoader(dexFile.getAbsolutePath(),
        extDir.getAbsolutePath(), null, context.getClassLoader());
      doHotfix((PathClassLoader) context.getClassLoader(), classLoader);
    }
    Log.d(TAG, "hot fix successful");
  }

  private static void doHotfix(PathClassLoader pathLoader, DexClassLoader dexLoader) {
    // 1, 获取系统的 PathClassLoader
    // final ClassLoader sysLoader = context.getClassLoader();
    // 2, 获取修复包的 dexElements 数组
    final Object myDexElements = ReflectUtils.getDexElements(dexLoader);
    Log.d(TAG, "my dex elements: " + Arrays.toString((Object[]) myDexElements));
    // 3, 获取系统原有的 dexElements 数组
    final Object sysDexElements = ReflectUtils.getDexElements(pathLoader);
    Log.d(TAG, "system dex elements: " + Arrays.toString((Object[]) sysDexElements));
    // 4, 合并成新的 dexElements 数组[将自己的数组里的元素放在合并数组的最前面]
    final Object mergedElements = ArrayUtils.merge(myDexElements, sysDexElements);
    Log.d(TAG, "merged dex elements: " + Arrays.toString((Object[]) mergedElements));
    // 获取系统的 pathList 对象
    // 5, 将合成的新 dexElements 数组设置给系统的 pathList 属性值
    ReflectUtils.setSystemDexElements(pathLoader, mergedElements);
  }
}
