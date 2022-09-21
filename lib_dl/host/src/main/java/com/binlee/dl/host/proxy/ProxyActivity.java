package com.binlee.dl.host.proxy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.binlee.dl.host.DlConst;

/**
 * Created on 2022-08-27.
 *
 * @author binlee
 */
public final class ProxyActivity extends AppCompatActivity {

  public static void start(@NonNull Context context, @NonNull ComponentName target) {
    start(context, new Intent(context, ProxyActivity.class)
      .putExtra(DlConst.REAL_COMPONENT, target)
    );
  }

  public static void start(@NonNull Context context, @NonNull Intent target) {
    context.startActivity(target);
  }
}
