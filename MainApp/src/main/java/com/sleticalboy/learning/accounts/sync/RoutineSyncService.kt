package com.sleticalboy.learning.accounts.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder

class RoutineSyncService : Service() {

  override fun onCreate() {
    super.onCreate()
    synchronized(LOCK) {
      if (sSyncAdapter == null) {
        sSyncAdapter = RoutineSyncAdapter(applicationContext)
      }
    }
  }

  override fun onBind(intent: Intent): IBinder? {
    return sSyncAdapter!!.syncAdapterBinder
  }

  companion object {
    private val LOCK = Any()
    private var sSyncAdapter: RoutineSyncAdapter? = null
  }
}