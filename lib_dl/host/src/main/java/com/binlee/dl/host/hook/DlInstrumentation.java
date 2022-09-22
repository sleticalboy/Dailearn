package com.binlee.dl.host.hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import com.binlee.dl.DlManager;
import com.binlee.dl.DlConst;

/**
 * Created on 2022/9/21
 *
 * @author binlee
 */
public final class DlInstrumentation extends Instrumentation {

  private static final String TAG = "DlInstrumentation";

  private Instrumentation mDelegate;

  public DlInstrumentation() {
  }

  public void setDelegate(Object delegate) {
    mDelegate = (Instrumentation) delegate;
  }

  @Override public Activity newActivity(ClassLoader cl, String className, Intent intent)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    // 真正要启动的组件
    final ComponentName target = intent.getParcelableExtra(DlConst.REAL_COMPONENT);
    if (target != null) {
      final Class<?> clazz = DlManager.loadClass(target.getClassName());
      Log.d(TAG, "newActivity() proxy: " + className + ", target: " + target);
      if (clazz != null) {
        return (Activity) clazz.newInstance();
      }
    } else {
      return mDelegate.newActivity(cl, className, intent);
    }
    return null;
  }

  @Override public boolean onException(Object obj, Throwable e) {
    return mDelegate.onException(obj, e);
  }
}
