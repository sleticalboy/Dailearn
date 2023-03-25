package com.binlee.learning.camera.v1

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Log
import android.util.Size
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.camera.CameraWrapper
import com.binlee.learning.camera.CameraWrapper.Callback
import com.binlee.learning.camera.CameraWrapper.OnPictureTakenCallback
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
  private var mSurface: SurfaceTexture? = null
  private var mCamera: CameraWrapper = CameraWrapper(this, object : Callback {
    override fun onOpened(preferredPreviewSize: Size?, camera: CameraWrapper?) {
      if (preferredPreviewSize == null) {
        Toast.makeText(this@LiveCameraActivity, "相机打开失败！", Toast.LENGTH_SHORT).show()
        finish()
        return
      }
      Log.d(TAG, "tryStartPreview() size: $preferredPreviewSize")
      // 调整预览窗口大小
      val params = binding.surfaceView.layoutParams as ViewGroup.MarginLayoutParams
      params.width = preferredPreviewSize.width
      params.height = preferredPreviewSize.height
      binding.surfaceView.layoutParams = params
      // 开始预览
      camera?.startPreview(mSurface)
    }

    override fun onFaceDetected(faces: Array<Camera.Face>) {
      // 更新人脸位置
    }
  })
  private var mFront = false

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
    binding.surfaceView.setOnTouchListener { _, event ->
      mCamera.autoFocus(this, binding.focusIcon, event)
      false
    }
    binding.surfaceView.surfaceTextureListener = object : SurfaceTextureListener {
      override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceTextureAvailable() called with: surface = $surface, width = $width, height = $height")
        this@LiveCameraActivity.mSurface = surface
        tryStartPreview()
      }

      override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceTextureSizeChanged() width = $width, height = $height")
      }

      override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        Log.d(TAG, "onSurfaceTextureDestroyed() called with: surface = $surface")
        mCamera.stopPreview()
        return true
      }

      override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // Log.d(TAG, "onSurfaceTextureUpdated() called with: surface = $surface")
      }
    }
    binding.btnTakePic.setOnClickListener { takePicture() }
    binding.btnSwitchCamera.setOnClickListener {
      mFront = if (mFront) {
        mCamera.open(CameraWrapper.ID_BACK)
        false
      } else {
        mCamera.open(CameraWrapper.ID_FRONT)
        true
      }
    }
  }

  private fun tryStartPreview() {
    if (!hasPermission(Manifest.permission.CAMERA)) {
      askPermission(arrayOf(Manifest.permission.CAMERA))
      return
    }
    mCamera.open(CameraWrapper.ID_BACK)
  }

  private fun takePicture() {
    mCamera.takePicture(getExternalFilesDir("picture")?.absolutePath!!,
        object : OnPictureTakenCallback {
          override fun onSuccess(picture: File) {
            Log.d(TAG, picture.path)
          }

          override fun onFailure(e: Throwable?) {
            Log.e(TAG, "onFailure: ", e)
          }
        }
    )
  }
}