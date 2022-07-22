package com.sleticalboy.util

import android.content.Intent
import android.os.Bundle
import android.util.Log

/**
 * Created on 18-2-7.
 *
 * @author leebin
 * @version 1.0
 * @description
 */
object ObjectUtils {

  fun equals(actual: Any?, excepted: Any?): Boolean {
    return actual === excepted || if (actual == null) excepted == null else actual == excepted
  }

  private fun printBundle(tag: String, target: Bundle?, level: Int) {
    var lev = level
    if (target == null) {
      Log.w(tag, "printBundle() empty bundle.")
      return
    }
    for (key in target.keySet()) {
      when (val value = target[key]) {
        is Intent -> {
          printBundle(tag, value.extras, lev++)
        }
        is Bundle -> {
          printBundle(tag, value as Bundle?, lev++)
        }
        else -> {
          Log.w(tag, "printBundle() level: $lev, $key: $value")
        }
      }
    }
  }
}
