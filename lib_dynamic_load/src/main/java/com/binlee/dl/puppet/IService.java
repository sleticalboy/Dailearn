package com.binlee.dl.puppet;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created on 2022-09-04.
 *
 * @author binlee
 */
public interface IService {

  void onCreate(Context context);

  default int onStartCommand(Intent intent, int flags, int startId) {
    // mStartCompatibility = getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.ECLAIR;
    // return mStartCompatibility ? START_STICKY_COMPATIBILITY : START_STICKY;
    return Service.START_STICKY;
  }

  IBinder onBind(Intent intent);

  default boolean onUnbind(Intent intent) {
    return false;
  }

  default void onRebind(Intent intent) {
  }

  default void onDestroy() {
  }
}
