package com.binlee.dl;

import android.app.Application;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.binlee.dl.host.hook.DlActivityManager;
import com.binlee.dl.host.hook.DlHandlerCallback;
import com.binlee.dl.host.hook.DlHooks;
import com.binlee.dl.host.hook.DlInstrumentation;
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
    if (mPm != null) return;
    Log.d(TAG, "init() host application: " + host);
    mPm = new DlPackageManager(host);
    // hook instrumentation & ams
    DlHooks.setInstrumentation(new DlInstrumentation());
    DlHooks.setHandlerCallback(new DlHandlerCallback());
    DlHooks.setActivityManager(new DlActivityManager(host, mPm));
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
    throwIfNotInitialized();
    final long start = System.currentTimeMillis();
    mPm.install(pluginPath);
    Log.d(TAG, "install() pluginPath: " + pluginPath + ", cost: " + (System.currentTimeMillis() - start) + "ms");
  }

  /**
   * 卸载插件
   *
   * @param pluginPath 插件路径
   */
  public void uninstall(String pluginPath) {
    throwIfNotInitialized();
    final long start = System.currentTimeMillis();
    mPm.uninstall(pluginPath);
    Log.d(TAG, "uninstall() pluginPath: " + pluginPath + ", cost: " + (System.currentTimeMillis() - start) + "ms");
  }

  /**
   * 加载类
   *
   * @param classname 类名称
   * @return {@link Class}<{@link ?}>
   * @throws ClassNotFoundException
   */
  @NonNull
  public Class<?> loadClass(@NonNull String classname) throws ClassNotFoundException {
    throwIfNotInitialized();
    return mPm.loadClass(classname);
  }

  private void throwIfNotInitialized() {
    if (mPm == null) {
      throw new IllegalStateException("Did you miss calling PluginManager#initialize() ?");
    }
  }
}
