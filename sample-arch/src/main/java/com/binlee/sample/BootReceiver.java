package com.binlee.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created on 21-2-27.
 *
 * @author binlee sleticalboy@gmail.com
 */
public class BootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
      ArchService.onBootCompleted(context);
    }
  }
}