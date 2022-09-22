package com.binlee.dl;

import android.app.Application;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.binlee.dl.host.hook.DlHandlerCallback;
import com.binlee.dl.host.hook.DlInstrumentation;
import com.binlee.dl.host.hook.DlHooks;
import com.binlee.dl.plugin.DlPackageManager;
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

  private static final DlManager DL_MANAGER = new DlManager();

  private DlPackageManager mPm;

  private DlManager() {
  }

  public static DlManager get() {
    return DL_MANAGER;
  }

  /**
   * 初始化
   *
   * @param host 宿主 app
   */
  public void init(Application host) {
    Log.d(TAG, "init() host: " + host);
    mPm = new DlPackageManager(host);
    sHost = host;
    sHostLoader = host.getClassLoader();
    sHostResources = host.getResources();
    // hook instrumentation & ams
    DlHooks.setInstrumentation(new DlInstrumentation());
    DlHooks.setHandlerCallback(new DlHandlerCallback());
    // hook pms
    final PackageManager pm = host.getPackageManager(); // -> ApplicationPackageManager
    // android.app.ApplicationPackageManager#mPM -> android.app.ActivityThread#getPackageManager() -> pms
    // com.android.server.pm.PackageManagerService#mPackages -> WatchedArrayMap<String, AndroidPackage>(implements Map)
    // pms 解析插件并将数据插入到 mPackages 中
  }

  /**
   * 安装插件
   *
   * @param pluginPath 插件路径
   */
  public void install(String pluginPath) {
    Log.d(TAG, "install() called with: pluginPath = [" + pluginPath + "]");
    throwIfNotInitialized();
    DlLoaders.install(sHost, pluginPath);
    DlResources.install(sHost, pluginPath);
    mPm.install(pluginPath);
  }

  /**
   * 卸载插件
   *
   * @param pluginPath 插件路径
   */
  public void uninstall(String pluginPath) {
    Log.d(TAG, "uninstall() called with: pluginPath = [" + pluginPath + "]");
    DlLoaders.remove(pluginPath);
    DlResources.remove(pluginPath);
    mPm.uninstall(pluginPath);
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

  public DlPackageManager getPackageManager() {
    return mPm;
  }
}
