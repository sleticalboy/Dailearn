package com.binlee.dl.plugin;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import androidx.appcompat.app.AppCompatActivity;
import com.binlee.dl.host.hook.DlHooks;
import com.binlee.dl.host.hook.DlInstrumentation;
import dalvik.system.PathClassLoader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
  private PackageParser.Package mOwnPackage;
  private PackageInfo mPackageInfo;
  private DlContext mContext;
  private DlThemeContext mThemeContext;
  private Application mApplication;
  private DlActivityLifecycleCallbacks mActivityLifecycleCallbacks;
  private List<BroadcastReceiver> mReceivers = new ArrayList<>();

  // class loader
  private ClassLoader mClassLoader;
  // resources
  private Resources mResources;

  private String mPluginPath;

  public static DlApk load(PackageManager pm, String pluginPath) {
    final DlApk dlApk = new DlApk(pm, pluginPath);
    dlApk.initialize();
    return dlApk;
  }

  // 模拟 PMS 应用安装流程
  // 1、解析插件包，获取 application、activity、service、receiver、provider 等信息
  // 2、创建类加载器
  // 3、创建资源
  private DlApk(PackageManager pm, String pluginPath) {
    mHostContext = ((DlPackageManager) pm).getHost();
    mOwnPackage = DlPackageUtil.parsePackage(pluginPath);
    mPackageInfo = DlPackageUtil.generatePackageInfo(mHostContext, mOwnPackage, pluginPath);
    mPluginPath = pluginPath;
    mContext = new DlContext(this, pm);
    if (mOwnPackage.activities.size() > 0) {
      // 创建资源
      makeResource(mHostContext.getResources());
      mThemeContext = new DlThemeContext(mContext, mResources, mPackageInfo.applicationInfo.theme);
    }
  }

  private void initialize() {
    // 创建类加载器
    makeClassLoader();

    // 安装 provider
    final List<ProviderInfo> providers = new ArrayList<>(mOwnPackage.providers.size());
    for (PackageParser.Provider provider : mOwnPackage.providers) {
      providers.add(provider.info);
    }
    installProviders(providers);

    // 创建 application
    makeApplication(mPackageInfo.applicationInfo.className);

    // 注册静态广播
    registerReceivers(mOwnPackage.receivers);
  }

  private void installProviders(List<ProviderInfo> providers) {
    // private void installContentProviders(Context context, List<ProviderInfo> providers) {}
    // android.app.ActivityThread#installContentProviders

    final Object currentAt = DlHooks.getActivityThread();
    try {
      Method method = currentAt.getClass().getDeclaredMethod("installContentProviders", Context.class, List.class);
      method.setAccessible(true);
      method.invoke(currentAt, mContext, providers);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  private void registerReceivers(List<PackageParser.Activity> receivers) {
    for (PackageParser.Activity activity : receivers) {
      BroadcastReceiver receiver;
      try {
        receiver = (BroadcastReceiver) mClassLoader.loadClass(activity.className).newInstance();
      } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
        continue;
      }
      IntentFilter filter = new IntentFilter();
      for (PackageParser.ActivityIntentInfo intent : activity.intents) {
        for (int i = 0; i < intent.countActions(); i++) {
          filter.addAction(intent.getAction(i));
        }
      }
      final Intent intent = mContext.registerReceiver(receiver, filter);
      mReceivers.add(receiver);
      Log.d(TAG, "registerReceiver() " + activity.className + ", intent: " + intent);
    }
  }

  private void unregisterReceivers() {
    for (int i = 0; i < mReceivers.size(); i++) {
      mContext.unregisterReceiver(mReceivers.remove(0));
    }
  }

  private void makeClassLoader() {
    if (mClassLoader != null) return;

    mClassLoader = new PathClassLoader(mPluginPath, mHostContext.getClassLoader());
    // 是否与宿主类合并？
  }

  private void makeResource(Resources parent) {
    if (Boolean.TRUE) {
      try {
        mResources = mHostContext.getPackageManager().getResourcesForApplication(mPackageInfo.applicationInfo);
      } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
      }
      Log.d(TAG, "makeResource() resources from parent pms: " + mResources);
      return;
    }

    final AssetManager assets;
    try {
      assets = AssetManager.class.newInstance();
      final Method method = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
      method.setAccessible(true);
      final Object obj = method.invoke(assets, mPluginPath);
      Log.d(TAG, "makeResource() addAssetPath: " + obj);
    } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
      e.printStackTrace();
      return;
    }
    // miui: android.content.res.MiuiResources
    // vivo: android.content.res.VivoResources
    // nubia: android.content.res.NubiaResources
    // aosp: android.content.res.Resources
    try {
      final Constructor<? extends Resources> constructor =
        parent.getClass().getDeclaredConstructor(AssetManager.class, DisplayMetrics.class, Configuration.class);
      constructor.setAccessible(true);
      mResources = constructor.newInstance(assets, parent.getDisplayMetrics(), parent.getConfiguration());
    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
      Log.w(TAG, "makeResource() failed, no such method: " + e.getMessage());
      mResources = new Resources(assets, parent.getDisplayMetrics(), parent.getConfiguration());
    }
    Log.d(TAG, "makeResource() parent: " + parent + ", plugin: " + mResources);
    // 是否与宿主资源合并？
    // 合并后资源冲突如何解决？
  }

  private void makeApplication(String className) {
    // check dl application class name
    if (className == null) {
      className = "android.app.Application";
    }
    Log.d(TAG, "makeApplication() " + className);

    final DlInstrumentation inst = DlHooks.getInstrumentation();
    try {
      mApplication = inst.newApplication(mClassLoader, className, mContext);
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

  public String getPackageName() {
    return mPackageInfo.packageName;
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

  public void release() {
    unregisterReceivers();
    // 移除 ActivityLifecycleCallbacks
    mApplication.unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
    // 解除注册
    DlHooks.getInstrumentation().removeCallbacks(this);

    mOwnPackage = null;
    mPackageInfo = null;
    mHostContext = null;
    mContext = null;
    mActivityLifecycleCallbacks = null;
    mApplication = null;
    mReceivers = null;

    // 卸载插件类
    mClassLoader = null;
    // 释放资源
    mResources.getAssets().close();
    mResources = null;
    mPluginPath = null;
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

    Field field;
    // try inject Activity#mApplication
    try {
      field = Activity.class.getDeclaredField("mApplication");
      field.setAccessible(true);
      field.set(activity, mApplication);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }

    // try inject ContextWrapper#mBase
    try {
      field = ContextWrapper.class.getDeclaredField("mBase");
      field.setAccessible(true);
      field.set(activity, mThemeContext);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }

    // try inject AppCompatActivity/ContextThemeWrapper#mResource
    try {
      if (activity instanceof AppCompatActivity) {
        field = AppCompatActivity.class.getDeclaredField("mResources");
      } else/* if (activity instanceof ContextThemeWrapper)*/ {
        field = ContextThemeWrapper.class.getDeclaredField("mResources");
      }
      field.setAccessible(true);
      field.set(activity, mResources);
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

  public ActivityInfo resolveActivity(ComponentName component, int flags) {
    for (ActivityInfo info : mPackageInfo.activities) {
      if (component.getClassName().equals(info.name)) {
        return info;
      }
    }
    return null;
  }

  public ActivityInfo resolveReceiver(ComponentName component, int flags) {
    for (ActivityInfo info : mPackageInfo.receivers) {
      if (component.getClassName().equals(info.name)) {
        return info;
      }
    }
    return null;
  }

  public ServiceInfo resolveService(ComponentName component, int flags) {
    for (ServiceInfo info : mPackageInfo.services) {
      if (component.getClassName().equals(info.name)) {
        return info;
      }
    }
    return null;
  }

  public ProviderInfo resolveProvider(ComponentName component, int flags) {
    for (ProviderInfo info : mPackageInfo.providers) {
      if (component.getClassName().equals(info.name)) {
        return info;
      }
    }
    return null;
  }
}
