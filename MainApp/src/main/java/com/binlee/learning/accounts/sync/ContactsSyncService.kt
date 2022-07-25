package com.binlee.learning.accounts.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Created by AndroidStudio on 20-2-23.
 *
 * @author binlee
 */
class ContactsSyncService : Service() {

  override fun onCreate() {
    synchronized(LOCK) {
      if (sSyncAdapter == null) {
        sSyncAdapter = ContactsSyncAdapter(applicationContext)
      }
    }
  }

  override fun onBind(intent: Intent): IBinder? {
    return sSyncAdapter!!.syncAdapterBinder
  }

  companion object {
    private val LOCK = Any()
    private var sSyncAdapter: ContactsSyncAdapter? = null
  }
}