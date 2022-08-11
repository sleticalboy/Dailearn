package com.binlee.emoji.helper

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Process

class OsHelper private constructor() {

  companion object {
    @SuppressLint("StaticFieldLeak")
    @Volatile
    private var sContext: Context? = null
    private var sRetryCount = 0

    val isMasterProcess: Boolean
      get() {
        val processName = getProcessName(Process.myPid())
        return processName != null && !processName.contains(":")
      }

    private val mLock = Object()

    fun getProcessName(pid: Int): String? {
      val am = app().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
      val apps = am.runningAppProcesses
      if (apps != null && apps.size > 0) {
        for (info in apps) {
          if (info.pid == pid) {
            return info.processName
          }
        }
      }
      return null
    }

    fun app(): Context {
      if (sContext == null) {
        // do your own initialization
        initialize()
      }
      return sContext!!
    }

    private fun initialize() {
      if (sContext != null) {
        return
      }
      try {
        val clazz = Class.forName("com.binlee.emoji.DebugApplication")
        val sAppField = clazz.getDeclaredField("sApp")
        sAppField.isAccessible = true
        val sApp = sAppField[null]
        if (sApp is Context) {
          sContext = sApp as Application
        }
        if (sContext != null) {
          return
        }
        sContext = reflectHidenApi()
        // 如果失败则再尝试 4 次
        do {
          mLock.wait(120L)
          sContext = reflectHidenApi()
        } while (sContext == null && sRetryCount < 5)
      } catch (e: Throwable) {
        sContext = null
      } finally {
        mLock.notifyAll()
        sRetryCount = 0
      }
    }

    @Throws(Throwable::class)
    private fun reflectHidenApi(): Context {
      sRetryCount++
      val clazz = Class.forName("android.app.ActivityThread")
      val method = clazz.getDeclaredMethod("currentApplication")
      method.isAccessible = true
      return method.invoke(null) as Application
    }
  }

  init {
    throw AssertionError("no instance.")
  }
}