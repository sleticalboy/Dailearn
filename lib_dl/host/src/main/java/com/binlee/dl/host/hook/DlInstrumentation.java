package com.binlee.dl.host.hook;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2022/9/21
 *
 * @author binlee
 */
public final class DlInstrumentation extends Instrumentation {

  private Instrumentation mDelegate;
  
  private final List<Callbacks> mCallbacks = new ArrayList<Callbacks>() {
    @Override public boolean add(Callbacks callbacks) {
      return !contains(callbacks) && super.add(callbacks);
    }
  };

  public DlInstrumentation() {
  }

  public void setDelegate(Object delegate) {
    mDelegate = (Instrumentation) delegate;
  }
  
  public void addCallbacks(Callbacks callbacks) {
    mCallbacks.add(callbacks);
  }
  
  public void removeCallbacks(Callbacks callbacks) {
    mCallbacks.remove(callbacks);
  }

  @Override public Application newApplication(ClassLoader cl, String className, Context context)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    return mDelegate.newApplication(cl, className, context);
  }

  @Override public Activity newActivity(ClassLoader cl, String className, Intent intent)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    return mDelegate.newActivity(cl, className, intent);
  }

  @Override public void callActivityOnCreate(Activity activity, Bundle icicle) {
    dispatchOnCallActivityOnCreate(activity);
    mDelegate.callActivityOnCreate(activity, icicle);
  }

  @Override public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
    mDelegate.callActivityOnCreate(activity, icicle, persistentState);
  }

  public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
    Intent intent, int requestCode) {
    return mDelegate.execStartActivity(who, contextThread, token, target, intent, requestCode);
  }

  public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
    Intent intent, int requestCode, Bundle options) {
    return mDelegate.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
  }

  public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Fragment target,
    Intent intent, int requestCode, Bundle options) {
    return mDelegate.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
  }

  public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, String target,
    Intent intent, int requestCode, Bundle options) {
    return mDelegate.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
  }

  private void dispatchOnCallActivityOnCreate(Activity activity) {
    for (Callbacks callback : mCallbacks) {
      callback.onCallActivityOnCreate(activity);
    }
  }
  
  public interface Callbacks {

    void onCallActivityOnCreate(Activity activity);
  }
}
