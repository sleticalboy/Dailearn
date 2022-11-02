package com.example.dyvd.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
public class DyService extends Service {

  private static final String TAG = "DyService";

  private final DyBinder mWorker = new DyBinder();

  public DyService() {
  }

  @Override public void onCreate() {
    startForegroundIfNeeded();
    mWorker.start(this);
  }

  private void startForegroundIfNeeded() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
    Notifications.createAllChannels(this);
    startForeground(Notifications.notifyId, Notifications.build(this, null, null));
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    return super.onStartCommand(intent, flags, startId);
  }

  @Override public void onDestroy() {
    mWorker.stop();
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    Log.d(TAG, "onBind() " + intent);
    return mWorker;
  }

  @Override public boolean onUnbind(Intent intent) {
    Log.d(TAG, "onUnbind() " + intent);
    mWorker.attach(null);
    return true;
  }

  @Override public void onRebind(Intent intent) {
    Log.d(TAG, "onRebind() " + intent);
  }
}
