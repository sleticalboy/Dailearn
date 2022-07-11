package com.binlee.sample.core;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import com.binlee.sample.util.Glog;

/**
 * Created on 21-2-24.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class LifecycleDetector implements IComponent, Application.ActivityLifecycleCallbacks {

  private static final String TAG = Glog.wrapTag("LifecycleDetector");

  private final Application mApp;
  private final Handler mCallback;
  private FragmentManager.FragmentLifecycleCallbacks mCb;

  public LifecycleDetector(Context context, Handler callback) {
    context = context.getApplicationContext();
    if (!(context instanceof Application)) {
      throw new RuntimeException("Application is not initialized.");
    }
    mApp = (Application) context;
    mCallback = callback;
  }

  @Override
  public void onStart() {
    mApp.registerActivityLifecycleCallbacks(this);
  }

  @Override
  public void onDestroy() {
    if (mApp != null) mApp.unregisterActivityLifecycleCallbacks(this);
  }

  @Override
  public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    Glog.v(TAG, "onActivityCreated() " + activity);
    if (!(activity instanceof FragmentActivity)) return;
    mCb = new FragmentManager.FragmentLifecycleCallbacks() {
      @Override
      public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
        Glog.v(TAG, "onFragmentResumed() " + f);
        mCallback.obtainMessage(IWhat.LIFECYCLE_CHANGE, 1, 0).sendToTarget();
      }

      @Override
      public void onFragmentPaused(@NonNull FragmentManager fm, @NonNull Fragment f) {
        Glog.v(TAG, "onFragmentPaused() " + f);
        mCallback.obtainMessage(IWhat.LIFECYCLE_CHANGE, 0, 0).sendToTarget();
      }
    };
    Glog.v(TAG, "onActivityCreated() " + activity + ", registerFragmentLifecycleCallbacks");
    ((FragmentActivity) activity).getSupportFragmentManager()
      .registerFragmentLifecycleCallbacks(mCb, false);
  }

  @Override
  public void onActivityDestroyed(@NonNull Activity activity) {
    Glog.v(TAG, "onActivityDestroyed() " + activity);
    if (mCb == null || !(activity instanceof FragmentActivity)) return;
    Glog.v(TAG, "onActivityDestroyed() " + activity + ", unregisterFragmentLifecycleCallbacks");
    ((FragmentActivity) activity).getSupportFragmentManager()
      .unregisterFragmentLifecycleCallbacks(mCb);
  }

  @Override
  public void onActivityStarted(@NonNull Activity activity) {
    // empty implementation
  }

  @Override
  public void onActivityResumed(@NonNull Activity activity) {
    // empty implementation
  }

  @Override
  public void onActivityPaused(@NonNull Activity activity) {
    // empty implementation
  }

  @Override
  public void onActivityStopped(@NonNull Activity activity) {
    // empty implementation
  }

  @Override
  public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    // empty implementation
  }
}
