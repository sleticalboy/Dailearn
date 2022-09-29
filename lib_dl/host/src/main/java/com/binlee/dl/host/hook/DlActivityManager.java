package com.binlee.dl.host.hook;

import android.app.IActivityManager;
import android.content.Context;
import android.util.Log;
import com.binlee.dl.plugin.DlPackageManager;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created on 2022/9/27
 *
 * @author binlee
 */
public final class DlActivityManager {

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

  public void beforeMethod(Method method, Object[] args) {
  }

  public void afterMethod(Method method, Object[] args, Object result) {
    switch (method.getName()) {
      case "startService":
        Log.d(TAG, "startService() method = [" + method + "], args = [" + Arrays.toString(args) + "], res: " + result);
        break;
      case "stopService":
        Log.d(TAG, "stopService() method = [" + method + "], args = [" + Arrays.toString(args) + "], res: " + result);
        break;
      case "bindService":
      case "bindIsolatedService":
        Log.d(TAG, "bindService() method = [" + method + "], args = [" + Arrays.toString(args) + "], res: " + result);
        break;
      case "unbindService":
        Log.d(TAG, "unbindService() method = [" + method + "], args = [" + Arrays.toString(args) + "], res: " + result);
        break;
      case "registerReceiverWithFeature":
        Log.d(TAG, "registerReceiverWithFeature() method = [" + method + "], args = [" + Arrays.toString(args) + "], res: " + result);
        break;
      case "broadcastIntentWithFeature":
        Log.d(TAG, "broadcastIntentWithFeature() method = [" + method + "], args = [" + Arrays.toString(args) + "], res: " + result);
        break;
      case "unregisterReceiver":
        Log.d(TAG, "unregisterReceiver() method = [" + method + "], args = [" + Arrays.toString(args) + "], res: " + result);
        break;
    }
  }
}
