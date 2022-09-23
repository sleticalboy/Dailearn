package com.example.plugin;

import android.app.Application;
import android.content.Context;
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
  }
}
