package com.binlee.learning.camera.v1

import android.Manifest
import android.view.View
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.camera.CameraCompat
import com.binlee.learning.databinding.ActivityEmptyBinding

/**
 * Created on 18-2-26.
 *
 * @author leebin
 * @version 1.0
 * @description
 */
class NewCameraActivity : BaseActivity() {

  private lateinit var mCameraCompat: CameraCompat

  override fun initData() {
    mCameraCompat = CameraCompat(this)
  }

  override fun initView() {}

  override fun layout(): View {
    // R.layout.activity_empty
    return ActivityEmptyBinding.inflate(layoutInflater).root
  }

  override fun logTag(): String = "NewCamera"

  override fun requiredPermissions(): Array<String> {
    return arrayOf(Manifest.permission.CAMERA)
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == PERM_REQUEST_CODE) {
      mCameraCompat.openCamera()
    }
  }
}