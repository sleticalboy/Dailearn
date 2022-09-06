package com.binlee.dl.test;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.binlee.dl.puppet.IActivity;

/**
 * Created on 2022/9/6
 *
 * @author binlee
 */
public class SubActivity extends AppCompatActivity implements IActivity {

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
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
