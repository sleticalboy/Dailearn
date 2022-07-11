package com.sleticalboy.learning

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import com.binlee.sample.jni.LibJni
import com.sleticalboy.learning.components.receiver.GlobalReceiver
import com.sleticalboy.util.NotificationHelper

/**
 * Created on 18-3-5.
 *
 * @author leebin
 * @version 1.0
 */
class MainApp : Application() {

  override fun attachBaseContext(base: Context?) {
    super.attachBaseContext(base)
    Log.d(TAG, "attachBaseContext() ${LibJni.nativeGetString()}")
  }

  override fun onCreate() {
    super.onCreate()
    mApp = this
    adaptAndroidO()
  }

  private fun adaptAndroidO() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    Log.d("MainApp", "adapt android O")
    registerReceivers()
    NotificationHelper.createAllChannels(this)
  }

  private fun registerReceivers() {
    val intentFilter = IntentFilter()
    intentFilter.addAction(Intent.ACTION_SCREEN_ON)
    intentFilter.addAction(Intent.ACTION_USER_PRESENT)
    registerReceiver(GlobalReceiver(), intentFilter)
  }

  companion object {

    private const val TAG = "MainApp"

    private var mApp: Application? = null

    val app: Context? get() = mApp
  }
}
