package com.binlee.dl.plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import com.binlee.dl.DlManager;
import dalvik.system.PathClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2022/9/22
 *
 * @author binlee
 */
public final class DlApk {

  private PackageInfo mPackageInfo;
  private Context mHostContext;
  private DlContext mContext;

  private final Object mClassLoaderLock = new Object();
  private ClassLoader mClassLoader;

  private Map<String, BroadcastReceiver> mRegisteredReceivers = new HashMap<>();

  private String mPluginPath;

  // 已安装的插件信息
  // 签名、activities、services、receivers、providers、instrumentations
  public DlApk(Context host, PackageInfo src, String pluginPath) {
    mHostContext = host;
    mPackageInfo = src;
    mPluginPath = pluginPath;
    mContext = new DlContext(host, this);

    initialize();
  }

  private void initialize() {
    // 注册 receivers
    if (mPackageInfo.receivers == null || mPackageInfo.receivers.length == 0) return;
    for (ActivityInfo info : mPackageInfo.receivers) {
      try {
        final Class<?> clazz = DlManager.loadClass(info.name);
        if (clazz == null) {
          // log warning
          continue;
        }
        final BroadcastReceiver receiver = (BroadcastReceiver) clazz.newInstance();
        IntentFilter filter = new IntentFilter();
        mHostContext.registerReceiver(receiver, filter);
        mRegisteredReceivers.put(info.name, receiver);
      } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
        e.printStackTrace();
        // log warning
      }
    }
    // 创建 application
    // mPackageInfo.applicationInfo.className
  }

  public PackageInfo getPackageInfo() {
    return mPackageInfo;
  }

  public Context getApplicationContext() {
    return mContext;
  }

  public ClassLoader getClassLoader() {
    synchronized (mClassLoaderLock) {
      if (mClassLoader == null) {
        createClassLoader();
      }
      return mClassLoader;
    }
  }

  public Resources getResources() {
    return null;
  }

  public AssetManager getAssets() {
    return null;
  }

  public Resources.Theme getTheme() {
    return null;
  }

  public void release() {
    // 移除 receivers
    for (Map.Entry<String, BroadcastReceiver> entry : mRegisteredReceivers.entrySet()) {
      mHostContext.unregisterReceiver(entry.getValue());
    }
    mRegisteredReceivers.clear();
    mRegisteredReceivers = null;
    mPackageInfo = null;
    mHostContext = null;
    mContext = null;
    mClassLoader = null;
    mPluginPath = null;
  }

  private void createClassLoader() {
    mClassLoader = new PathClassLoader(mPluginPath, mHostContext.getClassLoader());
  }
}
