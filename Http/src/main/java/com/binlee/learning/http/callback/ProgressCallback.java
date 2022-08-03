package com.binlee.learning.http.callback;

/**
 * Created on 18-9-3.
 *
 * @author leebin
 */
public interface ProgressCallback {

  /**
   * 开始下载之前回调
   *
   * @param total 下载内容总长度
   */
  void onPreExecute(long total);

  /**
   * 瞎子啊进度回调
   *
   * @param progress 进度条
   * @param read 已下载文件长度
   */
  void onProgress(float progress, long read);

  class SimpleCallback implements ProgressCallback {

    @Override
    public void onPreExecute(long total) {
    }

    @Override
    public void onProgress(float progress, long read) {
    }
  }
}
