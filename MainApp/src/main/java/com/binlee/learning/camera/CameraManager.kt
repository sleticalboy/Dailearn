package com.binlee.learning.camera

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.ArrayList

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
  private val mHandler = Handler(Looper.getMainLooper())

  /**
   * 开启预览
   *
   * @param surface
   */
  fun startPreview(surface: SurfaceTexture?) = try {
    if (mCamera == null) openCamera()
    mCamera!!.setPreviewTexture(surface)
    mCamera!!.startPreview()
  } catch (e: IOException) {
    e.printStackTrace()
  }

  /**
   * 打开相机
   */
  private fun openCamera() {
    val cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT
    mCamera = Camera.open(cameraId)
    if (mCamera == null) return
    mCamera!!.setDisplayOrientation(90)
    val parameters = mCamera!!.parameters
    parameters.pictureFormat = ImageFormat.JPEG
    val pictureSize = parameters.supportedPictureSizes[0]
    parameters.setPictureSize(pictureSize.width, pictureSize.height)
    val previewSize = parameters.supportedPreviewSizes[0]
    parameters.setPreviewSize(previewSize.width, previewSize.height)
    parameters.setRotation(270)
    mCamera!!.parameters = parameters
  }

  /**
   * 关闭预览
   */
  fun stopPreview() {
    mCamera!!.stopPreview()
    mCamera!!.release()
    mCamera = null
  }

  /**
   * 拍照
   *
   * @param dir
   * @param callback
   */
  fun takePicture(dir: File?, callback: OnPictureTakenCallback?) {
    mCamera!!.takePicture({}, null, { data: ByteArray, camera: Camera ->
      onPictureTaken(dir, data, callback)
      camera.startPreview()
    })
  }

  private fun onPictureTaken(dir: File?, data: ByteArray, callback: OnPictureTakenCallback?) {
    if (callback == null || dir == null) {
      throw RuntimeException("OnPictureTakenCallback and dir can not be null")
    }
    Thread {
      val file = File(dir, System.currentTimeMillis().toString() + ".jpg")
      val bos: BufferedOutputStream
      try {
        bos = BufferedOutputStream(FileOutputStream(file, true))
        bos.write(data)
        bos.flush()
        bos.close()
        if (file.exists() && file.length() > 0) {
          if (DEBUG) {
            Log.d(TAG, "take picture success, " + file.path)
          }
          callback.onSuccess(file)
        }
      } catch (e: Exception) {
        callback.onFailure(e)
      }
    }.start()
  }

  fun autoFocus(
    context: Context, focusView: ViewGroup,
    focusViewSize: Size, event: MotionEvent
  ) {
    mCamera!!.autoFocus { success: Boolean, _: Camera? ->
      if (success) {
        // 1, 设置聚焦区域
        setFocusArea(context, event)
        // 2, 显示聚焦图标
        showFocusIcon(focusView, event, focusViewSize)
        Log.e(TAG, "onAutoFocus: 聚焦成功")
      } else {
        Log.e(TAG, "onAutoFocus: 聚焦失败")
      }
    }
  }

  /**
   * 设置聚焦区域
   */
  private fun setFocusArea(context: Context, event: MotionEvent) {
    if (mCamera == null) {
      return
    }
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
    val area = Camera.Area(rect, 1000)
    val focusAreas: MutableList<Camera.Area> = ArrayList()
    focusAreas.add(area)
    val parameters = mCamera!!.parameters
    parameters.focusAreas = focusAreas
    parameters.meteringAreas = focusAreas
    mCamera!!.parameters = parameters
  }

  /**
   * 显示聚焦图标
   */
  private fun showFocusIcon(focusView: ViewGroup, event: MotionEvent, size: Size) {
    val x = event.x.toInt()
    val y = event.y.toInt()
    Log.d(TAG, "x:$x")
    Log.d(TAG, "y:$y")
    val params = focusView.layoutParams as FrameLayout.LayoutParams
    params.leftMargin = (x - size.getWidth() + 0.5).toInt()
    params.topMargin = (y - size.getHeight() + 0.5).toInt()
    focusView.layoutParams = params
    focusView.visibility = View.VISIBLE
    mHandler.postDelayed({ focusView.visibility = View.GONE }, 100)
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

  class Size(private val mWidth: Int, private val mHeight: Int) {

    fun getWidth(): Int = mWidth

    fun getHeight(): Int = mHeight
  }

  open class SimpleSurfaceTextureListener : SurfaceTextureListener {

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
      // CameraManager.getInstance().startPreview(surface);
      if (DEBUG) {
        Log.d(TAG, "preview start")
      }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
      getInstance().stopPreview()
      if (DEBUG) {
        Log.d(TAG, "preview stop")
      }
      return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
  }

  companion object {
    private const val TAG = "CameraManager"
    private val DEBUG = Log.isLoggable(TAG, Log.DEBUG)
    private val MANAGER = CameraManager()
    fun getInstance(): CameraManager {
      synchronized(MANAGER) { return MANAGER }
    }
  }

  inner class Compat(context: Context) {

    fun newInstance(context: Context): Compat = Compat(context)
  }
}