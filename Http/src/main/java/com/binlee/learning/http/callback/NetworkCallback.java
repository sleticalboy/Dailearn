package com.binlee.learning.http.callback;

import java.io.Serializable;

/**
 * Created on 18-9-19.
 *
 * @author leebin
 */
public interface NetworkCallback extends Serializable {

  /**
   * 错误回调
   *
   * @param e {@link Throwable}
   */
  void onError(Throwable e);

  /**
   * 完成回调
   */
  <T> void onComplete(T result);
}
