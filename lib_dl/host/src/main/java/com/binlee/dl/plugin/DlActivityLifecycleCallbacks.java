package com.binlee.dl.plugin;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created on 2022/9/23
 *
 * @author binlee
 */
final class DlActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

  private final List<Application.ActivityLifecycleCallbacks> mDelegate;

  @SuppressWarnings("unchecked")
  DlActivityLifecycleCallbacks(Application hostApp) {
    List<Application.ActivityLifecycleCallbacks> delegate;
    try {
      // android.app.Application#mActivityLifecycleCallbacks
      final Field field = Application.class.getDeclaredField("mActivityLifecycleCallbacks");
      field.setAccessible(true);
      delegate = (ArrayList<Application.ActivityLifecycleCallbacks>) field.get(hostApp);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
      delegate = null;
    }
    mDelegate = delegate;
  }

  private List<Application.ActivityLifecycleCallbacks> collectCallbacks() {
    if (mDelegate == null) return Collections.emptyList();

    synchronized (mDelegate) {
      return new ArrayList<>(mDelegate);
    }
  }

  @Override public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    for (Application.ActivityLifecycleCallbacks callbacks : collectCallbacks()) {
      callbacks.onActivityCreated(activity, savedInstanceState);
    }
  }

  @Override public void onActivityStarted(@NonNull Activity activity) {
    for (Application.ActivityLifecycleCallbacks callbacks : collectCallbacks()) {
      callbacks.onActivityStarted(activity);
    }
  }

  @Override public void onActivityResumed(@NonNull Activity activity) {
    for (Application.ActivityLifecycleCallbacks callbacks : collectCallbacks()) {
      callbacks.onActivityResumed(activity);
    }
  }

  @Override public void onActivityPaused(@NonNull Activity activity) {
    for (Application.ActivityLifecycleCallbacks callbacks : collectCallbacks()) {
      callbacks.onActivityPaused(activity);
    }
  }

  @Override public void onActivityStopped(@NonNull Activity activity) {
    for (Application.ActivityLifecycleCallbacks callbacks : collectCallbacks()) {
      callbacks.onActivityStopped(activity);
    }
  }

  @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    for (Application.ActivityLifecycleCallbacks callbacks : collectCallbacks()) {
      callbacks.onActivitySaveInstanceState(activity, outState);
    }
  }

  @Override public void onActivityDestroyed(@NonNull Activity activity) {
    for (Application.ActivityLifecycleCallbacks callbacks : collectCallbacks()) {
      callbacks.onActivityDestroyed(activity);
    }
  }
}
