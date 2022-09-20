package com.binlee.dl.host;

import android.content.res.Resources;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

/**
 * Created on 2022/8/16
 *
 * @author binlee
 */
public final class PluginManager {

  // 管理
  // 1、插件的加载和卸载（资源和 dex）


  /** 宿主资源 */
  private static Resources sParentResource;
  /** 宿主类加载器 */
  private static ClassLoader sParentLoader;

  private PluginManager() {
    //no instance
  }

  /**
   * 初始化
   *
   * @param loader 宿主类加载器
   * @param resources 宿主资源
   */
  public static void initialize(ClassLoader loader, Resources resources) {
    sParentLoader = loader;
    sParentResource = resources;
  }

  /**
   * 安装插件
   *
   * @param pluginPath 插件路径
   */
  public static void install(String pluginPath) {
    throwIfNotInitialized();
    DlLoaders.install(pluginPath, sParentLoader);
    DlResources.install(pluginPath, sParentResource);
  }

  /**
   * 卸载插件
   *
   * @param pluginPath 插件路径
   */
  public static void uninstall(String pluginPath) {
    DlLoaders.remove(pluginPath);
    DlResources.remove(pluginPath);
  }

  public static List<String> getAll() {
    return DlLoaders.collectAll();
  }


  /**
   * 加载类
   *
   * @param classname 类名称
   * @return {@link Class}<{@link ?}>
   * @throws ClassNotFoundException 类没有发现异常
   */
  @Nullable
  public static Class<?> loadClass(@NonNull String classname) throws ClassNotFoundException {
    throwIfNotInitialized();
    return DlLoaders.peek(sParentLoader).loadClass(classname);
  }

  /**
   * 获取资源，包含宿主和插件
   *
   * @return {@link Resources}
   */
  public static Resources resources() {
    throwIfNotInitialized();
    return DlResources.peek(sParentResource);
  }

  private static void throwIfNotInitialized() {
    if (sParentLoader == null || sParentResource == null) {
      throw new IllegalStateException("Did you miss calling PluginManager#initialize() ?");
    }
  }
}
