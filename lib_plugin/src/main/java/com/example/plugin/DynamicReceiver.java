package com.example.plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author binlee sleticalboy@gmail.com
 * created by IDEA on 2022/9/28
 */
public final class DynamicReceiver extends BroadcastReceiver {

  private static final String TAG = "DynamicReceiver";

  public DynamicReceiver() {
  }

  @Override public void onReceive(Context context, Intent intent) {
    Log.d(TAG, "onReceive() context = [" + context + "], intent = [" + intent + "]");
    if ("com.example.plugin.action.SAMPLE_ACTION_DYNAMIC".equals(intent.getAction())) {
      final String value = intent.getStringExtra("key_2");
      Log.i(TAG, "onReceive() key_2: " + value);
    }
    // 这里收到后直接注销
    context.getApplicationContext().unregisterReceiver(this);
  }
}
