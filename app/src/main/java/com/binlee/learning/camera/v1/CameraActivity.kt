package com.binlee.learning.camera.v1

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.Toast
import com.binlee.learning.R
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.camera.CameraV1
import com.binlee.learning.camera.CameraV1.Callback
import com.binlee.learning.camera.CameraX
import com.binlee.learning.camera.Face
import com.binlee.learning.databinding.ActivityCameraBinding
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.Tab
import java.io.File
import kotlin.math.abs
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
  private var mCamera: CameraV1 = CameraV1(this, object : Callback {
    override fun onOpened(preferredPreviewSize: Size?, camera: CameraV1?) {
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
      // 更新场景模式
      updateSceneModes()
      // 开始预览
      camera?.startPreview(mSurface)
      
      CameraX.setCameraId(this@CameraActivity, camera!!.getId())
    }

    override fun onLastClosed(cameraId: Int) {
      Log.d(TAG, "onLastClosed() cameraId = $cameraId")
    }

    override fun onFaceDetected(faces: Array<Face>, displayOrientation: Int) {
      // 更新人脸位置
      updateFaceView(faces, displayOrientation)
    }

    override fun onTakePictureDone(path: File?) {
      // 拍照完成回调
    }
  })

  private fun updateFaceView(faces: Array<Face>, displayOrientation: Int) {
    binding.faceView.setFaces(faces, displayOrientation, mCamera.getId() == CameraV1.ID_FRONT)
  }

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
    // setup status bar
    val attr = window.attributes
    attr.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LOW_PROFILE
    window.attributes = attr
    // setup texture listener
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
    binding.btnSwitchCamera.setOnClickListener { switchCamera(it) }
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

    // 点击对焦、上下滑动切换相机、左右滑动切换场景模式
    binding.surfaceView.setOnTouchListener(object : OnTouchListener {
      private var downX = 0f
      private var downY = 0f
      // 最小滑动距离
      private val minDistance = 120f

      override fun onTouch(v: View?, event: MotionEvent): Boolean {
        // Log.d(TAG, "onTouch() event = $event")
        when (event.action) {
          MotionEvent.ACTION_DOWN -> {
            downX = event.x
            downY = event.y
          }
          MotionEvent.ACTION_UP -> {
            val diffX = event.x - downX
            val diffY = event.y - downY
            Log.d(TAG, "onTouch() up -> dx: $diffX, dy: $diffY")

            // 对焦
            if (abs(diffX) < minDistance && abs(diffY) < minDistance) {
              mCamera.autoFocus(binding.focusView, binding.surfaceView, event.x, event.y)
              return true
            }

            // 左右滑动切换场景模式
            if (abs(diffX) >= minDistance && abs(diffX) > abs(diffY)) {
              var pos = binding.tabScenes.selectedTabPosition
              pos = if (diffX > 0) {
                // 向右滑动
                (pos + 1) % binding.tabScenes.tabCount
              } else {
                // 向左滑动
                if (pos - 1 < 0) {
                  pos = binding.tabScenes.tabCount - 1
                } else {
                  pos -= 1
                }
                pos
              }
              binding.tabScenes.selectTab(binding.tabScenes.getTabAt(pos))
              return true
            }

            // 上下滑动切换相机
            if (abs(diffY) >= minDistance && abs(diffY) > abs(diffX)) {
              switchCamera(binding.btnSwitchCamera)
              return true
            }
          }
          else -> {}
        }
        return true
      }
    })
    binding.tabScenes.setOnTabSelectedListener(object : OnTabSelectedListener {
      override fun onTabSelected(tab: Tab?) {
        // 更新相机场景参数
        mCamera.setSceneMode(tab?.tag as String?)
      }

      override fun onTabUnselected(tab: Tab?) {
      }

      override fun onTabReselected(tab: Tab?) {
      }
    })
  }

  private fun updateSceneModes() {
    binding.tabScenes.removeAllTabs()
    // mode -> mode name
    val items: List<Pair<String, String>> = resources.getStringArray(R.array.camera_scn_modes).map {
      val index = it.indexOf(',')
      Pair(it.substring(index + 1), it.substring(0, index))
    }
    for (entry in mCamera.filterSceneModes(linkedMapOf(*items.toTypedArray()))) {
      binding.tabScenes.addTab(binding.tabScenes.newTab().setText(entry.value).setTag(entry.key), false)
    }
    // 默认选中 0
    binding.tabScenes.selectTab(binding.tabScenes.getTabAt(0))
  }

  private fun switchCamera(view: View?) {
    val cameraId = if (view == null) {
      CameraX.getCameraId(this)
    } else {
      if (mCamera.getId() == CameraV1.ID_FRONT) {
        CameraV1.ID_BACK
      } else {
        CameraV1.ID_FRONT
      }
    }
    mCamera.open(cameraId)
    binding.faceView.clearFaces()
    view?.let {
      it.animate()
        .rotation(it.rotation + 180)
        .setDuration(500L)
        .start()
      with(binding.surfaceView) {
        animate().alpha(0.8f)
          .setDuration(500L)
          .setListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator) {
              // 设置模糊背景
            }

            override fun onAnimationEnd(animation: Animator) {
              alpha = 1f
            }
          })
          .start()
      }
    }
  }

  private fun tryStartPreview() {
    if (!hasPermission(Manifest.permission.CAMERA)) {
      askPermission(arrayOf(Manifest.permission.CAMERA))
      return
    }
    switchCamera(null)
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