package com.example.dyvd;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
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

import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity implements VideoAdapter.Callback,
        ClipboardManager.OnPrimaryClipChangedListener {

  private static final String TAG = "MainActivity";

  private ActivityVideoListBinding mBinding;
  // key: shareUrl, value: json
  private SharedPreferences mSp;
  private VideoAdapter mAdapter;
  private ClipboardManager mClipboard;

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
      resolveDownloadUrl(mBinding.tvShareText.getText().toString());
    });
    mBinding.ivClearUrl.setOnClickListener(v -> {
      mBinding.tvShareText.setText(null);
    });

    mSp = getSharedPreferences("all_dy_videos", MODE_PRIVATE);

    mBinding.rvVideos.setLayoutManager(new LinearLayoutManager(this));
    final List<VideoItem> items = new ArrayList<>();
    for (String key : mSp.getAll().keySet()) {
      final String text = mSp.getString(key, null);
      if (text != null) {
        items.add(VideoItem.parse(key, text));
      }
    }
    mAdapter = new VideoAdapter(this, items);
    mAdapter.setCallback(this);
    mBinding.rvVideos.setAdapter(mAdapter);
  }

  private void resolveDownloadUrl(String text) {
    // 目前仅支持抖音分享链接，后续会考虑支持快手等
    final String shareUrl = DyUtil.getOriginalShareUrl(text);
    if (TextUtils.isEmpty(shareUrl)) {
      Toast.makeText(this, "链接错误: " + text, Toast.LENGTH_SHORT).show();
      return;
    }

    if (mSp.getString(shareUrl, null) != null) {
      // Toast.makeText(this, "已存在，不用解析", Toast.LENGTH_SHORT).show();
      Log.d(TAG, "resolveDownloadUrl() 已存在，不用解析");
      return;
    }

    // Log.d(TAG, "resolveDownloadUrl() " + text);
    DyUtil.parseItem(shareUrl, this::postResult);
  }

  private void postResult(VideoItem item) {
    Log.d(TAG, "postResult() " + item);
    if (item == null) return;

    mAdapter.insertVideo(item);
    mBinding.rvVideos.scrollToPosition(0);
    mSp.edit().putString(item.getShareUrl(), item.getTextJson()).apply();
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
      Log.d(TAG, "fetchClipData() clip data: " + data + ", size: " + data.getItemCount());
      resolveDownloadUrl(data.getItemAt(0).getText().toString());
      return;
    }
    Log.d(TAG, "fetchClipData() no data");
  }

  @Override public void onCoverClick(String coverUrl) {
    FullCoverActivity.start(this, coverUrl);
  }

  private String mPendingUrl;

  @Override public void onStateClick(DyState state, String url) {
    Log.d(TAG, "onStateClick() called with: state = [" + state + "], url = [" + url + "]");
    switch (state) {
      case NONE:
        // 下载
        Toast.makeText(this, "下载", Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            mPendingUrl = url;
            return;
          }
        }
        downloadVideo(url);
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
      downloadVideo(mPendingUrl);
      mPendingUrl = null;
    }
  }

  private void downloadVideo(final String url) {
    DyUtil.download(this, url, new DyUtil.DownloadCallback() {
    });
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mClipboard.removePrimaryClipChangedListener(this);
  }
}