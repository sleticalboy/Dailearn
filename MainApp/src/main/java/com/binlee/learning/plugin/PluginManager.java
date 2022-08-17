package com.binlee.learning.plugin;

import android.content.res.Resources;
import java.util.List;

/**
 * Created on 2022/8/16
 *
 * @author binlee
 */
public final class PluginManager {

  // 管理
  // 1、插件的加载和卸载（资源和 dex）

  public static final class Config {
    /** 宿主资源 */
    private Resources parentResource;
    /** 宿主类加载器 */
    private ClassLoader parentLoader;

    public void setParentResource(Resources parent) {
      this.parentResource = parent;
    }

    public void setParentClassLoader(ClassLoader parent) {
      this.parentLoader = parent;
    }
  }

  private static Config sConfig;

  private PluginManager() {
    //no instance
  }

  /**
   * 初始化
   *
   * @param config 配置
   */
  public static void initialized(Config config) {
    sConfig = config;
  }

  /**
   * 安装插件
   *
   * @param pluginPath 插件路径
   */
  public static void install(String pluginPath) {
    throwIfNotInitialized();
    PluginLoaders.install(pluginPath, sConfig.parentLoader);
    PluginResources.install(pluginPath, sConfig.parentResource);
  }

  /**
   * 卸载插件
   *
   * @param pluginPath 插件路径
   */
  public static void uninstall(String pluginPath) {
    PluginLoaders.remove(pluginPath);
    PluginResources.remove(pluginPath);
  }

  public static List<String> getAll() {
    return PluginLoaders.collectAll();
  }

  /**
   * 获取类加载程序
   *
   * @return {@link ClassLoader}
   */
  public static ClassLoader getClassLoader() {
    throwIfNotInitialized();
    final ClassLoader classLoader = PluginLoaders.peek();
    return classLoader == null ? sConfig.parentLoader : classLoader;
  }

  /**
   * 获取资源，包含宿主和插件
   *
   * @return {@link Resources}
   */
  public static Resources getResources() {
    throwIfNotInitialized();
    final Resources resources = PluginResources.peek();
    return resources == null ? sConfig.parentResource : resources;
  }

  private static void throwIfNotInitialized() {
    if (sConfig == null) throw new IllegalStateException("Did you miss calling PluginManager#initialize() ?");
  }
}
