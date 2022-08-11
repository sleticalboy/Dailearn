package com.binlee.emoji.helper

import android.text.TextUtils
import java.io.File

class UrlHelper private constructor() {

  companion object {

    fun inspectUrl(url: String?): String? {
      var out = url
      if (TextUtils.isEmpty(out)) {
        return out
      }
      if (isFile(out) || isHttp(out) || isAndroid(out)) {
        return out
      }
      if (!isHttp(out) && out!!.startsWith("/")) {
        out = baseUrl() + out
      } else if (!isHttp(out) && !out!!.startsWith("/")) {
        out = baseUrl() + File.separator + out
      }
      return out
    }

    fun isFile(url: String?): Boolean {
      return url != null && url.startsWith("file://")
    }

    fun isHttp(url: String?): Boolean {
      return url != null && url.startsWith("http")
    }

    fun isAndroid(url: String?): Boolean {
      return url != null && url.startsWith("android.")
    }

    fun baseUrl(): String {
      return ""
    }
  }

  init {
    throw AssertionError("no instance.")
  }
}