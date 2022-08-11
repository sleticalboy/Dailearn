package com.binlee.emoji.helper

import android.util.DisplayMetrics

class UiHelper private constructor() {

  companion object {

    fun displayMetrics(): DisplayMetrics {
      return OsHelper.app().resources.displayMetrics
    }

    /**
     * 获取屏幕宽度 单位：像素
     *
     * @return 屏幕宽度
     */
    fun screenWidth(): Int {
      return displayMetrics().widthPixels
    }

    /**
     * 获取屏幕高度 单位：像素
     *
     * @return 屏幕高度
     */
    fun screenHeight(): Int {
      return displayMetrics().heightPixels
    }

    fun dip2px(dipValue: Float): Int {
      val scale = displayMetrics().density
      return (dipValue * scale + 0.5f).toInt()
    }
  }

  init {
    throw AssertionError("on instance.")
  }
}