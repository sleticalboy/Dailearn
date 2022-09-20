package com.binlee.dl.host.proxy;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.binlee.dl.puppet.IPuppetActivity;

/**
 * Created on 2022-08-27.
 *
 * @author binlee
 */
public final class ProxyActivity extends AppCompatActivity implements IMaster {

  private IPuppetActivity mPuppet;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final String target = getIntent().getStringExtra(TARGET_COMPONENT);
    mPuppet = PuppetFactory.load(getClassLoader(), target);
    if (mPuppet != null) {
      mPuppet.onCreate(savedInstanceState);
    } else {
      finish();
    }
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    if (mPuppet != null) mPuppet.onNewIntent(intent);
  }

  @Override protected void onStart() {
    super.onStart();
    if (mPuppet != null) mPuppet.onStart();
  }

  @Override protected void onResume() {
    super.onResume();
    if (mPuppet != null) mPuppet.onResume();
  }

  @Override protected void onStop() {
    super.onStop();
    if (mPuppet != null) mPuppet.onStop();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (mPuppet != null) mPuppet.onDestroy();
  }

  @Override public void startActivity(Intent intent) {
    // 保存目标 activity
    intent.putExtra(TARGET_COMPONENT, intent.getComponent().getClassName());
    // 重定向到代理 activity
    intent.setComponent(new ComponentName(this, getClass()));
    super.startActivity(intent);
  }
}
