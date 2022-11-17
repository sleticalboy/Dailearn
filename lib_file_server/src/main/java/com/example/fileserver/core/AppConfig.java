package com.example.fileserver.core;

import android.annotation.SuppressLint;
import android.content.Context;
import com.yanzhenjie.andserver.annotation.Config;
import com.yanzhenjie.andserver.framework.config.Multipart;
import com.yanzhenjie.andserver.framework.config.WebConfig;
import com.yanzhenjie.andserver.framework.website.AssetsWebsite;
import com.yanzhenjie.andserver.framework.website.FileBrowser;
import com.yanzhenjie.andserver.framework.website.StorageWebsite;
import java.io.File;

/**
 * Created on 2022/11/17
 *
 * @author binlee
 */
@Config
public class AppConfig implements WebConfig {

  @SuppressLint("SdCardPath")
  @Override public void onConfig(Context context, Delegate delegate) {

    delegate.addWebsite(new AssetsWebsite(context, "/files"));

    delegate.setMultipart(Multipart.newBuilder()
      .allFileMaxSize(1024 * 1024 * 20) // 20M
      .fileMaxSize(1024 * 1024 * 5) // 5M
      .maxInMemorySize(1024 * 10) // 1024 * 10 bytes
      .uploadTempDir(new File(context.getCacheDir(), "_server_upload_cache_"))
      .build());
  }
}
