package com.binlee.dl;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.util.Log;
import androidx.annotation.NonNull;
import com.binlee.dl.hook.DlActivityManager;
import com.binlee.dl.hook.DlHandlerCallback;
import com.binlee.dl.hook.DlHooks;
import com.binlee.dl.hook.DlInstrumentation;
import com.binlee.dl.internal.DlActivities;
import com.binlee.dl.internal.DlPackageManager;
import com.binlee.dl.internal.DlServices;

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
    // hook instrumentation、Handler#mCallback
    DlHooks.setInstrumentation(new DlInstrumentation());
    DlHooks.setHandlerCallback(new DlHandlerCallback());
    DlHooks.setActivityManager(new DlActivityManager());
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
   */
  @NonNull
  public Class<?> loadClass(@NonNull String classname) throws ClassNotFoundException {
    throwIfNotInitialized();
    return mPm.loadClass(classname);
  }

  ///////////////////////////////////////////////////////////////////////////
  // activity api
  ///////////////////////////////////////////////////////////////////////////

  public static void startActivity(@NonNull Context context, @NonNull ComponentName target) {
    checkComponent(target);
    DlActivities.start(context, target);
  }

  ///////////////////////////////////////////////////////////////////////////
  // service api
  ///////////////////////////////////////////////////////////////////////////

  public static void startService(Context context, ComponentName target) {
    checkComponent(target);
    DlServices.start(context, target);
  }

  public static void stopService(Context context, ComponentName target) {
    checkComponent(target);
    DlServices.stop(context, target);
  }

  public static void bindService(Context context, ComponentName target, ServiceConnection connection) {
    checkComponent(target);
    DlServices.bind(context, target, connection);
  }

  public static void unbindService(Context context, ComponentName target, ServiceConnection connection) {
    checkComponent(target);
    DlServices.unbind(context, target, connection);
  }

  private static void checkComponent(ComponentName target) {
    if (!DL_MANAGER.mPm.hasPlugin(target.getPackageName())) {
      throw new PluginNotFoundException(target.getPackageName());
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // internal check
  ///////////////////////////////////////////////////////////////////////////

  private void throwIfNotInitialized() {
    if (mPm == null) {
      throw new IllegalStateException("Did you miss calling PluginManager#initialize() ?");
    }
  }

  public static class PluginNotFoundException extends RuntimeException {

    public PluginNotFoundException(String packageName) {
      super("package " + packageName);
    }
  }
}
