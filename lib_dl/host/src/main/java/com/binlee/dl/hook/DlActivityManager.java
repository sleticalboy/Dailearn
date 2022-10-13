package com.binlee.dl.hook;

import android.util.Log;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Create on 2022/10/12
 *
 * @author binlee
 */
public final class DlActivityManager implements InvocationHandler {

  private static final String TAG = "DlActivityManager";

  private Object mDelegate;

  public void setDelegate(Object delegate) {
    mDelegate = delegate;
  }

  @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    intercept(method, args);
    return method.invoke(mDelegate, args);
  }

  private void intercept(Method method, Object[] args) {
    // startActivity
    // startService/stopService
    // public android.content.ComponentName startService(android.app.IApplicationThread caller, android.content.Intent service, java.lang.String resolvedType, java.lang.String callingPackage, int userId) throws android.os.RemoteException
    // public int stopService(android.app.IApplicationThread caller, android.content.Intent service, java.lang.String resolvedType, int userId) throws android.os.RemoteException
    // bindService/bindIsolatedService/unbindService
    // public int bindService(android.app.IApplicationThread caller, android.os.IBinder token, android.content.Intent service, java.lang.String resolvedType, android.app.IServiceConnection connection, int flags, java.lang.String callingPackage, int userId) throws android.os.RemoteException
    // public boolean unbindService(android.app.IServiceConnection connection) throws android.os.RemoteException
    Log.d(TAG, "intercept() method: " + method + ", args: " + Arrays.toString(args));
    // 如果是插件，就注入占坑的类
  }
}
