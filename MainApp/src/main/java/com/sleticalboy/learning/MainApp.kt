package com.binlee.learning

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.os.Build
import android.util.Log
import com.binlee.learning.components.receiver.GlobalReceiver
import com.binlee.learning.plugin.PluginManager
import com.binlee.learning.plugin.PluginManager.Config
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
    val config = Config()
    config.setParentResource(super.getResources())
    config.setParentClassLoader(super.getClassLoader())
    PluginManager.initialized(config)
  }

  override fun getClassLoader(): ClassLoader {
    return PluginManager.getClassLoader()
  }

  override fun getResources(): Resources {
    return PluginManager.getResources()
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
