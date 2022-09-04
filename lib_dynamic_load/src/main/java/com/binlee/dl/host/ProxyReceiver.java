package com.binlee.dl.host;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.binlee.dl.puppet.IBroadcastReceiver;

/**
 * Created on 2022-08-28.
 *
 * @author binlee
 */
public class ProxyReceiver extends BroadcastReceiver implements IMaster {

  @Override public void onReceive(Context context, Intent intent) {
    final String className = intent.getStringExtra(TARGET_COMPONENT);
    IBroadcastReceiver target = ComponentInitializer.initialize(context.getClassLoader(), className);
    if (target != null) {
      target.onReceive(context, intent);
    }
  }
}
