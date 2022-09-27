package com.binlee.dl.host.hook;

import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.Log;
import com.binlee.dl.DlConst;
import com.binlee.dl.plugin.DlPackageManager;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created on 2022/9/27
 *
 * @author binlee
 */
public final class DlActivityManager implements InvocationHandler {

  private static final String TAG = "DlInvocationHandler";
  private final Context mHostContext;
  private final DlPackageManager mPm;
  private IActivityManager mDelegate;

  public DlActivityManager(Context hostContext, DlPackageManager pm) {
    mHostContext = hostContext;
    mPm = pm;
  }

  public void setDelegate(IActivityManager delegate) {
    mDelegate = delegate;
  }

  public ClassLoader getClassLoader() {
    return mHostContext.getClassLoader();
  }

  @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    switch (method.getName()) {
      case "startService":
        return startService(proxy, method, args);
      case "stopService":
        return stopService(proxy, method, args);
      case "bindService":
        return bindService(proxy, method, args);
      case "unbindService": {
        return unbindService(proxy, method, args);
      }
    }
    return DlConst.AM_METHOD_RESULT_MISSED;
  }

  private Object unbindService(Object proxy, Method method, Object[] args) {
    Log.d(TAG, "unbindService() proxy = ["
      + proxy
      + "], method = ["
      + method
      + "], args = ["
      + Arrays.toString(args)
      + "]");
    return null;
  }

  private Object bindService(Object proxy, Method method, Object[] args) {
    Log.d(TAG, "bindService() proxy = ["
      + proxy
      + "], method = ["
      + method
      + "], args = ["
      + Arrays.toString(args)
      + "]");
    return null;
  }

  private Object stopService(Object proxy, Method method, Object[] args) {
    Log.d(TAG, "stopService() proxy = ["
      + proxy
      + "], method = ["
      + method
      + "], args = ["
      + Arrays.toString(args)
      + "]");
    return null;
  }

  private Object startService(Object proxy, Method method, Object[] args) throws Throwable {
    // public abstract android.content.ComponentName android.app.IActivityManager.startService(
    // android.app.IApplicationThread,android.content.Intent,java.lang.String,boolean,java.lang.String,java.lang.String,int)
    // throws android.os.RemoteException
    Log.d(TAG, "startService() proxy = ["
      + proxy
      + "], method = ["
      + method
      + "], args = ["
      + Arrays.toString(args)
      + "]");
    // 看插件中有没有这个 service，如果没有则通过 host 启动，如果有则通过插件启动
    // 如果 intent 中有真正的 service 就替换掉，没有就跳过
    final Intent intent = (Intent) args[1];
    if (intent.hasExtra(DlConst.REAL_COMPONENT)) {
      intent.setComponent(intent.getParcelableExtra(DlConst.REAL_COMPONENT));
      intent.removeExtra(DlConst.REAL_COMPONENT);
    }
    Log.d(TAG, "startService() proxy = ["
      + proxy
      + "], method = ["
      + method
      + "], args = ["
      + Arrays.toString(args)
      + "]");
    return method.invoke(mDelegate, args);
  }
}
