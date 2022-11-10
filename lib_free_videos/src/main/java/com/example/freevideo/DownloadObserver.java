package com.example.freevideo;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Pair;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
public class DownloadObserver extends BroadcastReceiver {

  private static final DownloadObserver sObserver = new DownloadObserver();
  private final Map<Long, Pair<VideoItem, DyUtil.DownloadCallback>> mCallbacks;

  private boolean mRegistered = false;

  private DownloadObserver() {
    mCallbacks = new HashMap<>();
  }

  public static DownloadObserver get() {
    return sObserver;
  }

  @Override public void onReceive(Context context, Intent intent) {
    if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
      final long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L);
      final Pair<VideoItem, DyUtil.DownloadCallback> pair = mCallbacks.remove(id);
      if (pair != null) {
        pair.first.state = DyState.DOWNLOADED;
        pair.second.onComplete(pair.first);
      }
    }
  }

  public void addCallback(VideoItem item, DyUtil.DownloadCallback callback) {
    mCallbacks.put(item.id, Pair.create(item, callback));
  }

  public void start(Context context) {
    if (mRegistered) return;
    context.registerReceiver(this, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    mRegistered = true;
  }

  public void stop(Context context) {
    context.unregisterReceiver(this);
  }
}
