package com.binlee.emoji.helper

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Process

/**
 * Created on 19-7-24.
 *
 * @author leebin
 */
class ThreadHelper private constructor() {

  companion object {

    private var sAsyncHandler: Handler? = null
    private var sMainHandler: Handler? = null

    fun mainHandler(): Handler {
      if (sMainHandler == null) {
        sMainHandler = Handler(Looper.getMainLooper())
      }
      return sMainHandler!!
    }

    @Synchronized
    fun asyncHandler(): Handler {
      if (sAsyncHandler == null) {
        val handlerThread = HandlerThread(
          "sAsyncHandlerThread", Process.THREAD_PRIORITY_BACKGROUND
        )
        handlerThread.start()
        // getLooper() 方法可能会发生阻塞
        sAsyncHandler = Handler(handlerThread.looper)
      }
      return sAsyncHandler!!
    }

    fun runOnMain(task: Runnable) {
      mainHandler().post(task)
    }

    fun runOnMain(task: Runnable, delayMillis: Long) {
      mainHandler().postDelayed(task, delayMillis)
    }

    fun runOnWorker(task: Runnable) {
      asyncHandler().post(task)
    }

    fun runOnWorker(task: Runnable, delayMillis: Long) {
      asyncHandler().postDelayed(task, delayMillis)
    }

    val isMain: Boolean
      get() = Looper.getMainLooper().thread === Thread.currentThread()
  }

  init {
    throw AssertionError("no instance")
  }
}