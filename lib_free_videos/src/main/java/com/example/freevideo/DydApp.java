package com.example.freevideo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import com.binlee.sqlite.orm.VideosDb;
import com.example.freevideo.service.DyService;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
public class DydApp extends Application {

  @Override protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    VideosDb.registerTables(VideoItem.class);
  }

  @Override public void onCreate() {
    super.onCreate();
    startService(new Intent(this, DyService.class));
  }
}
