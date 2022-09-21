package com.binlee.dl.host;

import android.app.Application;
import android.content.res.Resources;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.binlee.dl.host.hook.DlHandlerCallback;
import com.binlee.dl.host.hook.DlInstrumentation;
import com.binlee.dl.host.hook.DlHooks;
import java.util.List;

/**
 * Created on 2022/8/16
 *
 * @author binlee
 */
public final class DlManager {

  private static final String TAG = "DlManager";

  // 管理
  // 1、插件的加载和卸载（资源和 dex）

  /** 宿主 application */
  private static Application sHost;
  private static ClassLoader sHostLoader;
  private static Resources sHostResources;

  private DlManager() {
    //no instance
  }

  /**
   * 初始化
   *
   * @param host 宿主 app
   * @param loader 宿主类加载器
   * @param resources 宿主资源
   */
  public static void init(Application host, ClassLoader loader, Resources resources) {
    Log.d(TAG, "init() host: " + host + ", loader: " + loader + ", res: " + resources);
    sHost = host;
    sHostLoader = loader;
    sHostResources = resources;
    // hook instrumentation & ams
    DlHooks.setInstrumentation(new DlInstrumentation());
    DlHooks.setHandlerCallback(new DlHandlerCallback());
  }

  /**
   * 安装插件
   *
   * @param pluginPath 插件路径
   */
  public static void install(String pluginPath) {
    throwIfNotInitialized();
    DlLoaders.install(sHost, pluginPath);
    DlResources.install(sHost, pluginPath);
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
    return DlLoaders.peek(sHostLoader).loadClass(classname);
  }

  /**
   * 获取资源，包含宿主和插件
   *
   * @return {@link Resources}
   */
  public static Resources resources() {
    throwIfNotInitialized();
    return DlResources.peek(sHostResources);
  }

  private static void throwIfNotInitialized() {
    if (sHost == null) {
      throw new IllegalStateException("Did you miss calling PluginManager#initialize() ?");
    }
  }
}
