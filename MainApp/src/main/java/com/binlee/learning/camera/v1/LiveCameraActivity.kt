package com.binlee.learning.camera.v1

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.camera.CameraManager
import com.binlee.learning.camera.CameraManager.OnPictureTakenCallback
import com.binlee.learning.camera.CameraManager.SimpleSurfaceTextureListener
import com.binlee.learning.databinding.ActivityLiveCameraBinding
import java.io.File

/**
 * Created on 18-2-27.
 *
 * @author leebin
 * @version 1.0
 */
class LiveCameraActivity : BaseActivity() {

  private lateinit var binding: ActivityLiveCameraBinding
  private var surface: SurfaceTexture? = null
  private var size: Size? = null

  override fun logTag(): String = "LiveCamera"

  override fun requiredPermissions(): Array<String> {
    return if (hasPermission(Manifest.permission.CAMERA)) arrayOf() else arrayOf(Manifest.permission.CAMERA)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == PERM_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      tryStartPreview()
    }
  }

  override fun layout(): View {
    // return R.layout.activity_live_camera
    binding = ActivityLiveCameraBinding.inflate(layoutInflater)
    return binding.root
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun initView() {
    // 对焦
    binding.viewCover.setOnTouchListener { _, event ->
      CameraManager.get().autoFocus(this, binding.focusIcon, event)
      false
    }
    binding.surfaceView.surfaceTextureListener = object : SimpleSurfaceTextureListener() {
      override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        this@LiveCameraActivity.surface = surface
        this@LiveCameraActivity.size = Size(width, height)
        tryStartPreview()
      }
    }
    binding.btnTakePic.setOnClickListener { takePicture() }
  }

  private fun tryStartPreview() {
    if (hasPermission(Manifest.permission.CAMERA) && surface != null && size != null) {
      val size = CameraManager.get().startPreview(surface)
      if (size == null) {
        Toast.makeText(this, "相机打开失败！", Toast.LENGTH_SHORT).show()
        // finish()
      } else {
        Log.d(logTag(), "tryStartPreview() size: $size")
        // 调整预览窗口大小
        val params = binding.surfaceView.layoutParams as FrameLayout.LayoutParams
        params.width = size.width
        params.height = size.height
        binding.surfaceView.layoutParams = params
      }
    }
  }

  private fun takePicture() {
    CameraManager.get().takePicture(getExternalFilesDir("picture")?.absolutePath!!,
      object : OnPictureTakenCallback {
        override fun onSuccess(picture: File) {
          Log.d(logTag(), picture.path)
        }

        override fun onFailure(e: Throwable?) {
          Log.e(logTag(), "onFailure: ", e)
        }
      }
    )
  }
}