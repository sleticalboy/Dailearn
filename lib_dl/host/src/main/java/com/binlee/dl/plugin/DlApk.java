package com.binlee.dl.plugin;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import android.view.ContextThemeWrapper;
import com.binlee.dl.host.hook.DlHooks;
import com.binlee.dl.host.hook.DlInstrumentation;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2022/9/22
 *
 * @author binlee
 */
public final class DlApk implements DlInstrumentation.Callbacks {

  private static final String TAG = "DlApk";

  // 宿主 application
  private Context mHostContext;

  // 插件相关信息
  private PackageInfo mPackageInfo;
  private DlContext mBaseContext;
  private Application mApplication;
  private DlActivityLifecycleCallbacks mActivityLifecycleCallbacks;

  // class loader
  private ClassLoader mClassLoader;
  // resources
  private Resources mResources;

  // 已注册的 receivers
  private Map<String, BroadcastReceiver> mRegisteredReceivers = new HashMap<>();

  private String mPluginPath;

  // 模拟 PMS 应用安装流程
  // 1、解析插件包，获取 application、activity、service、receiver、provider 等信息
  // 2、创建类加载器
  // 3、创建资源
  public DlApk(PackageManager pm, PackageInfo src, String pluginPath) {
    mHostContext = ((DlPackageManager) pm).getHost();
    mPackageInfo = src;
    mPluginPath = pluginPath;
    mBaseContext = new DlContext(this, pm);

    initialize();
  }

  private void initialize() {
    // 创建类加载器
    makeClassLoader();
    // 创建 application
    makeApplication(mPackageInfo.applicationInfo.className);
    // 创建资源
    makeResource();
  }

  private void makeClassLoader() {
    if (mClassLoader != null) return;

    mClassLoader = new PathClassLoader(mPluginPath, mHostContext.getClassLoader());
    // 是否与宿主类合并？
  }

  private void makeResource() {
    try {
      mResources = mHostContext.getPackageManager().getResourcesForApplication(mPackageInfo.applicationInfo);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    // 是否与宿主资源合并？
    // 资源冲突如何解决？
  }

  private void makeApplication(String className) {
    // check dl application class name
    if (className == null) {
      className = "android.app.Application";
    }
    Log.d(TAG, "makeApplication() " + className);

    final DlInstrumentation inst = DlHooks.getInstrumentation();
    try {
      mApplication = inst.newApplication(mClassLoader, className, mBaseContext);
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
    }
    inst.callApplicationOnCreate(mApplication);
    // 通过 host 的 ActivityLifecycleCallbacks 将事件分发给 plugin
    mActivityLifecycleCallbacks = new DlActivityLifecycleCallbacks(((Application) mHostContext));
    mApplication.registerActivityLifecycleCallbacks(this.mActivityLifecycleCallbacks);

    // hook onNewActivity 事件
    inst.addCallbacks(this);
  }

  public PackageInfo getPackageInfo() {
    return mPackageInfo;
  }

  public ApplicationInfo getApplicationInfo() {
    return mPackageInfo.applicationInfo;
  }

  public Context getApplicationContext() {
    return mApplication;
  }

  public ClassLoader getClassLoader() {
    return mClassLoader;
  }

  public Resources getResources() {
    return mResources;
  }

  public AssetManager getAssets() {
    return mResources.getAssets();
  }

  public Resources.Theme getTheme() {
    final Resources.Theme theme = mResources.newTheme();
    theme.applyStyle(mPackageInfo.applicationInfo.theme, false);
    return theme;
  }

  public void release() {
    // 移除 receivers
    for (String key : mRegisteredReceivers.keySet()) {
      mHostContext.unregisterReceiver(mRegisteredReceivers.remove(key));
    }
    mRegisteredReceivers = null;

    mPackageInfo = null;
    mHostContext = null;
    mBaseContext = null;

    // 移除 ActivityLifecycleCallbacks
    mApplication.unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
    mActivityLifecycleCallbacks = null;
    mApplication = null;

    // 卸载插件类
    mClassLoader = null;
    // 释放资源
    mResources.getAssets().close();
    mResources = null;

    // 解除注册
    DlHooks.getInstrumentation().removeCallbacks(this);

    mPluginPath = null;
  }

  @Override public void onNewActivity(Activity activity) {
    if (activity == null) return;
    try {
      final Field field = ContextThemeWrapper.class.getDeclaredField("mResources");
      field.setAccessible(true);
      field.set(activity, mResources);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  @Override public void onCallActivityOnCreate(Activity activity) {
    if (activity == null) return;
    // 注入相关属性：application、baseContext、resource、theme
    // android.app.Activity#mApplication
    // android.content.ContextWrapper#mBase
    Log.d(TAG, "onCallActivityOnCreate() before: " + activity
      + ", \npackage manager: " + activity.getPackageManager()
      + ", \nbase context: " + activity.getBaseContext()
      + ", \napp context: " + activity.getApplicationContext()
      + ", \napplication: " + activity.getApplication()
      + ", \nclassloader: " + activity.getClassLoader()
      + ", \nresource: " + activity.getResources()
      + ", \ntheme: " + activity.getTheme()
    );

    try {
      Field field = Activity.class.getDeclaredField("mApplication");
      field.setAccessible(true);
      field.set(activity, mApplication);

      field = ContextWrapper.class.getDeclaredField("mBase");
      field.setAccessible(true);
      field.set(activity, mBaseContext);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }

    Log.d(TAG, "onCallActivityOnCreate() after: " + activity
      + ", \npackage manager: " + activity.getPackageManager()
      + ", \nbase context: " + activity.getBaseContext()
      + ", \napp context: " + activity.getApplicationContext()
      + ", \napplication: " + activity.getApplication()
      + ", \nclassloader: " + activity.getClassLoader()
      + ", \nresource: " + activity.getResources()
      + ", \ntheme: " + activity.getTheme()
    );
  }
}
