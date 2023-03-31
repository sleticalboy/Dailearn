package com.binlee.learning.camera.v1

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.util.Log
import android.util.Size
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.camera.CameraWrapper
import com.binlee.learning.camera.CameraWrapper.Callback
import com.binlee.learning.camera.Face
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

    override fun onFaceDetected(faces: Array<Face>, displayOrientation: Int) {
      // 更新人脸位置
      binding.faceView.setFaces(faces, displayOrientation, mFront)
    }

    override fun onTakePictureDone(path: File?) {
      // 拍照完成回调
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
    // 点击对焦、上下滑动切换相机
    // val gesture = GestureDetector(this, object: SimpleOnGestureListener() {
    //   override fun onSingleTapUp(e: MotionEvent?): Boolean {
    //     mCamera.autoFocus(this@LiveCameraActivity, binding.focusIcon, e!!)
    //     return true
    //   }
    // })
    binding.surfaceView.setOnTouchListener { _, event ->
      mCamera.autoFocus(binding.focusIcon, binding.surfaceView, event)
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
    // 拍照
    binding.btnTakePic.setOnClickListener {
      mCamera.takePicture(getExternalFilesDir("picture")?.absolutePath!!)
    }
    // 切换摄像头
    binding.btnSwitchCamera.setOnClickListener {
      mFront = if (mFront) {
        binding.faceView.clearFaces()
        mCamera.open(CameraWrapper.ID_BACK)
        false
      } else {
        binding.faceView.clearFaces()
        mCamera.open(CameraWrapper.ID_FRONT)
        true
      }
      it.animate()
        .rotation(it.rotation + 180)
        .setDuration(500L)
        .start()
    }
  }

  private fun tryStartPreview() {
    if (!hasPermission(Manifest.permission.CAMERA)) {
      askPermission(arrayOf(Manifest.permission.CAMERA))
      return
    }
    binding.faceView.clearFaces()
    mCamera.open(CameraWrapper.ID_BACK)
  }

  override fun onPause() {
    super.onPause()
    mCamera.stopPreview()
  }

  override fun onDestroy() {
    super.onDestroy()
    mCamera.close()
  }
}