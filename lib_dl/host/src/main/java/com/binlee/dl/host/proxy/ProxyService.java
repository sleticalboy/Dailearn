package com.binlee.dl.host.proxy;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.binlee.dl.host.IMaster;

/**
 * Created on 2022-08-28.
 *
 * @author binlee
 */
public final class ProxyService extends Service implements IMaster {

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    return super.onStartCommand(intent, flags, startId);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }
}
