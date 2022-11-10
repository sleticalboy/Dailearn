package com.example.freevideo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.freevideo.databinding.ActivityVideoListBinding;
import com.example.freevideo.service.DyBinder;
import com.example.freevideo.service.DyService;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;

public class VideoListActivity extends AppCompatActivity implements VideoAdapter.Callback,
  ServiceConnection, Handler.Callback {

  private static final String TAG = "VideoListActivity";

  private ActivityVideoListBinding mBinding;
  private VideoAdapter mAdapter;

  private DyBinder mService;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mBinding = ActivityVideoListBinding.inflate(getLayoutInflater());
    setContentView(mBinding.getRoot());

    // 读取剪切板数据，判断是否是抖音分享链接
    // 如果是则发送网络请求，获取响应数据
    // 通过 jsoup 解析响应数据，获取视频下载链接
    // 下载视频并存入数据库

    mBinding.btnResolveUrl.setOnClickListener(v -> {
      mService.requestResolveVideo(mBinding.tvShareText.getText().toString());
    });
    mBinding.ivClearUrl.setOnClickListener(v -> {
      mBinding.tvShareText.setText(null);
    });
    mBinding.rvVideos.setLayoutManager(new LinearLayoutManager(this));
    mBinding.rvVideos.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    mAdapter = new VideoAdapter(this);
    mBinding.rvVideos.setAdapter(mAdapter);

    bindService(new Intent(this, DyService.class), this, Context.BIND_AUTO_CREATE);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    unbindService(this);
  }

  @Override public void onServiceConnected(ComponentName name, IBinder service) {
    mService = (DyBinder) service;
    mService.attach(new Handler(Looper.getMainLooper(), this));
    mService.requestLoadAll();
  }

  @Override public void onServiceDisconnected(ComponentName name) {
    Log.d(TAG, "onServiceDisconnected()  name: " + name);
  }

  @SuppressWarnings("unchecked")
  @Override public boolean handleMessage(@NonNull Message msg) {
    switch (msg.what) {
      case 1:
        mAdapter.replace((List<VideoItem>) msg.obj);
        return true;
      case 2:
        mAdapter.insertVideo((VideoItem) msg.obj);
        mBinding.rvVideos.scrollToPosition(0);
        return true;
      case 4:
      case 5:
      case 6:
        // 开始下载
        // 正在下载
        // 下载完成
        mAdapter.notifyItemChanged((VideoItem) msg.obj);
        return true;
      default:
        return false;
    }
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
            requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 100);
            mPendingItem = item;
            return;
          }
        }
        mService.requestDownloadVideo(item);
        break;
      case DOWNLOADING:
        // 暂停
        Toast.makeText(this, "暂停", Toast.LENGTH_SHORT).show();
        break;
      case DOWNLOADED:
        // 删除或打开
        Toast.makeText(this, "删除", Toast.LENGTH_SHORT).show();
        break;
      case BROKEN:
        // 重试或删除
        Snackbar.make(mBinding.getRoot(), "重试?", Snackbar.LENGTH_SHORT)
          .setAction(R.string.ok, v -> {
            mService.requestDownloadVideo(item);
          })
          .show();
        break;
    }
  }

  @Override public boolean onLongClick(VideoItem item) {
    Snackbar.make(mBinding.getRoot(), R.string.confirm_delete, Snackbar.LENGTH_SHORT)
      .setAction(R.string.ok, v -> {
        mAdapter.remove(item);
        mService.requestRemoveVideo(item);
      })
      .show();
    return true;
  }

  @Override
  public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
    @NonNull final int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      mService.requestDownloadVideo(mPendingItem);
      mPendingItem = null;
    }
  }
}