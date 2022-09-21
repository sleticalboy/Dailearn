package com.example.plugin;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PluginActivity extends AppCompatActivity {

  private static final String TAG = "PluginActivity";

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Log.d(TAG, "onCreate() " + getApplication() + ", res: " + getApplication().getResources());
    new Handler().postDelayed(() -> {
      Toast.makeText(this, "Hello Plugin", Toast.LENGTH_SHORT).show();
    }, 500L);
  }
}