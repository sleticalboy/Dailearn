package com.binlee.learning.util

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build

/**
 * Created on 20-4-2.
 *
 * @author binlee sleticalboy@gmail.com
 */
object NotificationHelper {

  private const val COMMON_TAG = "Daily-Work"
  const val SPECIAL_TAG = "Special-Notify"
  private var sManager: NotificationManager? = null

  fun createAllChannels(context: Context) {
    ensureManager(context)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    sManager!!.createNotificationChannels(listOf(commonChannel(), messageChannel()))
  }

  @TargetApi(Build.VERSION_CODES.O)
  private fun commonChannel(): NotificationChannel {
    return NotificationChannel(COMMON_TAG, COMMON_TAG, NotificationManager.IMPORTANCE_DEFAULT)
  }

  @TargetApi(Build.VERSION_CODES.O)
  private fun messageChannel(): NotificationChannel {
    val channel = NotificationChannel(
      SPECIAL_TAG, SPECIAL_TAG,
      NotificationManager.IMPORTANCE_DEFAULT
    )
    channel.enableLights(true)
    channel.lightColor = Color.RED
    return channel
  }

  private fun ensureManager(context: Context) {
    if (sManager != null) return
    sManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
  }
}