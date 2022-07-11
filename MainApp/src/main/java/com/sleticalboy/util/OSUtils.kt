package com.sleticalboy.util

import android.app.ActivityManager
import android.content.Context
import android.text.TextUtils
import android.util.Log

/**
 * Created on 18-5-17.
 *
 * @author leebin
 * @description
 */
object OSUtils {

  private const val LOG_TAG = "OSUtils ->"

  @JvmStatic
  fun isMainProcess(context: Context): Boolean {
    val processName = getProcessName(context, android.os.Process.myPid())
    Log.e(LOG_TAG, "process: $processName")
    return !TextUtils.isEmpty(processName) && !processName!!.contains(":")
  }

  private fun getProcessName(context: Context, pid: Int): String? {
    val am = context.applicationContext.getSystemService(Context.ACTIVITY_SERVICE)
        as ActivityManager
    val runningApps = am.runningAppProcesses ?: return null
    return runningApps.firstOrNull { it.pid == pid }?.processName
  }

}
