package com.binlee.learning.camera.v1

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
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
import com.binlee.learning.databinding.ActivityCameraBinding
import java.io.File
import kotlin.math.roundToInt

/**
 * Created on 18-2-27.
 *
 * @author leebin
 * @version 1.0
 */
class CameraActivity : BaseActivity() {

  private lateinit var binding: ActivityCameraBinding
  private var mSurface: SurfaceTexture? = null
  private var mCamera: CameraWrapper = CameraWrapper(this, object : Callback {
    override fun onOpened(preferredPreviewSize: Size?, camera: CameraWrapper?) {
      if (preferredPreviewSize == null) {
        Toast.makeText(this@CameraActivity, "相机打开失败！", Toast.LENGTH_SHORT).show()
        finish()
        return
      }
      // d.w / p.w = d.h / p.h
      val width = windowManager.defaultDisplay.width
      val height = width * 1f / preferredPreviewSize.width * preferredPreviewSize.height
      Log.d(TAG, "tryStartPreview() size: $preferredPreviewSize, real w: $width, h: $height")
      // 调整预览窗口大小
      val params = binding.surfaceView.layoutParams as ViewGroup.MarginLayoutParams
      params.width = width
      params.height = height.roundToInt()
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
    // return R.layout.activity_camera
    binding = ActivityCameraBinding.inflate(layoutInflater)
    return binding.root
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun initView() {
    // 状态栏
    val attr = window.attributes
    attr.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LOW_PROFILE
    window.attributes = attr

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
        Log.d(TAG, "onSurfaceTextureAvailable() width = $width, height = $height")
        mSurface = surface
        tryStartPreview()
      }

      override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceTextureSizeChanged() width = $width, height = $height")
        mSurface = surface
        mCamera.updateSurface(surface)
      }

      override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        Log.d(TAG, "onSurfaceTextureDestroyed()")
        mCamera.stopPreview()
        return true
      }

      override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // Log.d(TAG, "onSurfaceTextureUpdated() called with: surface = $surface")
      }
    }
    // 缩略图，进相册
    binding.btnThumbnail.setOnClickListener {
      Toast.makeText(this, "Open Gallery!", Toast.LENGTH_SHORT).show()
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
    binding.bottomCover.setBackgroundColor(Color.GRAY)
    // 事件拦截掉，不能传给 camera surface
    binding.bottomCover.setOnClickListener { }

    binding.btnSettings.setOnClickListener {
      Toast.makeText(this, "Open Settings!", Toast.LENGTH_SHORT).show()
    }
    binding.btnFlashMode.setOnClickListener {
      Toast.makeText(this, "Open Flash Mode!", Toast.LENGTH_SHORT).show()
    }
    binding.btnExposure.setOnClickListener {
      Toast.makeText(this, "Open Exposure!", Toast.LENGTH_SHORT).show()
    }
    binding.btnWhiteBalance.setOnClickListener {
      Toast.makeText(this, "Open White Balance!", Toast.LENGTH_SHORT).show()
    }
    binding.btnRatio.setOnClickListener {
      Toast.makeText(this, "Open Ratio!", Toast.LENGTH_SHORT).show()
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