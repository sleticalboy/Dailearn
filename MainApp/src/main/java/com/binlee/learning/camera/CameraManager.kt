package com.binlee.learning.camera

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.Parameters
import android.text.TextUtils
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.ViewGroup
import com.binlee.learning.util.UiUtils
import java.io.File
import java.io.IOException

/**
 * Created on 18-2-27.
 *
 *
 * CameraManager
 *
 * @author leebin
 * @version 1.0
 */
class CameraManager {

  private var mCamera: Camera? = null
  private var mSize: Size? = null

  /**
   * 开启预览
   *
   * @param surface
   */
  @Throws(IOException::class)
  fun startPreview(surface: SurfaceTexture?): Size? {
    if (mCamera == null) {
      mSize = openCamera()
      if (mSize != null) {
        mCamera?.setPreviewTexture(surface)
        mCamera?.startPreview()
      }
    }
    return mSize
  }

  /**
   * 打开相机
   */
  private fun openCamera(): Size? {
    val cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT
    val camera = Camera.open(cameraId) ?: return null

    camera.setDisplayOrientation(90)
    val params = camera.parameters
    params.pictureFormat = ImageFormat.JPEG

    // 设置预览大小
    val size = params.preferredPreviewSizeForVideo
    Log.d(TAG, "openCamera() preview size(${size.width}, ${size.height})")
    params.setPreviewSize(size.width, size.height)

    val sizes = params.supportedPictureSizes
    if (sizes.contains(size)) {
      params.setPictureSize(size.width, size.height)
    } else {
      params.setPictureSize(sizes[0].width, sizes[1].height)
    }

    params.setRotation(270)
    return try {
      camera.parameters = params
      mCamera = camera
      Size(size.height, size.width)
    } catch (tr: Exception) {
      dumpParameters(params, "openCamera", tr)
      mCamera = null
      null
    }
  }

  /**
   * 关闭预览
   */
  fun stopPreview() {
    mCamera?.stopPreview()
    mCamera?.release()
    mCamera = null
  }

  /**
   * 拍照
   *
   * @param dir
   * @param callback
   */
  fun takePicture(dir: String, callback: OnPictureTakenCallback?) {
    mCamera!!.takePicture(/* shutter = */{}, /* raw = */null, /* jpeg = */{ data: ByteArray, camera: Camera ->
      onPictureTaken(dir, data, callback)
      camera.startPreview()
    })
  }

  private fun onPictureTaken(dir: String, data: ByteArray, callback: OnPictureTakenCallback?) {
    callback?.let {
      val file = File(dir, System.currentTimeMillis().toString() + ".jpg")
      file.writeBytes(data)
      if (file.exists() && file.length() > 0) {
        Log.d(TAG, "onPictureTaken() path: ${file.path}")
        it.onSuccess(file)
      }
    }
  }

  fun autoFocus(context: Context, focusView: View, event: MotionEvent) {
    mCamera?.autoFocus { success: Boolean, _: Camera? ->
      Log.d(TAG, "autoFocus() onAutoFocus() success: $success")
      if (success) {
        // 1, 设置聚焦区域
        // setFocusArea(context, event)
        // 2, 显示聚焦图标
        showFocusIcon(focusView, event)
      }
    }
  }

  /**
   * 设置聚焦区域
   */
  private fun setFocusArea(context: Context, event: MotionEvent) {
    mCamera?.let {
      var ax = (2000f * event.rawX / context.resources.displayMetrics.widthPixels - 1000).toInt()
      var ay = (2000f * event.rawY / context.resources.displayMetrics.heightPixels - 1000).toInt()
      if (ax > 900) {
        ax = 900
      } else if (ax < -900) {
        ax = -900
      }
      if (ay > 900) {
        ay = 900
      } else if (ay < -900) {
        ay = -900
      }
      val rect = Rect(ax - 100, ay - 100, ax + 100, ay + 100)
      val focusAreas = listOf(Camera.Area(rect, 1000))
      val params = it.parameters
      params.focusAreas = focusAreas
      params.meteringAreas = focusAreas

      try {
        it.parameters = params
      } catch (tr: Throwable) {
        // dumpParameters(params, "setFocusArea", tr)
      }
    }
  }

  private fun dumpParameters(params: Parameters?, where: String, tr: Throwable) {
    Log.e(TAG, "$where() update camera params failed!", tr)
    params?.let {
      val splitter = TextUtils.SimpleStringSplitter(';')
      splitter.setString(params.flatten())
      for (kv in splitter) {
        Log.e(TAG, "$where() try to update camera param: $kv")
      }
    }
  }

  /**
   * 显示聚焦图标
   */
  private fun showFocusIcon(focusView: View, event: MotionEvent) {
    val x = event.x
    val y = event.y

    val width = 240
    val params = focusView.layoutParams as ViewGroup.MarginLayoutParams
    params.width = width
    params.height = width
    // 触点 - 宽或高的一半
    params.leftMargin = (x - width / 2 + 0.5f).toInt()
    // margin top 要多处理一个状态栏高度
    val sh = UiUtils.getStatusBarHeight()
    params.topMargin = (y - sh - width / 2 + 0.5f).toInt()
    focusView.visibility = View.VISIBLE
    focusView.postDelayed({ hideFocusIcon(focusView) }, 500L)
  }

  private fun hideFocusIcon(focusView: View) {
    val params = focusView.layoutParams as ViewGroup.MarginLayoutParams
    params.topMargin = 0
    params.leftMargin = 0
    focusView.visibility = View.GONE
  }

  /**
   * 拍照完成的回调
   */
  interface OnPictureTakenCallback {
    /**
     * 成功时时调用
     *
     * @param picture
     */
    fun onSuccess(picture: File)

    /**
     * 失败时调用
     *
     * @param e
     */
    fun onFailure(e: Throwable?)
  }

  abstract class SimpleSurfaceTextureListener : SurfaceTextureListener {

    abstract override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int)

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
      get().stopPreview()
      Log.d(TAG, "preview stop")
      return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
  }

  companion object {
    private const val TAG = "CameraManager"
    private val MANAGER = CameraManager()

    @JvmStatic fun get(): CameraManager = MANAGER
  }
}