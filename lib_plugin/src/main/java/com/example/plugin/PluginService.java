package com.example.plugin;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;

/**
 * Created on 2022/9/26
 *
 * @author binlee
 */
public final class PluginService extends Service {

  private static final String TAG = "PluginService";

  private PluginBinder mBinder;

  @Override public void onCreate() {
    Log.d(TAG, "onCreate() called");
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG, "onStartCommand() intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
    return super.onStartCommand(intent, flags, startId);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    if (mBinder == null) {
      mBinder = new PluginBinder(this);
    }
    Log.d(TAG, "onBind() intent: " + intent + ", binder: " + mBinder);
    return mBinder;
  }

  @Override public boolean onUnbind(Intent intent) {
    Log.d(TAG, "onUnbind() called with: intent = [" + intent + "]");
    return false;
  }

  @Override public void onDestroy() {
    Log.d(TAG, "onDestroy() called");
  }

  private static class PluginBinder extends Binder {

    private final PluginService mService;

    public PluginBinder(PluginService service) {
      mService = service;
    }
  }
}
