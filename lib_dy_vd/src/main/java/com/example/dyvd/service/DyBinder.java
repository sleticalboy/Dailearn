package com.example.dyvd.service;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.dyvd.DownloadObserver;
import com.example.dyvd.DyUtil;
import com.example.dyvd.VideoItem;
import com.example.dyvd.db.VideosDb;
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

  public Handler getWorker() {
    return mWorker;
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
            postBatchResult(list.subList(i * 10, end > size ? i * 10 + reminder : end), false);
          }
        } else {
          postBatchResult(list, true);
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

  private void spyClipData() {
    final ClipData data = mClipboard.getPrimaryClip();
    if (data != null && data.getItemCount() > 0) {
      Message.obtain(mWorker, 2, data.getItemAt(0).getText().toString()).sendToTarget();
      return;
    }
    Log.d(TAG, "fetchClipData() no data");
  }

  private void requestDownload(VideoItem item) {
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

  private void resolveVideo(String text) {
    final String shareUrl = DyUtil.getOriginalShareUrl(text);
    if (TextUtils.isEmpty(shareUrl)) {
      // Toast.makeText(this, "invalid: " + text, Toast.LENGTH_SHORT).show();
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

  private void postBatchResult(List<VideoItem> items, boolean full) {
    if (items == null) return;

    Log.d(TAG, "postBatchResult() size: " + items.size());

    // 通知 UI 刷新
    Message.obtain(mClient, full ? 1 : 3, items).sendToTarget();
  }

  private void postResult(VideoItem item) {
    if (item == null) return;
    // client 为 null 时，缓存数据，等 client 绑定时，重新发布出去
    if (mClient == null) {
      Log.d(TAG, "postResult() pending " + item);
      return;
    }
    // 通知 UI 刷新
    Message.obtain(mClient, 2, item).sendToTarget();
    // 更新数据库
    mDb.insert(item);
  }

  private void postDownloadState(VideoItem item, int what) {
    Message.obtain(mClient, what, item).sendToTarget();
    // 更新数据库
    mDb.insert(item);
  }
}
