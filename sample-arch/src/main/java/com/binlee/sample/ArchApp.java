package com.binlee.sample;

import android.app.Application;
import android.content.Context;
import com.binlee.sample.util.Glog;

/**
 * Created on 21-2-27.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class ArchApp extends Application {

  static {
    Glog.setConfig(new Glog.Config(true, true, true));
  }

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
  }

  @Override
  public void onCreate() {
    super.onCreate();
  }
}
