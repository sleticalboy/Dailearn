package com.binlee.learning.bt.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import java.util.ArrayDeque
import java.util.ArrayList
import java.util.Deque
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created on 20-8-13.
 *
 * @author Ben binli@grandstream.cn
 */
class Dispatcher(private val context: Context) {

  private val mReadyConns: Deque<Connection> = ArrayDeque()
  private val mRunningConns: Deque<Connection> = ArrayDeque()
  private var mMaxRequest = 5
  private var mExecutorService: ExecutorService? = null
  private var hidHost: BluetoothProfile? = null

  private fun promoteAndExecute() {
    val executables: MutableList<Connection> = ArrayList()
    synchronized(this) {
      val it = mReadyConns.iterator()
      while (it.hasNext()) {
        val conn = it.next()
        if (mRunningConns.size >= mMaxRequest) {
          break
        }
        it.remove()
        executables.add(conn)
        mRunningConns.add(conn)
      }
    }
    for (conn in executables) {
      conn.executeOn(executorService())
    }
  }

  private fun executorService(): ExecutorService {
    if (mExecutorService == null) {
      mExecutorService = ThreadPoolExecutor(
        0, 60, 60, TimeUnit.SECONDS,
        SynchronousQueue()
      ) { r: Runnable? ->
        val thread = Thread(r, "Ble Dispatcher")
        thread.isDaemon = false
        thread
      }
    }
    return mExecutorService!!
  }

  fun finish(connection: Connection?) {
    promoteAndExecute()
  }

  fun notifyConnectionState(device: BluetoothDevice?) {
    for (conn in mRunningConns) {
      if (device == conn.device) {
        conn.notifyStateChange()
      }
    }
  }

  fun setMaxRequest(maxRequest: Int) {
    mMaxRequest = maxRequest
    promoteAndExecute()
  }

  fun enqueue(connection: Connection) {
    connection.setDispatcher(this)
    synchronized(this) { mReadyConns.add(connection) }
    promoteAndExecute()
  }

  fun cancel(device: BluetoothDevice?) {
    for (conn in mReadyConns) {
      if (device == conn.device) {
        conn.cancel()
      }
    }
    for (conn in mRunningConns) {
      if (device == conn.device) {
        conn.cancel()
      }
    }
  }

  fun cancelAll() {
    for (connection in mReadyConns) {
      connection.cancel()
    }
    for (connection in mRunningConns) {
      connection.cancel()
    }
  }

  fun setHidHost(proxy: BluetoothProfile?) {
    hidHost = proxy
  }

  fun getHidHost(): BluetoothProfile? = hidHost

  fun getContext(): Context? = context
}