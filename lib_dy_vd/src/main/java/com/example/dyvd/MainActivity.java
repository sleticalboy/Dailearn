package com.example.dyvd;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.dyvd.databinding.ActivityMainBinding;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  private ActivityMainBinding mBinding;
  // key: shareUrl, value: json
  private SharedPreferences mSp;
  private VideoAdapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mBinding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(mBinding.getRoot());

    // 读取剪切板数据，判断是否是抖音分享链接
    // 如果是则发送网络请求，获取响应数据
    // 通过 jsoup 解析响应数据，获取视频下载链接
    // 下载视频并存入数据库

    mBinding.btnResolve.setOnClickListener(v -> {
      resolveDownloadUrl(mBinding.tvHello.getText().toString());
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
    mBinding.rvVideos.setAdapter(mAdapter);
  }

  private void resolveDownloadUrl(String text) {
    final String shareUrl = DyUtil.getOriginalShareUrl(text);
    if (TextUtils.isEmpty(shareUrl)) {
      Toast.makeText(this, "链接错误: " + text, Toast.LENGTH_SHORT).show();
      return;
    }
    // Log.d(TAG, "resolveDownloadUrl() " + text);

    if (mSp.getString(shareUrl, null) != null) {
      // 已存在，不用解析
      Toast.makeText(this, "已存在，不用解析", Toast.LENGTH_SHORT).show();
      return;
    }

    new Thread(() -> {
      try {
        // stream to string
        String result = DyUtil.getClearJson(shareUrl);
        Log.d(TAG, "resolveDownloadUrl() result: " + result);
        runOnUiThread(() -> postResult(VideoItem.parse(shareUrl, result)));
      } catch (IOException | JSONException e) {
        e.printStackTrace();
      }
    }).start();
  }

  private void postResult(VideoItem item) {
    Log.d(TAG, "postResult() " + item);
    if (item == null) return;
    mAdapter.insertVideo(item);
    mBinding.rvVideos.scrollToPosition(0);
    mSp.edit().putString(item.getShareUrl(), item.getTextJson()).apply();
  }

  @Override protected void onResume() {
    super.onResume();

    Log.d(TAG, "onResume() focus: " + getCurrentFocus());

    final ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    if (manager.hasPrimaryClip()) {
      final ClipData data = manager.getPrimaryClip();
      Log.d(TAG, "onResume() clip data: " + data);
      for (int i = 0; i < data.getItemCount(); i++) {
        final ClipData.Item item = data.getItemAt(i);
        resolveDownloadUrl(item.getText().toString());
      }
      return;
    }
    Log.d(TAG, "onResume() no clipboard data");
  }
}