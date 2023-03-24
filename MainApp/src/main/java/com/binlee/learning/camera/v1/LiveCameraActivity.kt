package com.binlee.learning.camera.v1

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.View
import android.view.ViewGroup
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

  override fun whenPermissionResult(permissions: Array<out String>, grantResults: BooleanArray) {
    if (grantResults[0]) tryStartPreview()
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
        tryStartPreview()
      }
    }
    binding.btnTakePic.setOnClickListener { takePicture() }
  }

  private fun tryStartPreview() {
    if (!hasPermission(Manifest.permission.CAMERA)) {
      askPermission(arrayOf(Manifest.permission.CAMERA))
      return
    }
    if (surface != null) {
      val size = CameraManager.get().startPreview(surface)
      if (size == null) {
        Toast.makeText(this, "相机打开失败！", Toast.LENGTH_SHORT).show()
        finish()
      } else {
        Log.d(logTag(), "tryStartPreview() size: $size")
        // 调整预览窗口大小
        val params = binding.surfaceView.layoutParams as ViewGroup.MarginLayoutParams
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