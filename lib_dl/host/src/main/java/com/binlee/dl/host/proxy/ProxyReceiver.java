package com.binlee.dl.host.proxy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.binlee.dl.puppet.IPuppetReceiver;

/**
 * Created on 2022-08-28.
 *
 * @author binlee
 */
public final class ProxyReceiver extends BroadcastReceiver implements IMaster {

  @Override public void onReceive(Context context, Intent intent) {
    final String className = intent.getStringExtra(TARGET_COMPONENT);
    IPuppetReceiver puppet = PuppetFactory.load(context.getClassLoader(), className);
    if (puppet != null) {
      puppet.onReceive(context, intent);
    }
  }
}
