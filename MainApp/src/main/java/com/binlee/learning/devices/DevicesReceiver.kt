package com.binlee.learning.devices

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Created on 20-2-17.
 *
 * @author sleticalboy
 * @description
 */
class DevicesReceiver : DeviceAdminReceiver() {

  override fun onEnabled(context: Context, intent: Intent) {
    Log.d(TAG, "onEnabled() called with: context = [$context], intent = [$intent]")
  }

  override fun onDisabled(context: Context, intent: Intent) {
    Log.d(TAG, "onDisabled() called with: context = [$context], intent = [$intent]")
  }

  override fun onDisableRequested(context: Context, intent: Intent): CharSequence? {
    Log.d(TAG, "onDisableRequested() called with: context = [$context], intent = [$intent]")
    return null
  }

  companion object {
    private const val TAG = "DevicesReceiver"
  }
}