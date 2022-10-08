package com.binlee.dl.plugin;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * Created on 2022/9/22
 *
 * @author binlee
 */
public final class DlContext extends ContextWrapper {

  private final DlApk mDlApk;
  private final PackageManager mPm;

  public DlContext(DlApk dlApk, PackageManager pm) {
    super(((DlPackageManager) pm).getHost());
    mDlApk = dlApk;
    mPm = pm;
  }

  @Override public PackageManager getPackageManager() {
    return mPm;
  }

  @Override public ApplicationInfo getApplicationInfo() {
    return mDlApk.getApplicationInfo();
  }

  @Override public Context getApplicationContext() {
    return mDlApk.getApplicationContext();
  }

  @Override public ClassLoader getClassLoader() {
    return mDlApk.getClassLoader();
  }

  @Override
  public String getPackageName() {
    return mDlApk.getPackageName();
  }
}
