package com.example.dyvd;

import android.app.Application;
import android.content.Intent;
import com.example.dyvd.service.DyService;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
public class DydApp extends Application {

  @Override public void onCreate() {
    super.onCreate();
    startService(new Intent(this, DyService.class));
  }
}
