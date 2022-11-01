package com.example.dyvd;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.dyvd.databinding.ActivityVideoListBinding;
import com.example.dyvd.db.FakeVideoDb;

public class VideoListActivity extends AppCompatActivity implements VideoAdapter.Callback,
        ClipboardManager.OnPrimaryClipChangedListener {

  private static final String TAG = "MainActivity";

  private ActivityVideoListBinding mBinding;
  private VideoAdapter mAdapter;
  private ClipboardManager mClipboard;

  private FakeVideoDb mDb;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mBinding = ActivityVideoListBinding.inflate(getLayoutInflater());
    setContentView(mBinding.getRoot());

    mClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    mClipboard.addPrimaryClipChangedListener(this);

    // 读取剪切板数据，判断是否是抖音分享链接
    // 如果是则发送网络请求，获取响应数据
    // 通过 jsoup 解析响应数据，获取视频下载链接
    // 下载视频并存入数据库

    mBinding.btnResolveUrl.setOnClickListener(v -> {
      resolveVideo(mBinding.tvShareText.getText().toString());
    });
    mBinding.ivClearUrl.setOnClickListener(v -> {
      mBinding.tvShareText.setText(null);
    });
    mBinding.rvVideos.setLayoutManager(new LinearLayoutManager(this));

    mDb = new FakeVideoDb(getApplicationContext());
    mAdapter = new VideoAdapter(this, mDb.getAllVideos());
    mAdapter.setCallback(this);
    mBinding.rvVideos.setAdapter(mAdapter);
  }

  private void resolveVideo(String text) {
    // 目前仅支持抖音分享链接，后续会考虑支持快手等
    final String shareUrl = DyUtil.getOriginalShareUrl(text);
    if (TextUtils.isEmpty(shareUrl)) {
      Toast.makeText(this, "链接错误: " + text, Toast.LENGTH_SHORT).show();
      return;
    }

    if (mDb.hasVideo(shareUrl)) {
      Log.d(TAG, "resolveVideo() aborted as existed");
      return;
    }

    DyUtil.parseItem(shareUrl, this::postResult);
  }

  private void postResult(VideoItem item) {
    Log.d(TAG, "postResult() " + item);
    if (item == null) return;

    mAdapter.insertVideo(item);
    mBinding.rvVideos.scrollToPosition(0);
    mDb.insert(item);
  }

  @Override public void onWindowFocusChanged(boolean hasFocus) {
    if (hasFocus) {
      fetchClipData();
    }
  }

  @Override public void onPrimaryClipChanged() {
    fetchClipData();
  }

  private void fetchClipData() {
    final ClipData data = mClipboard.getPrimaryClip();
    if (data != null && data.getItemCount() > 0) {
      resolveVideo(data.getItemAt(0).getText().toString());
      return;
    }
    Log.d(TAG, "fetchClipData() no data");
  }

  @Override public void onClickCover(VideoItem item) {
    FullCoverActivity.start(this, item.coverUrl);
  }

  private VideoItem mPendingItem;

  @Override public void onClickState(VideoItem item) {
    switch (item.state) {
      case NONE:
        // 下载
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            mPendingItem = item;
            return;
          }
        }
        downloadVideo(item);
        break;
      case DOWNLOADING:
        // 暂停
        Toast.makeText(this, "暂停", Toast.LENGTH_SHORT).show();
        break;
      case DOWNLOADED:
        // 删除或打开
        Toast.makeText(this, "删除", Toast.LENGTH_SHORT).show();
        break;
    }
  }

  @Override
  public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      downloadVideo(mPendingItem);
      mPendingItem = null;
    }
  }

  private void downloadVideo(final VideoItem item) {
    DyUtil.download(this, item, new DyUtil.DownloadCallback() {
      @Override public void onError(VideoItem item) {
        mAdapter.notifyItemChanged(item);
      }

      @Override public void onComplete(VideoItem item) {
        mAdapter.notifyItemChanged(item);
      }
    });
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mClipboard.removePrimaryClipChangedListener(this);
    DownloadObserver.get().stop(this);
  }
}