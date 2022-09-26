package com.binlee.dl.host.proxy;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.binlee.dl.DlConst;

/**
 * Created on 2022-08-28.
 *
 * @author binlee
 */
public final class ProxyService extends Service {

  public static void start(Context context, ComponentName target) {
    context.startService(new Intent(context, ProxyService.class)
      .putExtra(DlConst.REAL_COMPONENT, target)
    );
  }

  public static void stop(Context context, ComponentName target) {
    context.stopService(new Intent(context, ProxyService.class)
      .putExtra(DlConst.REAL_COMPONENT, target)
    );
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    return super.onStartCommand(intent, flags, startId);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }
}
