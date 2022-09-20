package com.binlee.dl.puppet;

import android.content.Context;
import android.content.Intent;

/**
 * Created on 2022-09-04.
 *
 * @author binlee
 */
public interface IPuppetReceiver {
  void onReceive(Context context, Intent intent);
}
