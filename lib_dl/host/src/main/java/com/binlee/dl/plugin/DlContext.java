package com.binlee.dl.plugin;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import com.binlee.dl.DlManager;

/**
 * Created on 2022/9/22
 *
 * @author binlee
 */
public class DlContext extends ContextWrapper {

  private final DlApk mDlApk;

  public DlContext(Context base, DlApk dlApk) {
    super(base);
    mDlApk = dlApk;
  }

  @Override public PackageManager getPackageManager() {
    return DlManager.get().getPackageManager();
  }

  @Override public Context getApplicationContext() {
    return mDlApk.getApplicationContext();
  }

  @Override public ClassLoader getClassLoader() {
    return mDlApk.getClassLoader();
  }

  @Override public Resources getResources() {
    return mDlApk.getResources();
  }

  @Override public AssetManager getAssets() {
    return mDlApk.getAssets();
  }

  @Override public Resources.Theme getTheme() {
    return mDlApk.getTheme();
  }
}
