package com.binlee.dl.plugin;

import android.annotation.SuppressLint;
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
import android.content.res.Resources;
import android.util.Log;
import android.view.ContextThemeWrapper;
import androidx.appcompat.app.AppCompatActivity;
import com.binlee.dl.host.hook.DlHooks;
import com.binlee.dl.host.hook.DlInstrumentation;
import dalvik.system.PathClassLoader;
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
  }

  private void initialize() {
    // 创建类加载器
    makeClassLoader();

    // 创建资源
    makeResource(mHostContext.getResources());
    mThemeContext = new DlThemeContext(mContext, mResources, mPackageInfo.applicationInfo.theme);

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
    // 1、利用系统 pms 创建 Resources，可兼容不同的 rom
    try {
      mResources = mHostContext.getPackageManager().getResourcesForApplication(mPackageInfo.applicationInfo);
      Log.d(TAG, "makeResource() resources from pms: " + mResources);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }

    // 2、创建 asset manager 并添加资源
    final AssetManager assets;
    try {
      assets = AssetManager.class.newInstance();
      @SuppressLint("DiscouragedPrivateApi")
      final Method method = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
      method.setAccessible(true);
      final Object obj = method.invoke(assets, mPluginPath);
      Log.d(TAG, "makeResource() addAssetPath: " + obj);
    } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
      Log.w(TAG, "makeResource() failed to init AssetManager", e);
      return;
    }
    // 使用 asset manager 构造 resources，使生成 mResourceImpl
    Resources resources = new Resources(assets, parent.getDisplayMetrics(), parent.getConfiguration());

    dumpResources(resources, "before replace ");

    // 3、偷梁换柱，替换掉 mResourceImpl
    // public android.content.res.ResourcesImpl android.content.res.Resources.getImpl()
    // public void android.content.res.Resources.setImpl(android.content.res.ResourcesImpl)
    try {
      @SuppressLint("DiscouragedPrivateApi")
      final Method getImpl = Resources.class.getDeclaredMethod("getImpl");
      getImpl.setAccessible(true);
      final Object resourceImpl = getImpl.invoke(resources);
      Log.d(TAG, "makeResource() getImpl return type: " + getImpl.getReturnType() + ", returns: " + resourceImpl);

      @SuppressLint("DiscouragedPrivateApi")
      final Method setImpl = Resources.class.getDeclaredMethod("setImpl", getImpl.getReturnType());
      setImpl.setAccessible(true);
      setImpl.invoke(mResources, resourceImpl);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }

    // 替换 mClassLoader，用于初始化自定义 drawable
    try {
      final Field field = Resources.class.getDeclaredField("mClassLoader");
      field.setAccessible(true);
      field.set(mResources, mClassLoader);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }

    dumpResources(mResources, "after replace ");

    Log.d(TAG, "makeResource() parent: " + parent + ", plugin: " + mResources);

    // 是否与宿主资源合并？
    // 合并后资源冲突如何解决？
  }

  private void dumpResources(Resources resources, String prefix) {
    try {
      int layoutId = resources.getIdentifier("activity_plugin", "layout", mPackageInfo.packageName);
      Log.d(TAG, prefix + "dumpResources() R.layout.activity_plugin: " + layoutId);
      Log.d(TAG, prefix + "dumpResources() layout: " + resources.getLayout(layoutId));
      final int stringId = resources.getIdentifier("app_name", "string", mPackageInfo.packageName);
      Log.d(TAG, prefix + "dumpResources() R.string.app_name: " + layoutId);
      Log.d(TAG, prefix + "dumpResources() app_name: " + resources.getString(stringId));
    } catch (Resources.NotFoundException e) {
      e.printStackTrace();
    }
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

  public Resources getResources() {
    return mResources;
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

    // plugin's Activity#mBase -> DlThemeContext
    // DlThemeContext#mBase -> DlContext
    // DlContext#mBase -> host application

    // 注入相关属性：application、baseContext、resource、theme
    // android.app.Activity#mApplication
    // android.content.ContextWrapper#mBase
    dumpActivity(activity, "before inject ");

    Field field;
    // try inject Activity#mApplication
    try {
      field = Activity.class.getDeclaredField("mApplication");
      field.setAccessible(true);
      field.set(activity, mApplication);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }

    // base context chain:
    // androidx.appcompat.view.ContextThemeWrapper -> android.app.ContextImpl
    // com.binlee.dl.plugin.DlThemeContext -> com.binlee.dl.plugin.DlContext -> com.binlee.learning.MainApp -> android.app.ContextImpl
    // try inject ContextWrapper#mBase
    try {
      field = ContextWrapper.class.getDeclaredField("mBase");
      field.setAccessible(true);
      field.set(activity, mThemeContext);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }

    Context base = activity.getBaseContext();
    if (base instanceof androidx.appcompat.view.ContextThemeWrapper) {
      // ContextImpl
      base = ((androidx.appcompat.view.ContextThemeWrapper) base).getBaseContext();
    }

    activity.setTheme(mThemeContext.getThemeResource());

    // try inject AppCompatActivity/ContextThemeWrapper#mResource
    try {
      boolean injected = false;
      try {
        final Class<?> clazz = Class.forName("androidx.appcompat.widget.ResourcesWrapper");
        if (clazz.isAssignableFrom(activity.getResources().getClass())) {
          field = clazz.getDeclaredField("mResources");
          field.setAccessible(true);
          field.set(activity.getResources(), mResources);
          injected = true;
        }
      } catch (ClassNotFoundException ignored) {
      }
      if (!injected) {
        if (activity instanceof AppCompatActivity) {
          field = AppCompatActivity.class.getDeclaredField("mResources");
        } else/* if (activity instanceof ContextThemeWrapper)*/ {
          field = ContextThemeWrapper.class.getDeclaredField("mResources");
        }
        field.setAccessible(true);
        field.set(activity, mResources);
      }
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }

    dumpActivity(activity, "after inject ");
  }

  private void dumpActivity(Activity activity, String prefix) {
    Log.d(TAG, prefix + "dumpActivity() " + activity
      + ", \npackage manager: " + activity.getPackageManager()
      + ", \nbase context chain: " + getContextChain(activity)
      + ", \napp context: " + activity.getApplicationContext()
      + ", \napplication: " + activity.getApplication()
      + ", \nclassloader: " + activity.getClassLoader()
      + ", \nresource: " + activity.getResources()
      + ", \ntheme: " + activity.getTheme()
    );
  }

  private String getContextChain(Activity activity) {
    StringBuilder buffer = new StringBuilder();
    Context context = activity.getBaseContext();
    buffer.append(context).append("->");
    while (context instanceof ContextWrapper) {
      context = ((ContextWrapper) context).getBaseContext();
      buffer.append(context).append("->");
    }
    return buffer.toString();
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
