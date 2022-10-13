package com.binlee.dl.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.binlee.dl.DlConst;
import com.binlee.dl.proxy.ProxyActivity;

/**
 * Created on 2022/10/13
 *
 * @author binlee
 */
public final class DlActivities {

  private DlActivities() {
    //no instance
  }

  public static void start(@NonNull Context context, @NonNull ComponentName target) {
    start(context, new Intent(context, ProxyActivity.class)
      .putExtra(DlConst.REAL_COMPONENT, target)
    );
  }

  public static void start(@NonNull Context context, @NonNull Intent intent) {
    context.startActivity(intent);
  }
}
