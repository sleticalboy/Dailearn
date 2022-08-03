package com.binlee.learning.http.callback;

import androidx.annotation.UiThread;

/**
 * Created on 18-9-3.
 * <p>
 * 回调都已经在 UI 线程了 不需要再做切换线程的操作
 *
 * @author leebin
 */
@UiThread
public interface DownloadCallback extends NetworkCallback {

  /**
   * 下载/上传开始时回调
   *
   * @param total 下载/上传内容的总长度
   */
  void onStart(long total);

  /**
   * 下载/上传进度回调
   *
   * @param progress 进度条
   */
  void onProgress(float progress);

  /**
   * 下载/上传暂停回调
   */
  void onPause();

  /**
   * 取消下载/上传回调
   */
  void onCancel();

  /**
   * 继续下载/上传
   */
  void onResume();

  class SimpleCallback implements DownloadCallback {

    private static final long serialVersionUID = 4926024278512826465L;

    @Override
    public void onError(Throwable e) {
    }

    @Override
    public <T> void onComplete(final T result) {
    }

    @Override
    public void onStart(long total) {
    }

    @Override
    public void onProgress(float progress) {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onResume() {
    }
  }
}
