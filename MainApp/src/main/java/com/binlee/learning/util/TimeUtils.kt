package com.binlee.learning.util

import android.text.TextUtils
import android.util.Log
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Created on 18-5-11.
 *
 *
 * 日期工具类
 *
 * @author leebin
 */
object TimeUtils {

  private val DEFAULT_FORMAT = SimpleDateFormat(
    "yyyy-MM-dd'T'HH:mm:ss+08:00",
    Locale.SIMPLIFIED_CHINESE
  )

  fun getDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
    var lat1 = lat1
    var lng1 = lng1
    var lat2 = lat2
    var lng2 = lng2
    lng1 *= 0.01745329251994329
    lat1 *= 0.01745329251994329
    lng2 *= 0.01745329251994329
    lat2 *= 0.01745329251994329
    val var12 = sin(lng1)
    val var14 = sin(lat1)
    val var16 = cos(lng1)
    val var18 = cos(lat1)
    val var20 = sin(lng2)
    val var22 = sin(lat2)
    val var24 = cos(lng2)
    val var26 = cos(lat2)
    val var28 = DoubleArray(3)
    val var29 = DoubleArray(3)
    var28[0] = var18 * var16
    var28[1] = var18 * var12
    var28[2] = var14
    var29[0] = var26 * var24
    var29[1] = var26 * var20
    var29[2] = var22
    val var30 = sqrt(
      (var28[0] - var29[0]) * (var28[0] - var29[0])
          + (var28[1] - var29[1]) * (var28[1] - var29[1])
          + (var28[2] - var29[2]) * (var28[2] - var29[2])
    )
    val var31 = asin(var30 / 2.0) * 1.27420015798544E7
    return DecimalFormat("##.###").format(var31).toFloat()
  }

  fun str2millis(timeStr: String?): Long {
    return if (!TextUtils.isEmpty(timeStr)) {
      try {
        DEFAULT_FORMAT.parse(timeStr).time
      } catch (e: ParseException) {
        Log.d("TimeUtils", "${e.message}")
        0L
      }
    } else 0L
  }

  fun millis2str(time: Long): String? {
    return if (time > 0L) {
      DEFAULT_FORMAT.format(Date(time))
    } else null
  }
}