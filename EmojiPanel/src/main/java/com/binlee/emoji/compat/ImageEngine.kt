package com.binlee.emoji.compat

import android.content.Context
import android.util.Log
import android.widget.ImageView
import androidx.annotation.IntDef
import java.io.File

abstract class ImageEngine {

  abstract fun show(url: String?, target: ImageView)

  abstract fun showGif(url: String?, target: ImageView)

  abstract fun show(url: String?, target: ImageView, config: Config?, callback: Callback<*>?)

  abstract fun <Res> show(
    resClass: Class<Res>?, url: String?, target: ImageView,
    config: Config?, callback: Callback<*>?
  )

  abstract fun download(context: Context, url: String?, callback: Callback<File?>?)

  abstract fun preload(context: Context?, callback: PreloadCallback?, vararg urls: String?)

  protected abstract fun convertConfig(config: Config?): Any

  class Config : Cloneable {
    // -1 表示原始尺寸
    var mWidth = -1
    var mHeight = -1

    @Cache
    var mCache = Cache.NONE

    @Style
    var mStyle = Style.FIT_CENTER
    var mRadius = 0
    var mDegree = 0
    var mAsGif = false
    public override fun clone(): Config {
      return copy(this)
    }

    companion object {
      fun asGif(): Config {
        val config = Config()
        config.mAsGif = true
        return config
      }

      fun asNull(): Config {
        return Config()
      }

      fun apply(that: Config?): Config {
        return if (that == null) asNull() else copy(that)
      }

      private fun copy(source: Config?): Config {
        if (source == null) {
          return asNull()
        }
        val copy = Config()
        copy.mWidth = source.mWidth
        copy.mHeight = source.mHeight
        copy.mStyle = source.mStyle
        copy.mRadius = source.mRadius
        copy.mDegree = source.mDegree
        copy.mAsGif = source.mAsGif
        return copy
      }
    }
  }

  interface Callback<Res> {
    fun onResReady(model: Any?, res: Res)
    fun onFail(model: Any?, e: Throwable?)
  }

  /**
   * 预加载资源回调
   */
  interface PreloadCallback {
    /**
     * 预加载进度展示
     *
     * @param model     预加载的资源路径
     * @param progress  全部预加载资源进度
     * @param success   加载的单个资源是否成功
     * @param completed 全部资源是否预加载完毕
     */
    fun onProgress(model: Any?, progress: Int, success: Boolean, completed: Boolean)
  }

  interface Logger {
    fun log(tag: String?, msg: String?, e: Throwable?)
  }

  @IntDef(Cache.NONE, Cache.DISK, Cache.RESOURCE, Cache.AUTOMATIC)
  @kotlin.annotation.Retention
  annotation class Cache {
    companion object {
      const val NONE = 0x00
      const val DISK = 0x01
      const val RESOURCE = 0x03
      const val AUTOMATIC = 0x04
    }
  }

  @IntDef(Style.FIT_CENTER, Style.CENTER_CROP, Style.CENTER_INSIDE, Style.CIRCLE, Style.ROUNDED, Style.ROTATE)
  @kotlin.annotation.Retention
  annotation class Style {
    companion object {
      const val FIT_CENTER = 0x10
      const val CENTER_CROP = 0x11
      const val CENTER_INSIDE = 0x12
      const val CIRCLE = 0x13
      const val ROUNDED = 0x14
      const val ROTATE = 0x15
    }
  }

  companion object {
    val ANDROID: Logger = object : Logger {
      override fun log(tag: String?, msg: String?, e: Throwable?) {
        Log.println(
          if (e == null) Log.DEBUG else Log.ERROR, tag, if (e == null) {
            msg
          } else {
            "$msg${Log.getStackTraceString(e)}"
          }!!
        )
      }
    }

    val CALLBACK_NONE: Callback<*> = object : Callback<Any?> {
      override fun onResReady(model: Any?, res: Any?) {}
      override fun onFail(model: Any?, e: Throwable?) {}
    }
  }
}