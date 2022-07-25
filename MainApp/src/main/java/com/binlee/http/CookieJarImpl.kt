package com.binlee.http

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.ArrayList
import java.util.HashSet

/**
 * Created on 18-3-28.
 *
 * @author leebin
 * @description Cookie 持久化「sp, db, file」
 */
class CookieJarImpl(context: Context?) : CookieJar {

  private val mPreferences: SharedPreferences

  override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
    val values: MutableSet<String> = HashSet()
    for (cookie in cookies) {
      values.add(cookie.toString())
    }
    mPreferences.edit().putStringSet(url.host(), values).apply()
  }

  override fun loadForRequest(url: HttpUrl): List<Cookie> {
    val values = mPreferences.getStringSet(url.host(), HashSet())
    val cookies: MutableList<Cookie> = ArrayList()
    for (value in values!!) {
      cookies.add(Cookie.parse(url, value)!!)
    }
    return cookies
  }

  init {
    if (context == null) throw NullPointerException("context == null")
    mPreferences = context.getSharedPreferences("cookie-jar", Context.MODE_PRIVATE)
  }
}