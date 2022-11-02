package com.example.dyvd.service;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.dyvd.DownloadObserver;
import com.example.dyvd.DyUtil;
import com.example.dyvd.VideoItem;
import com.example.dyvd.db.VideosDb;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
public class DyService extends Service implements ClipboardManager.OnPrimaryClipChangedListener,
  Handler.Callback {

  private static final String TAG = "DyService";

  private ClipboardManager mClipboard;
  private VideosDb mDb;

  // 与 activity 通信，主动 push 事件到 activity
  private Messenger mServer;
  private Messenger mClient;

  public DyService() {
    // app 启动时 startService()
    // 进入 activity 页面时 bindService()
  }

  @Override public void onCreate() {
    startForegroundIfNeeded();

    mClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    mClipboard.addPrimaryClipChangedListener(this);
    DownloadObserver.get().start(this);

    mDb = new VideosDb(getApplicationContext());

    // 启动一个线程
    HandlerThread worker = new HandlerThread("DyWorker");
    worker.start();
    mServer = new Messenger(new Handler(worker.getLooper(), this));
  }

  private void startForegroundIfNeeded() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
    Notifications.createAllChannels(this);
    startForeground(Notifications.notifyId, Notifications.build(this, null, null));
  }


  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    return super.onStartCommand(intent, flags, startId);
  }

  @Override public void onDestroy() {
    mClipboard.removePrimaryClipChangedListener(this);
    DownloadObserver.get().stop(this);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    mClient = intent.getParcelableExtra("client_messenger");
    return mServer.getBinder();
  }

  @Override public void onPrimaryClipChanged() {
    spyClipData();
  }

  @Override public boolean handleMessage(@NonNull Message msg) {
    switch (msg.what) {
      case 1:
        // load all videos
        final List<VideoItem> list = mDb.getAll();
        // 如果数据量过大，要分批次投递，防止 binder 挂掉
        final int size = list.size();
        if (size > 10) {
          final int steps = size / 10;
          final int reminder = size % 10;
          int end;
          for (int i = 0; i < steps; i++) {
            end = i * 10 + 10;
            postBatchResult(list.subList(i * 10, end > size ? i * 10 + reminder : end));
          }
        } else {
          postBatchResult(list);
        }
        return true;
      case 2:
        // resolve url
        resolveVideo(((String) msg.obj));
        return true;
      case 3:
        // 下载请求
        requestDownload((VideoItem) msg.obj);
        return true;
      case 4:
        // 删除
        mDb.remove((VideoItem) msg.obj);
        return true;
    }
    return false;
  }

  private void requestDownload(VideoItem item) {
    DyUtil.download(this, item, new DyUtil.DownloadCallback() {

      @Override public void onStart(VideoItem item) {
        postDownloadState(item, 4);
      }

      @Override public void onError(VideoItem item) {
        postDownloadState(item, 5);
      }

      @Override public void onComplete(VideoItem item) {
        postDownloadState(item, 6);
      }
    });
  }

  private void spyClipData() {
    final ClipData data = mClipboard.getPrimaryClip();
    if (data != null && data.getItemCount() > 0) {
      try {
        mServer.send(Message.obtain(null, 2, data.getItemAt(0).getText().toString()));
      } catch (RemoteException e) {
        e.printStackTrace();
      }
      return;
    }
    Log.d(TAG, "fetchClipData() no data");
  }

  private void resolveVideo(String text) {
    final String shareUrl = DyUtil.getOriginalShareUrl(text);
    if (TextUtils.isEmpty(shareUrl)) {
      Toast.makeText(this, "invalid: " + text, Toast.LENGTH_SHORT).show();
      return;
    }

    if (mDb.hasVideo(shareUrl)) {
      Log.d(TAG, "resolveVideo() aborted as existed");
      return;
    }

    final DyUtil.Result result = DyUtil.parseItem(shareUrl);
    if (result.success()) {
      postResult(result.result);
    }
  }

  private void postBatchResult(List<VideoItem> items) {
    if (items == null) return;

    Log.d(TAG, "postBatchResult() size: " + items.size() + ", " + items);

    // 通知 UI 刷新
    final Message msg = Message.obtain();
    msg.what = 3;
    msg.getData().putParcelableArrayList("video_items", (ArrayList<? extends Parcelable>) items);
    try {
      mClient.send(msg);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  private void postResult(VideoItem item) {
    Log.d(TAG, "postResult() " + item);
    if (item == null) return;
    // 通知 UI 刷新
    try {
      mClient.send(Message.obtain(null, 2, item));
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    // 更新数据库
    mDb.insert(item);
  }

  private void postDownloadState(VideoItem item, int what) {
    try {
      mClient.send(Message.obtain(null, what, item));
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    // 更新数据库
    mDb.insert(item);
  }
}
