package com.binlee.dl.host;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.binlee.dl.puppet.IService;

/**
 * Created on 2022-08-28.
 *
 * @author binlee
 */
public class ProxyService extends Service implements IMaster {

  private IService mTarget;

  @Override public void onCreate() {
    super.onCreate();
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    final String target = intent.getStringExtra(TARGET_COMPONENT);
    mTarget = ComponentInitializer.initialize(getClassLoader(), target);
    if (mTarget != null) {
      mTarget.onCreate();
      return mTarget.onStartCommand(intent, flags, startId);
    }
    return super.onStartCommand(intent, flags, startId);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return mTarget != null ? mTarget.onBind(intent) : null;
  }

  @Override public boolean onUnbind(Intent intent) {
    return mTarget != null ? mTarget.onUnbind(intent) : super.onUnbind(intent);
  }

  @Override public void onRebind(Intent intent) {
    if (mTarget != null) mTarget.onRebind(intent);
  }

  @Override public void onDestroy() {
    if (mTarget != null) mTarget.onDestroy();
  }
}
