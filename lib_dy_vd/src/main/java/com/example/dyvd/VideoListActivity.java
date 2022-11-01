package com.example.dyvd;

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
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.dyvd.databinding.ActivityVideoListBinding;
import com.example.dyvd.service.DyService;

public class VideoListActivity extends AppCompatActivity implements VideoAdapter.Callback, Handler.Callback {

  private static final String TAG = "MainActivity";

  private ActivityVideoListBinding mBinding;
  private VideoAdapter mAdapter;

  private Messenger mServer;

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
      resolveVideo(mBinding.tvShareText.getText().toString());
    });
    mBinding.ivClearUrl.setOnClickListener(v -> {
      mBinding.tvShareText.setText(null);
    });
    mBinding.rvVideos.setLayoutManager(new LinearLayoutManager(this));

    mAdapter = new VideoAdapter(this);
    mAdapter.setCallback(this);
    mBinding.rvVideos.setAdapter(mAdapter);

    final Intent intent = new Intent(this, DyService.class);
    intent.putExtra("client_messenger", new Messenger(new Handler(this)));
    bindService(intent, new ServiceConnection() {
      @Override public void onServiceConnected(ComponentName name, IBinder service) {
        mServer = new Messenger(service);
        loadAllVideos();
      }

      @Override public void onServiceDisconnected(ComponentName name) {
        //
      }
    }, Context.BIND_AUTO_CREATE);
  }

  private void loadAllVideos() {
    // 显示 dialog，正在加载
    try {
      // 请求加载数据
      mServer.send(Message.obtain(null, 1));
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  private void resolveVideo(String text) {
    try {
      mServer.send(Message.obtain(null, 2, text));
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  private void postResult(VideoItem item) {
    Log.d(TAG, "postResult() " + item);
    if (item == null) return;

    mAdapter.insertVideo(item);
    mBinding.rvVideos.scrollToPosition(0);
  }

  @Override public boolean handleMessage(@NonNull Message msg) {
    switch (msg.what) {
      case 1:
        mAdapter.replace(msg.getData().getParcelableArrayList("video_items"));
        return true;
      case 2:
        postResult(((VideoItem) msg.obj));
        return true;
      case 3:
        mAdapter.append(msg.getData().getParcelableArrayList("video_items"));
        return true;
      case 4:
      case 5:
      case 6:
        // 开始下载
        // 正在下载
        // 下载完成
        mAdapter.notifyItemChanged((VideoItem) msg.obj);
        return true;
    }
    return false;
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
    try {
      mServer.send(Message.obtain(null, 3, item));
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }
}