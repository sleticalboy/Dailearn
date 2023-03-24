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

  override fun onStart() {
    super.onStart()
    askPermission(arrayOf(Manifest.permission.CAMERA))
  }

  override fun whenPermissionResult(permissions: Array<out String>, grantResults: BooleanArray) {
    if (grantResults[0])
    mCameraCompat.openCamera()
  }
}