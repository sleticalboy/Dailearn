package com.binlee.dl.hook;

import android.app.Activity;
import android.app.AppComponentFactory;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.binlee.dl.DlConst;
import com.binlee.dl.DlManager;

/**
 * Created on 2022/9/27
 *
 * @author binlee
 */
@RequiresApi(api = Build.VERSION_CODES.P)
public final class DlComponentFactory extends AppComponentFactory {

  private static final String TAG = "DlComponentFactory";

  private final AppComponentFactory mDelegate;

  public DlComponentFactory() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      mDelegate = new androidx.core.app.AppComponentFactory();
    } else {
      mDelegate = new AppComponentFactory();
    }
    Log.d(TAG, "DlComponentFactory() delegate: " + mDelegate);
  }

  @RequiresApi(api = Build.VERSION_CODES.Q) @NonNull @Override
  public ClassLoader instantiateClassLoader(@NonNull ClassLoader cl, @NonNull ApplicationInfo aInfo) {
    Log.d(TAG, "instantiateClassLoader() app Info: " + aInfo);
    return mDelegate.instantiateClassLoader(cl, aInfo);
  }

  @NonNull @Override public Application instantiateApplication(@NonNull ClassLoader cl, @NonNull String className)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    Log.d(TAG, "instantiateApplication() className: " + className);
    return mDelegate.instantiateApplication(cl, className);
  }

  @NonNull @Override
  public Activity instantiateActivity(@NonNull ClassLoader cl, @NonNull String className, @Nullable Intent intent)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    Log.d(TAG, "instantiateActivity() className: " + className + ", intent: " + intent );
    // 真正要启动的组件
    final ComponentName target;
    if (intent != null && (target = intent.getParcelableExtra(DlConst.REAL_COMPONENT)) != null) {
      Log.d(TAG, "instantiateActivity() proxy: " + className + ", target: " + target);
      return (Activity) DlManager.get().loadClass(target.getClassName()).newInstance();
    }
    return mDelegate.instantiateActivity(cl, className, intent);
  }

  @NonNull @Override
  public Service instantiateService(@NonNull ClassLoader cl, @NonNull String className, @Nullable Intent intent)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    Log.d(TAG, "instantiateService() className: " + className + ", intent: " + intent);
    try {
      return mDelegate.instantiateService(cl, className, intent);
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      Log.d(TAG, "instantiateService() plugin: " + className);
      return (Service) DlManager.get().loadClass(className).newInstance();
    }
  }

  @NonNull @Override public BroadcastReceiver instantiateReceiver(@NonNull ClassLoader cl, @NonNull String className,
    @Nullable Intent intent) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    Log.d(TAG, "instantiateReceiver() className: " + className + ", intent: " + intent);
    return mDelegate.instantiateReceiver(cl, className, intent);
  }

  @NonNull @Override public ContentProvider instantiateProvider(@NonNull ClassLoader cl, @NonNull String className)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    Log.d(TAG, "instantiateProvider() className: " + className);
    return mDelegate.instantiateProvider(cl, className);
  }
}
