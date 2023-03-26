package com.binlee.learning.components.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class GlobalReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val action = intent.action
    if (Intent.ACTION_SCREEN_ON == action || Intent.ACTION_USER_PRESENT == action) {
      Log.d(TAG, "onReceive() receive: $action, start work.")
    }
  }

  companion object {
    private const val TAG = "GlobalReceiver"
  }
}