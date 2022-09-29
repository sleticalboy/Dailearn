package com.example.plugin;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Created on 2022/9/23
 *
 * @author binlee
 */
public class PluginApplication extends Application {

  private static final String TAG = "PluginApplication";

  @Override protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    Log.d(TAG, "attachBaseContext() " + base + "");
  }

  @Override public void onCreate() {
    super.onCreate();
    Log.d(TAG, "onCreate() " + getPackageManager());
    // staticReceiver();
    dynamicReceiver();
  }

  private void staticReceiver() {
    final IntentFilter filter = new IntentFilter();
    filter.addAction("com.example.plugin.action.SAMPLE_ACTION_STATIC");
    final Intent intent = registerReceiver(new PluginReceiver(), filter);
    Log.d(TAG, "staticReceiver() register com.example.plugin.PluginReceiver: " + intent);
  }

  private void dynamicReceiver() {
    final IntentFilter filter = new IntentFilter();
    filter.addAction("com.example.plugin.action.SAMPLE_ACTION_DYNAMIC");
    final Intent intent = registerReceiver(new DynamicReceiver(), filter);
    Log.d(TAG, "dynamicReceiver() register com.example.plugin.DynamicReceiver: " + intent);
  }
}
