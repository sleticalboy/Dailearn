package com.binlee.learning.camera

import android.hardware.Camera

/**
 * Created on 2023/3/31
 *
 * @author binlee
 */

fun Camera.errorStr(error: Int): String {
  if (error == Camera.CAMERA_ERROR_EVICTED) return "EVICTED"
  if (error == 3) return "DISABLED"
  if (error == Camera.CAMERA_ERROR_SERVER_DIED) return "SERVER_DIED"
  return "UNKNOWN"
}