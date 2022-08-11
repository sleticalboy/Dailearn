package com.binlee.emoji.helper

import android.os.SystemClock
import android.util.Log
import com.binlee.emoji.BuildConfig
import kotlin.math.min
import kotlin.math.pow

/**
 * Created on 19-1-18.
 *
 * @author leebin
 */
object LogHelper {

  private val LOGGABLE = BuildConfig.DEBUG
  private const val MAX_LOG_LENGTH = 4000
  private val MILLIS_MULTIPLIER = 1.0 / 10.0.pow(6.0)

  fun elapsedMillis(logTime: Long): Double {
    return (logTime() - logTime) * MILLIS_MULTIPLIER
  }

  fun logTime(): Long {
    return SystemClock.elapsedRealtimeNanos()
  }

  @JvmStatic
  fun debug(tag: String, msg: String) {
    debug(tag, msg, thr = null)
  }

  /**
   * copy from OkHttp
   */
  @JvmStatic
  fun debug(tag: String?, msg: String, thr: Throwable? = null) {
    var input = msg
    if (!LOGGABLE) {
      return
    }
    if (thr != null) {
      input = "$msg${Log.getStackTraceString(thr)}"
    }

    // Split by line, then ensure each line can fit into Log's maximum length.
    var i = 0
    val length = input.length
    while (i < length) {
      var newline = input.indexOf('\n', i)
      newline = if (newline != -1) newline else length
      do {
        val end = min(newline, i + MAX_LOG_LENGTH)
        Log.println(Log.DEBUG, tag, input.substring(i, end))
        i = end
      } while (i < newline)
      i++
    }
  }

  fun dumpStackTrace(tag: String?) {
    for (element in Thread.currentThread().stackTrace) {
      Log.e(tag, "" + element)
    }
    Log.i(tag, "<============= the current stack trace ==============>")
  }
}