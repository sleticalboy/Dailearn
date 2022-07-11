package com.sleticalboy.learning.accounts.sync

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import android.os.SystemClock
import android.util.Log

class RoutineSyncAdapter internal constructor(context: Context?) :
  AbstractThreadedSyncAdapter(context, true) {

  override fun onPerformSync(
    account: Account, extras: Bundle, authority: String,
    provider: ContentProviderClient, syncResult: SyncResult
  ) {
    Log.d(
      TAG, "onPerformSync() called with: account = [" + account + "], extras = [" + extras
          + "], authority = [" + authority + "], provider = [" + provider
          + "], syncResult = [" + syncResult + "], " + Thread.currentThread()
    )
    SystemClock.sleep(800L)
  }

  companion object {
    private const val TAG = "RoutineSyncAdapter"
  }
}