package com.binlee.learning.http.callback;

import androidx.annotation.UiThread;

/**
 * Created on 18-9-19.
 *
 * @author leebin
 */
@UiThread
public interface UploadCallback extends NetworkCallback {

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

  class SimpleCallback implements UploadCallback {

    private static final long serialVersionUID = -9026111178220529309L;

    @Override
    public void onError(final Throwable e) {
    }

    @Override
    public <T> void onComplete(final T result) {
    }

    @Override
    public void onStart(final long total) {
    }

    @Override
    public void onProgress(final float progress) {
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
