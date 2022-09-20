package com.binlee.dl.puppet;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;

/**
 * Created on 2022-08-27.
 *
 * @author binlee
 */
public interface IPuppetActivity {

  void onCreate(@Nullable Bundle savedInstanceState);

  void onNewIntent(Intent intent);

  void onStart();

  void onResume();

  void onStop();

  void onDestroy();
}
