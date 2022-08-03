package com.binlee.learning.weight.xrefresh.interfaces

import android.view.View

/**
 * Created on 18-2-3.
 *
 * @author leebin
 * @version 1.0
 * @description
 */
interface ILoadView {

  /**
   * 开始加载
   */
  fun begin()

  /**
   * 更新进度
   *
   * @param progress
   * @param total
   */
  fun progress(progress: Long, total: Long)

  /**
   * 加载完成
   *
   * @param progress
   * @param total
   */
  fun finish(progress: Long, total: Long)

  /**
   * 正在加载
   */
  fun loading()

  /**
   * 隐藏
   */
  fun hidden()

  /**
   * 获取当前视图
   *
   * @return 当前视图
   */
  fun get(): View
}
