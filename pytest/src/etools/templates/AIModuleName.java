package com.quvideo.mobile.component.{pkg};

import android.graphics.Bitmap;
import com.quvideo.mobile.component.common.AIFrameInfo;

/**
 * Created on 2022/12/27
 *
 * @author binlee
 */
public class AI{module-name} {

  private long handle;

  public AI{module-name}() {
    handle = Q{module-name}.init();
  }

  /**
   * 执行图片修复
   *
   * @param source 原图
   * @param width 期望修复宽度
   * @param height 期望修复尺高度
   * @return {@link Bitmap} 修复后图片
   */
  public Bitmap forward(Bitmap source, int width, int height) {
    if (handle == 0) return null;

    final AIFrameInfo output =  new AIFrameInfo();
    int res = Q{module-name}.nativeForward4J(handle, new AIFrameInfo(source), width, height, output);
    return res == 0 ? AIFrameInfo.toBitmap(output) : null;
  }

  /** 释放算法句柄 */
  public void release() {
    if (handle == 0) return;

    Q{module-name}.nativeRelease(handle);
    handle = 0;
  }
}
