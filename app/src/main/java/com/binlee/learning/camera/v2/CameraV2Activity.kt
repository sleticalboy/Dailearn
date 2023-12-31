package com.binlee.learning.camera.v2

import android.Manifest
import android.view.View
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.databinding.ActivityEmptyBinding

/**
 * Created on 18-2-26.
 *
 * @author leebin
 * @version 1.0
 * @description
 */
class CameraV2Activity : BaseActivity() {

  private lateinit var mCamera: CameraV2

  override fun initData() {
    mCamera = CameraV2(this)
  }

  override fun initView() {}

  override fun layout(): View {
    // R.layout.activity_empty
    return ActivityEmptyBinding.inflate(layoutInflater).root
  }

  override fun onStart() {
    super.onStart()
    askPermission(arrayOf(Manifest.permission.CAMERA))
  }

  override fun whenPermissionResult(permissions: Array<out String>, grantResults: BooleanArray) {
    if (grantResults[0]) mCamera.openCamera()
  }
}