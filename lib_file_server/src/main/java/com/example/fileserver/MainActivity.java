package com.example.fileserver;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fileserver.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  private ActivityMainBinding mBinding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mBinding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(mBinding.getRoot());

    mBinding.btnStartServer.setOnClickListener(v -> startServer());
    mBinding.btnStopServer.setOnClickListener(v -> stopServer());
    mBinding.tvCurrentUrl.setOnClickListener(v -> copyServerUrl());
    mBinding.tvCurrentUrl.setOnLongClickListener(v -> {
      openInBrowser();
      return true;
    });
    mBinding.tvCurrentWifi.setText("当前连接的 WiFi: " + FileUtil.getWifiLabel(this));

    FileServer.getUrl().observe(this, url -> {

      Log.d(TAG, "onChanged() " + url);

      mBinding.tvCurrentUrl.setTag(url);
      if (url != null) {
        final SpannableString text = new SpannableString("当前服务器链接: " + url);
        text.setSpan(new URLSpan(url), 9, 9 + url.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        mBinding.tvCurrentUrl.setText(text);
      } else {
        mBinding.tvCurrentUrl.setText("当前服务器链接: 无");
        mBinding.tvRecord.setText("");
      }

      mBinding.btnStartServer.setEnabled(url == null);
      mBinding.btnStopServer.setEnabled(url != null);
      mBinding.tvCurrentUrl.setClickable(url != null);
    });

    FileServer.getRequest().observe(this, record -> {
      if (record != null && record.trim().length() != 0) {
        mBinding.tvRecord.append(record + "\n");
      }
    });

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
      || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
      startServer();
      return;
    }

    // 读取文件权限

    new AlertDialog.Builder(this)
      .setTitle("提示")
      .setMessage(getString(R.string.app_name) + " 需要读取文件权限才能运行 ☺☺")
      .setNegativeButton("拒绝", (dialog, which) -> exit())
      .setPositiveButton("确定", (dialog, which) -> {
        requestPermissions(new String[] {
          Manifest.permission.READ_EXTERNAL_STORAGE,
          Manifest.permission.ACCESS_FINE_LOCATION
        }, 0x01);
      })
      .show();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    FileServer.getUrl().removeObservers(this);
  }

  private void openInBrowser() {
    Intent intent = new Intent();
    intent.setAction("android.intent.action.VIEW");
    intent.setData(Uri.parse((String) mBinding.tvCurrentUrl.getTag()));
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);
  }

  private void copyServerUrl() {
    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    clipboard.setPrimaryClip(ClipData.newPlainText("", (String) mBinding.tvCurrentUrl.getTag()));
    Toast.makeText(this, "链接已复制，快去分享给小伙伴吧！", Toast.LENGTH_SHORT).show();
  }

  private void stopServer() {
    stopService(new Intent(this, FileService.class));
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == 0x01) {
      if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
        exit();
      } else {
        startServer();
      }
    }
  }

  private void startServer() {
    // 需要打开 WiFi
    startService(new Intent(this, FileService.class));
  }

  private void exit() {
    Toast.makeText(this, "无法访问文件系统，退出！", Toast.LENGTH_SHORT).show();

    stopServer();

    finish();
  }
}