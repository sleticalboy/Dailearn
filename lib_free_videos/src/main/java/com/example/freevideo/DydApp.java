package com.example.freevideo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import com.binlee.sqlite.orm.OrmConfig;
import com.binlee.sqlite.orm.OrmCore;
import com.binlee.sqlite.orm.core.OrmDb;
import com.example.freevideo.service.DyService;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
public class DydApp extends Application {

  @Override protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);

    final OrmConfig config = new OrmConfig();
    config.tables(VideoItem.class);
    OrmCore.init(config);
  }

  @Override public void onCreate() {
    super.onCreate();
    startService(new Intent(this, DyService.class));
  }
}
