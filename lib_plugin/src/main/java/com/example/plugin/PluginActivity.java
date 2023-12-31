package com.example.plugin;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PluginActivity extends AppCompatActivity {

  private static final String TAG = "PluginActivity";

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate() app: " + getApplication());
    Log.d(TAG, "onCreate() resource: " + getResources());
    Log.d(TAG, "onCreate() class loader: " + getClassLoader());
    Log.d(TAG, "onCreate() base context: " + getBaseContext());
    Log.d(TAG, "onCreate() package name: " + getPackageName());
    // TODO: 2022/9/21 这里实际显示的是 alarm_activity，资源冲突了，要解决
    // int layout activity_main 0x7f0b001c
    // int layout activity_alarm 0x7f0b001c
    // 1、修改 aapt 工具达到 id 可控
    // 2、修改 resources.asrc 文件，改变 id
    setContentView(R.layout.activity_plugin);
    getWindow().getDecorView().postDelayed(() -> {
      try {
        Toast.makeText(this, R.string.app_name, Toast.LENGTH_SHORT).show();
      } catch (Resources.NotFoundException e) {
        e.printStackTrace();
      }
    }, 500L);
    final TextView textView = findViewById(R.id.tv);
    // onCreate() text: @2131623964, app_name: lib_plugin, 2131623964
    if (textView != null) {
      final String s = textView.getText().toString();
      textView.setText(Integer.parseInt(s.substring(1)));
      Log.d(TAG, "onCreate() text: " + textView.getText());
      Log.d(TAG, "onCreate() text color: " + textView.getTextColors() + ", " + R.color.black);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    Log.d(TAG, "onStart() called");
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.d(TAG, "onResume() called");
  }

  @Override
  protected void onPause() {
    super.onPause();
    Log.d(TAG, "onPause() called");
  }

  @Override
  protected void onStop() {
    super.onStop();
    Log.d(TAG, "onStop() called");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "onDestroy() called");
  }
}