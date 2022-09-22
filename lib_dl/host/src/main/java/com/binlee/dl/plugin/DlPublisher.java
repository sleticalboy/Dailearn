package com.binlee.dl.plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import com.binlee.dl.DlManager;

/**
 * Created on 2022/9/22
 *
 * @author binlee
 */
public final class DlPublisher {

  private DlPublisher() {
    //no instance
  }

  // 注册 receiver
  public static BroadcastReceiver registerReceiver(Context context, ActivityInfo info) {
    try {
      final Class<?> clazz = DlManager.loadClass(info.name);
      if (clazz == null) return null;
      final BroadcastReceiver receiver = (BroadcastReceiver) clazz.newInstance();
      IntentFilter filter = new IntentFilter();
      context.registerReceiver(receiver, filter);
      return receiver;
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
    }
    return null;
  }
}
