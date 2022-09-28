package com.binlee.dl.plugin;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import androidx.annotation.Nullable;

/**
 * Created on 2022/9/22
 *
 * @author binlee
 */
public class DlContext extends ContextWrapper {

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

  @Override public Resources getResources() {
    return mDlApk.getResources();
  }

  @Override public AssetManager getAssets() {
    return mDlApk.getAssets();
  }

  @Override public Resources.Theme getTheme() {
    return mDlApk.getTheme();
  }

  @Override public void startActivity(Intent intent) {
    super.startActivity(intent);
  }

  @Override public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter) {
    return super.registerReceiver(receiver, filter);
  }

  @Nullable @Override public ComponentName startService(Intent service) {
    return super.startService(service);
  }

  @Nullable @Override public ComponentName startForegroundService(Intent service) {
    return super.startForegroundService(service);
  }

  @Override public boolean bindService(Intent service, ServiceConnection conn, int flags) {
    return super.bindService(service, conn, flags);
  }

  @Override public ContentResolver getContentResolver() {
    return super.getContentResolver();
  }
}
