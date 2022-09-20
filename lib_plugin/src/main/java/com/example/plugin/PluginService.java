package com.example.plugin;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.binlee.dl.puppet.IPuppetService;

public class PluginService extends Service implements IPuppetService {

  @Override public void onCreate(Context context) {
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    return super.onStartCommand(intent, flags, startId);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }
}
