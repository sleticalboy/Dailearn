package com.example.plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author binlee sleticalboy@gmail.com
 * created by IDEA on 2022/9/28
 */
public final class PluginReceiver extends BroadcastReceiver {

  private static final String TAG = "PluginReceiver";

  public PluginReceiver() {
  }

  @Override public void onReceive(Context context, Intent intent) {
    Log.d(TAG, "onReceive() context = [" + context + "], intent = [" + intent + "]");
    if ("com.example.plugin.action.SAMPLE_ACTION_STATIC".equals(intent.getAction())) {
      final String value = intent.getStringExtra("key_1");
      Log.i(TAG, "onReceive() key_1: " + value);
    }
  }
}
