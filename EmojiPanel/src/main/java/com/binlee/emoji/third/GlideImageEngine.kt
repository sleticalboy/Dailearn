package com.binlee.emoji.third

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.binlee.emoji.compat.ImageEngine
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.Rotate
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class GlideImageEngine : ImageEngine() {

  override fun show(url: String?, target: ImageView) {
    show(url, target, Config.asNull(), CALLBACK_NONE)
  }

  override fun showGif(url: String?, target: ImageView) {
    show(url, target, Config.asGif(), CALLBACK_NONE)
  }

  override fun show(url: String?, target: ImageView, config: Config?, callback: Callback<*>?) {
    show(Drawable::class.java, url, target, config, callback)
  }

  override fun <Res> show(
    resClass: Class<Res>?, url: String?, target: ImageView,
    config: Config?, callback: Callback<*>?
  ) {
    val real = config ?: Config.asNull()
    Glide.with(target).`as`(resClass!!)
      .load(url)
      .apply(convertConfig(config))
      .listener(RequestLogger())
      .into(object : CustomViewTarget<ImageView, Res>(target) {
        override fun onLoadFailed(errorDrawable: Drawable?) = Unit

        override fun onResourceReady(res: Res, transition: Transition<in Res>?) {
          if (res is GifDrawable && real.mAsGif) {
            target.setImageDrawable(res as GifDrawable)
            (res as GifDrawable).start()
          } else if (res is Drawable) {
            target.setImageDrawable(res as Drawable)
          } else if (res is Bitmap) {
            target.setImageBitmap(res as Bitmap)
          }
        }

        override fun onResourceCleared(placeholder: Drawable?) = Unit
      })
  }

  override fun download(context: Context, url: String?, callback: Callback<File?>?) {
    if (callback == null) {
      throw NullPointerException("download callback is null.")
    }
    val target = Glide.with(context.applicationContext)
      .downloadOnly()
      .load(url)
      .listener(RequestLogger())
      .submit()
    try {
      val file = target.get()
      callback.onResReady(url, file)
    } catch (e: Throwable) {
      callback.onFail(url, e)
      ANDROID.log(TAG, "#download error", e)
    }
  }

  override fun preload(context: Context?, callback: PreloadCallback?, vararg urls: String?) {
    require(urls.isNotEmpty()) { "url is null or empty." }
    val counter = AtomicInteger()
    val unit = 100f / urls.size
    val mgr = Glide.with(context!!.applicationContext)
    val listener: RequestLogger<Drawable> = object : RequestLogger<Drawable>() {
      override fun onLoadFailed(
        e: GlideException?, model: Any,
        target: Target<Drawable>, isFirstResource: Boolean
      ): Boolean {
        onProgress(model, false)
        return false
      }

      override fun onResourceReady(
        resource: Drawable, model: Any,
        target: Target<Drawable>, dataSource: DataSource,
        isFirstResource: Boolean
      ): Boolean {
        onProgress(model, true)
        return false
      }

      private fun onProgress(model: Any, success: Boolean) {
        if (callback == null) {
          return
        }
        val count = counter.incrementAndGet()
        callback.onProgress(model, (count * unit).toInt(), success, count == urls.size)
      }
    }
    for (url in urls) {
      mgr.load(url).listener(listener).preload()
    }
  }

  override fun convertConfig(config: Config?): RequestOptions {
    var options = RequestOptions()
    if (config == null) {
      return options.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).dontTransform()
    }
    options = options.override(config.mWidth, config.mHeight)
    options = when (config.mCache) {
      Cache.AUTOMATIC -> {
        options.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
      }
      Cache.RESOURCE -> {
        options.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
      }
      Cache.DISK -> {
        options.skipMemoryCache(true)
      }
      Cache.NONE -> {
        options.diskCacheStrategy(DiskCacheStrategy.NONE)
      }
      else -> {
        // maybe null
        options.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
      }
    }
    options = when (config.mStyle) {
      Style.CENTER_CROP -> {
        options.centerCrop()
      }
      Style.CENTER_INSIDE -> {
        options.centerInside()
      }
      Style.FIT_CENTER -> {
        options.fitCenter()
      }
      Style.CIRCLE -> {
        options.circleCrop()
      }
      Style.ROUNDED -> {
        options.transform(RoundedCorners(config.mRadius))
      }
      Style.ROTATE -> {
        options.transform(Rotate(config.mDegree))
      }
      else -> {
        options.dontTransform()
      }
    }
    return options
  }

  private open class RequestLogger<R> @JvmOverloads constructor(private val mTag: String = DEF_TAG) :
    RequestListener<R> {
    override fun onLoadFailed(
      e: GlideException?, model: Any, target: Target<R>,
      isFirstResource: Boolean
    ): Boolean {
      ANDROID.log(mTag, "onLoadFailed() $model", e)
      return false
    }

    override fun onResourceReady(
      resource: R, model: Any, target: Target<R>,
      dataSource: DataSource, isFirstResource: Boolean
    ): Boolean {
      ANDROID.log(mTag, "onResourceReady() $model", null)
      return false
    }

    companion object {
      private const val DEF_TAG = "RequestLogger"
    }
  }

  companion object {
    private const val TAG = "GlideImageEngine"
  }
}