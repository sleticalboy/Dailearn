package com.binlee.learning.util

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * Created on 18-2-3.
 *
 * @author leebin
 * @version 1.0
 * @description utils for operations about UI
 */
object UiUtils {

  /**
   * 获取屏幕的尺寸
   *
   * @param context
   * @return
   */
  @JvmStatic
  fun getScreenSize(context: Context): Size {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val displayMetrics = DisplayMetrics()
    wm.defaultDisplay.getMetrics(displayMetrics)
    val density = displayMetrics.density
    val width = (displayMetrics.widthPixels * density).toInt()
    val height = (displayMetrics.heightPixels * density).toInt()
    return Size(width, height)
  }

  /**
   * dp to px
   *
   * @param context
   * @param dp
   * @return
   */
  @JvmStatic
  fun dp2px(context: Context, dp: Float): Int {
    val scale = context.resources.displayMetrics.density
    return (dp * scale + 0.5f).toInt()
  }

  /**
   * px to dp
   *
   * @param context
   * @param px
   * @return
   */
  fun px2dp(context: Context, px: Float): Int {
    val scale = context.resources.displayMetrics.density
    return (px / scale + 0.5f).toInt()
  }

  /**
   * sp to px
   *
   * @param context
   * @param sp
   * @return
   */
  fun sp2px(context: Context, sp: Float): Int {
    val fontScale = context.resources.displayMetrics.scaledDensity
    return (sp * fontScale + 0.5f).toInt()
  }

  @JvmStatic
  fun getStatusBarHeight(): Int {
    val res = Resources.getSystem()
    val id = res.getIdentifier("status_bar_height", "dimen", "android")
    return res.getDimensionPixelSize(id)
  }

  class Size internal constructor(val width: Int, val height: Int)
}
