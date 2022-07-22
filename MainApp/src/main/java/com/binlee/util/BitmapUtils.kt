package com.sleticalboy.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object BitmapUtils {

  private const val IO_BUFFER_SIZE = 2 * 1024

  fun drawable2Bitmap(drawable: Drawable?): Bitmap? {
    if (drawable == null) {
      return null
    }
    // 取 drawable 的长宽
    val w = drawable.intrinsicWidth
    val h = drawable.intrinsicHeight
    // 取 drawable 的颜色格式
    val config = if (drawable.opacity != PixelFormat.OPAQUE) {
      Bitmap.Config.ARGB_8888
    } else {
      Bitmap.Config.RGB_565
    }
    // 建立对应 bitmap
    val bitmap = Bitmap.createBitmap(w, h, config)
    // 建立对应 bitmap 的画布
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, w, h)
    // 把 drawable 内容画到画布中
    drawable.draw(canvas)
    return bitmap
  }

  fun toBlur(originBitmap: Bitmap, scaleRatio: Int): Bitmap? {
    var scaleRatio = scaleRatio
    //        int scaleRatio = 10;
    // 增大scaleRatio缩放比，使用一样更小的bitmap去虚化可以到更好的得模糊效果，而且有利于占用内存的减小；
    val blurRadius = 8 //通常设置为8就行。
    //增大blurRadius，可以得到更高程度的虚化，不过会导致CPU更加intensive

    /* 其中前三个参数很明显，其中宽高我们可以选择为原图尺寸的1/10；
    第四个filter是指缩放的效果，filter为true则会得到一个边缘平滑的bitmap，
    反之，则会得到边缘锯齿、pixelrelated的bitmap。
    这里我们要对缩放的图片进行虚化，所以无所谓边缘效果，filter=false。*/
    if (scaleRatio <= 0) {
      scaleRatio = 10
    }
    val scaledBitmap = Bitmap.createScaledBitmap(
      originBitmap,
      originBitmap.width / scaleRatio,
      originBitmap.height / scaleRatio,
      false
    )
    return doBlur(scaledBitmap, blurRadius, true)
  }

  fun doBlur(sentBitmap: Bitmap, radius: Int, canReuseInBitmap: Boolean): Bitmap? {
    val bitmap: Bitmap
    if (canReuseInBitmap) {
      bitmap = sentBitmap
    } else {
      bitmap = sentBitmap.copy(sentBitmap.config, true)
    }

    if (radius < 1) {
      return null
    }

    val w = bitmap.width
    val h = bitmap.height

    val pix = IntArray(w * h)
    bitmap.getPixels(pix, 0, w, 0, 0, w, h)

    val wm = w - 1
    val hm = h - 1
    val wh = w * h
    val div = radius + radius + 1

    val r = IntArray(wh)
    val g = IntArray(wh)
    val b = IntArray(wh)
    var rsum: Int
    var gsum: Int
    var bsum: Int
    var x: Int
    var y: Int
    var i: Int
    var p: Int
    var yp: Int
    var yi: Int
    var yw: Int
    val vmin = IntArray(max(w, h))

    var divsum = div + 1 shr 1
    divsum *= divsum
    val dv = IntArray(256 * divsum)
    i = 0
    while (i < 256 * divsum) {
      dv[i] = i / divsum
      i++
    }

    yi = 0
    yw = yi

    val stack = Array(div) { IntArray(3) }
    var stackpointer: Int
    var stackstart: Int
    var sir: IntArray
    var rbs: Int
    val r1 = radius + 1
    var routsum: Int
    var goutsum: Int
    var boutsum: Int
    var rinsum: Int
    var ginsum: Int
    var binsum: Int

    y = 0
    while (y < h) {
      bsum = 0
      gsum = bsum
      rsum = gsum
      boutsum = rsum
      goutsum = boutsum
      routsum = goutsum
      binsum = routsum
      ginsum = binsum
      rinsum = ginsum
      i = -radius
      while (i <= radius) {
        p = pix[yi + min(wm, max(i, 0))]
        sir = stack[i + radius]
        sir[0] = p and 0xff0000 shr 16
        sir[1] = p and 0x00ff00 shr 8
        sir[2] = p and 0x0000ff
        rbs = r1 - abs(i)
        rsum += sir[0] * rbs
        gsum += sir[1] * rbs
        bsum += sir[2] * rbs
        if (i > 0) {
          rinsum += sir[0]
          ginsum += sir[1]
          binsum += sir[2]
        } else {
          routsum += sir[0]
          goutsum += sir[1]
          boutsum += sir[2]
        }
        i++
      }
      stackpointer = radius

      x = 0
      while (x < w) {

        r[yi] = dv[rsum]
        g[yi] = dv[gsum]
        b[yi] = dv[bsum]

        rsum -= routsum
        gsum -= goutsum
        bsum -= boutsum

        stackstart = stackpointer - radius + div
        sir = stack[stackstart % div]

        routsum -= sir[0]
        goutsum -= sir[1]
        boutsum -= sir[2]

        if (y == 0) {
          vmin[x] = min(x + radius + 1, wm)
        }
        p = pix[yw + vmin[x]]

        sir[0] = p and 0xff0000 shr 16
        sir[1] = p and 0x00ff00 shr 8
        sir[2] = p and 0x0000ff

        rinsum += sir[0]
        ginsum += sir[1]
        binsum += sir[2]

        rsum += rinsum
        gsum += ginsum
        bsum += binsum

        stackpointer = (stackpointer + 1) % div
        sir = stack[stackpointer % div]

        routsum += sir[0]
        goutsum += sir[1]
        boutsum += sir[2]

        rinsum -= sir[0]
        ginsum -= sir[1]
        binsum -= sir[2]

        yi++
        x++
      }
      yw += w
      y++
    }
    x = 0
    while (x < w) {
      bsum = 0
      gsum = bsum
      rsum = gsum
      boutsum = rsum
      goutsum = boutsum
      routsum = goutsum
      binsum = routsum
      ginsum = binsum
      rinsum = ginsum
      yp = -radius * w
      i = -radius
      while (i <= radius) {
        yi = max(0, yp) + x

        sir = stack[i + radius]

        sir[0] = r[yi]
        sir[1] = g[yi]
        sir[2] = b[yi]

        rbs = r1 - abs(i)

        rsum += r[yi] * rbs
        gsum += g[yi] * rbs
        bsum += b[yi] * rbs

        if (i > 0) {
          rinsum += sir[0]
          ginsum += sir[1]
          binsum += sir[2]
        } else {
          routsum += sir[0]
          goutsum += sir[1]
          boutsum += sir[2]
        }

        if (i < hm) {
          yp += w
        }
        i++
      }
      yi = x
      stackpointer = radius
      y = 0
      while (y < h) {
        // Preserve alpha channel: ( 0xff000000 & pix[yi] )
        pix[yi] = -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

        rsum -= routsum
        gsum -= goutsum
        bsum -= boutsum

        stackstart = stackpointer - radius + div
        sir = stack[stackstart % div]

        routsum -= sir[0]
        goutsum -= sir[1]
        boutsum -= sir[2]

        if (x == 0) {
          vmin[y] = min(y + r1, hm) * w
        }
        p = x + vmin[y]

        sir[0] = r[p]
        sir[1] = g[p]
        sir[2] = b[p]

        rinsum += sir[0]
        ginsum += sir[1]
        binsum += sir[2]

        rsum += rinsum
        gsum += ginsum
        bsum += binsum

        stackpointer = (stackpointer + 1) % div
        sir = stack[stackpointer]

        routsum += sir[0]
        goutsum += sir[1]
        boutsum += sir[2]

        rinsum -= sir[0]
        ginsum -= sir[1]
        binsum -= sir[2]

        yi += w
        y++
      }
      x++
    }

    bitmap.setPixels(pix, 0, w, 0, 0, w, h)

    return bitmap
  }

  fun loadBitmapFromVideo(context: Context, timeUs: Long, uri: Uri): Bitmap? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(context, uri)
    val frameBitmap = retriever.getFrameAtTime(timeUs)
    retriever.release()
    return frameBitmap
  }
}
