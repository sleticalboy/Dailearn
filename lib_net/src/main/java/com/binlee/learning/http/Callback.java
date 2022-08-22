package com.binlee.learning.http;

/**
 * Created on 2021/7/29
 *
 * @author binli@faceunity.com
 */
public interface Callback {

  /**
   * 下载出错时回调
   *
   * @param e {@link Throwable}
   */
  void onError(Throwable e);
}
