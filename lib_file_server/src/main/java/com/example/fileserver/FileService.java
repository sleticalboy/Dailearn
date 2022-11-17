package com.example.fileserver;

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
public class FileService extends Service {

  private static final String TAG = "FileService";

  private FileServer mServer = new FileServer();

  public FileService() {
  }

  @Override public void onCreate() {
    startForegroundIfNeeded();

    mServer.start(this);
  }

  private void startForegroundIfNeeded() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
    Notifications.createAllChannels(this);
    startForeground(Notifications.notifyId, Notifications.build(this, null, null));
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    return super.onStartCommand(intent, flags, startId);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    Log.d(TAG, "onBind() " + intent);
    return null;
  }

  @Override public void onDestroy() {
    mServer.stop();
    mServer = null;
  }
}
