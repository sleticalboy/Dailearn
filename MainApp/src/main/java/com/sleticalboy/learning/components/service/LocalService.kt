package com.sleticalboy.learning.components.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class LocalService : Service() {

  private var mBinder: LocalBinder? = null

  override fun onCreate() {
    super.onCreate()
    Log.d(TAG, "onCreate() called")
  }

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    Log.d(TAG, "onStartCommand() called with startId: $startId")
    return super.onStartCommand(intent, flags, startId)
  }

  override fun onBind(intent: Intent): IBinder? {
    Log.d(TAG, "onBind() called intent: $intent")
    if (mBinder == null) {
      mBinder = LocalBinder(this)
    }
    return mBinder
  }

  override fun onUnbind(intent: Intent): Boolean {
    Log.d(TAG, "onUnbind() called with intent: $intent")
    return super.onUnbind(intent)
  }

  override fun onDestroy() {
    super.onDestroy()
    Log.d(TAG, "onDestroy() called")
  }

  class LocalBinder(private val mService: LocalService) : Binder() {

    fun getService(): LocalService {
      return mService
    }
  }

  companion object {
    private const val TAG = "LocalService"
  }
}