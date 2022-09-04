package com.binlee.dl.puppet;

import android.content.Intent;
import android.os.IBinder;

/**
 * Created on 2022-09-04.
 *
 * @author binlee
 */
public interface IService {

  void onCreate();

  int onStartCommand(Intent intent, int flags, int startId);

  IBinder onBind(Intent intent);

  boolean onUnbind(Intent intent);

  void onRebind(Intent intent);

  void onDestroy();
}
