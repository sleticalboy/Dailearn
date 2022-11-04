package com.example.dyvd.service;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.example.dyvd.DownloadObserver;
import com.example.dyvd.DyUtil;
import com.example.dyvd.VideoItem;
import com.example.dyvd.db.VideosDb;
import com.example.dyvd.engine.Engine;
import com.example.dyvd.engine.EngineFactory;
import java.util.List;

/**
 * Created on 2022/11/2
 *
 * @author binlee
 */
public class DyBinder extends Binder implements ClipboardManager.OnPrimaryClipChangedListener, Handler.Callback  {

  private static final String TAG = "DyWorker";

  private Context mContext;

  private ClipboardManager mClipboard;
  private VideosDb mDb;

  private Handler mWorker;
  private Handler mClient;

  public DyBinder() {
  }

  public void start(Context context) {

    mContext = context.getApplicationContext();

    mClipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
    mClipboard.addPrimaryClipChangedListener(this);
    DownloadObserver.get().start(mContext);

    mDb = new VideosDb(mContext);

    // 启动一个线程
    HandlerThread thread = new HandlerThread("DyWorker");
    thread.start();
    mWorker = new Handler(thread.getLooper(), this);
  }

  public void stop() {
    mClipboard.removePrimaryClipChangedListener(this);
    mWorker.removeCallbacksAndMessages(null);
    mDb.close();
    DownloadObserver.get().stop(mContext);
  }

  public void attach(Handler client) {
    mClient = client;
  }

  public void requestResolveVideo(String text) {
    Message.obtain(mWorker, 2, text).sendToTarget();
  }

  public void requestLoadAll() {
    // 请求加载数据
    Message.obtain(mWorker, 1).sendToTarget();
  }

  public void requestRemoveVideo(VideoItem item) {
    Message.obtain(mWorker, 4, item).sendToTarget();
  }

  public void requestDownloadVideo(VideoItem item) {
    Message.obtain(mWorker, 3, item).sendToTarget();
  }

  @Override public void onPrimaryClipChanged() {
    spyClipData();
  }

  @Override public boolean handleMessage(@NonNull Message msg) {
    switch (msg.what) {
      case 1:
        // load all videos
        postBatchResult(mDb.getAll());
        return true;
      case 2:
        // resolve url
        resolveText(((String) msg.obj));
        return true;
      case 3:
        // 下载请求
        downloadVideo((VideoItem) msg.obj);
        return true;
      case 4:
        // 删除
        mDb.remove((VideoItem) msg.obj);
        return true;
    }
    return false;
  }

  private void spyClipData() {
    final ClipData data = mClipboard.getPrimaryClip();
    if (data != null && data.getItemCount() > 0) {
      requestResolveVideo(data.getItemAt(0).getText().toString());
      return;
    }
    Log.d(TAG, "fetchClipData() no data");
  }

  private void downloadVideo(VideoItem item) {
    DyUtil.download(mContext, item, new DyUtil.DownloadCallback() {

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

  private void resolveText(String text) {

    final Engine engine = EngineFactory.create(text);
    if (engine == null || engine.shortUrl == null) {
      // Log.w(TAG, "resolveText() unsupported: " + text);
      Toast.makeText(mContext, "invalid: " + text, Toast.LENGTH_SHORT).show();
      return;
    }

    if (mDb.hasVideo(engine.shortUrl)) {
      Log.d(TAG, "resolveVideo() aborted as existed");
      return;
    }

    final Engine.Result result = engine.parseItem();
    if (result.success()) {
      postResult(result.result);
    } else {
      Log.d(TAG, "resolveText() " + result);
    }
  }

  private void postBatchResult(List<VideoItem> items) {
    if (items == null) return;

    Log.d(TAG, "postBatchResult() size: " + items.size());

    // 通知 UI 刷新
    Message.obtain(mClient, 1, items).sendToTarget();
  }

  private void postResult(VideoItem item) {
    if (item == null) return;

    if (mClient != null) {
      // 通知 UI 刷新
      Message.obtain(mClient, 2, item).sendToTarget();
    }
    // 更新数据库
    mDb.insert(item);
  }

  private void postDownloadState(VideoItem item, int what) {
    Message.obtain(mClient, what, item).sendToTarget();
    // 更新数据库
    mDb.insert(item);
  }
}
