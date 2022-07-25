package com.binlee.util

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper

/**
 * Created on 20-4-2.
 *
 * @author binlee sleticalboy@gmail.com
 */
object ThreadHelper {

  private var sMain: Handler? = null
  private var sWorker: Handler? = null

  private fun ensureMain() {
    if (sMain != null) return
    sMain = Handler(Looper.getMainLooper())
  }

  private fun ensureWorker() {
    if (sWorker != null) return
    val thread = HandlerThread("ThreadHelper")
    thread.start()
    sWorker = Handler(thread.looper)
  }

  private fun exec(main: Boolean, task: Runnable?, delay: Long) {
    if (task == null) return
    if (main) {
      if (Looper.getMainLooper() == Looper.myLooper()) {
        task.run()
        return
      }
      ensureMain()
    } else {
      ensureWorker()
    }
    (if (main) sMain else sWorker)!!.postDelayed(task, delay)
  }

  fun runOnMain(task: Runnable?) {
    exec(true, task, -1)
  }

  fun runOnMain(task: Runnable?, delay: Long) {
    exec(true, task, delay)
  }

  fun runOnWorker(task: Runnable?) {
    exec(false, task, -1)
  }

  fun runOnWorker(task: Runnable?, delay: Long) {
    exec(false, task, delay)
  }
}