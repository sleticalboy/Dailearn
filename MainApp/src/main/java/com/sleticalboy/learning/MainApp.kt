package com.binlee.learning

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import com.binlee.dl.DlManager
import com.binlee.learning.components.receiver.GlobalReceiver
import com.binlee.learning.util.NotificationHelper

/**
 * Created on 18-3-5.
 *
 * @author leebin
 * @version 1.0
 */
class MainApp : Application() {

  // 插件化
  // 1、代码插件化
  //   1.1、原生代码
  //     如何启动清单文件中未声明的 activity？
  //   1.2、so
  // 2、资源插件化
  //   2.1、如何加载新的资源？
  //   2.1、资源 id 冲突如何解决？

  override fun attachBaseContext(base: Context?) {
    super.attachBaseContext(base)
    DlManager.get().init(this)
  }

  override fun onCreate() {
    super.onCreate()
    mApp = this
    adaptAndroidO()
  }

<<<<<<< HEAD:MainApp/src/main/java/com/sleticalboy/learning/MainApp.kt
<<<<<<< HEAD:MainApp/src/main/java/com/sleticalboy/learning/MainApp.kt
  private fun adaptAndroidO() {
=======
  override fun getResources(): Resources {
    return DlManager.resources()
  }

  override fun getAssets(): AssetManager {
    return DlManager.resources().assets
  }

=======
>>>>>>> 90f26f9c (reat: virtual runtime via didi's VirtualApk):MainApp/src/main/java/com/binlee/learning/MainApp.kt
  private fun registerNotificationChannels() {
>>>>>>> 661d1ff1 (feat: hook ActivityThread#mInstrumentation to start a plugin activity):MainApp/src/main/java/com/binlee/learning/MainApp.kt
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
