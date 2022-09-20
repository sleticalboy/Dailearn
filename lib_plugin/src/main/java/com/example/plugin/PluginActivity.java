package com.example.plugin;

import android.content.Intent;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.binlee.dl.puppet.IPuppetActivity;

public class PluginActivity extends AppCompatActivity implements IPuppetActivity {

  private static final String TAG = "PluginActivity";

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Log.d(TAG, "onCreate() savedInstanceState: " + savedInstanceState);
  }

  @Override public void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
  }

  @Override public void onStart() {
    super.onStart();
  }

  @Override public void onResume() {
    super.onResume();
  }

  @Override public void onStop() {
    super.onStop();
  }

  @Override public void onDestroy() {
    super.onDestroy();
  }
}