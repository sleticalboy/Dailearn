package com.sleticalboy.util

import android.content.Context

/**
 * Created on 18-6-11.
 *
 * @author leebin
 * @description 单位转换工具
 */
object DimenUtils {

  @JvmStatic
  fun dpToPx(context: Context, dps: Int): Int {
    return Math.round(context.resources.displayMetrics.density * dps)
  }

  @JvmStatic
  fun screenWidth(context: Context): Int {
    return context.resources.displayMetrics.widthPixels
  }

  @JvmStatic
  fun screenHeight(context: Context): Int {
    return context.resources.displayMetrics.heightPixels
  }
}