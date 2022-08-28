package com.binlee.dl.host;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.binlee.dl.puppet.IActivity;

/**
 * Created on 2022-08-27.
 *
 * @author binlee
 */
public final class ProxyActivity extends AppCompatActivity {

  // 通过 intent 拿到 target component，之后通过反射初始化并注入参数
  // 然后通过此类代理插件类的行为

  public static final String TARGET_COMPONENT = "com.binlee.dl.TARGET_COMPONENT";

  private IActivity mTarget;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final String targetComponent = getIntent().getStringExtra(TARGET_COMPONENT);
    try {
      mTarget = (IActivity) getClassLoader().loadClass(targetComponent).newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
      finish();
    }
    if (mTarget != null) mTarget.onCreate(savedInstanceState);
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    if (mTarget != null) mTarget.onNewIntent(intent);
  }

  @Override protected void onStart() {
    super.onStart();
    if (mTarget != null) mTarget.onStart();
  }

  @Override protected void onResume() {
    super.onResume();
    if (mTarget != null) mTarget.onResume();
  }

  @Override protected void onStop() {
    super.onStop();
    if (mTarget != null) mTarget.onStop();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (mTarget != null) mTarget.onDestroy();
  }
}
