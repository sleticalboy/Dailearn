package com.binlee.dl.test;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.binlee.dl.puppet.IService;

/**
 * Created on 2022/9/6
 *
 * @author binlee
 */
public class SubService extends Service implements IService {

  @Override public void onCreate(Context context) {
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    return super.onStartCommand(intent, flags, startId);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }
}
