package com.binlee.util

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.hardware.Camera
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.widget.ImageView
import com.binlee.learning.R
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.ArrayList

/**
 * Created on 18-2-24.
 *
 * @author leebin
 * @version 1.0
 * @description
 */
class CameraUtils {

  private var mIsPreview = false
  var mCamera: Camera? = null
    private set
  private val cameraInfo = Camera.CameraInfo()
  private var cameraId = Camera.CameraInfo.CAMERA_FACING_BACK

  private val cameraParameters: Camera.Parameters?
    get() = if (mCamera != null) {
      mCamera!!.parameters
    } else null

  /**
   * open mCamera
   *
   * @param cameraId see [android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK] and
   * [android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT]
   */
  fun openCamera(cameraId: Int) {
    this.cameraId = cameraId
    try {
      mCamera = Camera.open(cameraId)
      Camera.getCameraInfo(cameraId, cameraInfo)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  /**
   * stop preview
   */
  fun stopPreview() {
    if (mIsPreview) {
      mIsPreview = false
      if (mCamera != null) {
        mCamera!!.stopPreview()
        mCamera!!.release()
        mCamera = null
      }
    }
  }

  /**
   * start preview
   *
   * @param holder [SurfaceHolder] object
   */
  fun startPreview(holder: SurfaceHolder) {
    if (mIsPreview) {
      mCamera!!.stopPreview()
    }
    if (mCamera != null) {
      val params = mCamera!!.parameters
      params.pictureFormat = ImageFormat.JPEG
      // 解决相机预览时旋转问题
      mCamera!!.setDisplayOrientation(90)
      if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
        params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        params.flashMode = Camera.Parameters.FLASH_MODE_OFF
      }
      val supportedPreviewSizes = params.supportedPreviewSizes[0]
      params.setPreviewSize(supportedPreviewSizes.width, supportedPreviewSizes.height)

      val supportedPictureSizes = params.supportedPictureSizes[0]
      params.setPictureSize(supportedPictureSizes.width, supportedPictureSizes.height)
      // 以下两行是为了解决拍照后照片被旋转问题
      // params.set("orientation", "portrait") // 可选
      // params.set("rotation", 90) // 必须 推荐使用 params.setRotation(90) 进行设置
      params.setRotation(90)
      mCamera!!.parameters = params

      try {
        mCamera!!.setPreviewDisplay(holder)
        mCamera!!.startPreview()
      } catch (e: IOException) {
        e.printStackTrace()
      }

      mIsPreview = true
    }
  }

  /**
   * take photos
   */
  fun takePicture() {
    if (mIsPreview && mCamera != null) {
      mCamera!!.takePicture(ShutterCallbackImpl(), null, PictureCallbackImpl())
    }
  }

  private inner class PictureCallbackImpl : Camera.PictureCallback {
    override fun onPictureTaken(data: ByteArray, camera: Camera?) {
      mIsPreview = false
      saveImage(data)
      mCamera!!.startPreview()
      mIsPreview = true
    }
  }

  private inner class ShutterCallbackImpl : Camera.ShutterCallback {
    override fun onShutter() {}
  }

  /**
   * save image
   *
   * @param data byte array data of the image
   */
  private fun saveImage(data: ByteArray) {
    Thread(Runnable {
      val path = ImageUtils.saveImagePath
      Log.d("path", path)
      val file = File(path)
      var fos: FileOutputStream? = null
      try {
        fos = FileOutputStream(file, true)
        fos.write(data)
        ImageUtils.saveImage(file, data, path)
        fos.close()
      } catch (e: FileNotFoundException) {
        e.printStackTrace()
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }).start()
  }

  /**
   * auto focus
   *
   * @param autoFocusCallback [Camera.AutoFocusCallback] object
   * @return
   */
  fun autoFocus(autoFocusCallback: Camera.AutoFocusCallback?): Boolean {
    val params = mCamera!!.parameters
    val flashModes = params.supportedFlashModes
    if (flashModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
      if (Camera.Parameters.FOCUS_MODE_AUTO != params.focusMode) {
        params.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
        mCamera!!.parameters = params
      }
      if (autoFocusCallback != null) {
        mCamera!!.autoFocus(autoFocusCallback)
      }
      return true
    }
    return false
  }

  /**
   * set click to focus area
   *
   * @param context [Context] object
   * @param event   [MotionEvent] object
   */
  fun setFocusArea(context: Context, event: MotionEvent) {
    if (mCamera == null) {
      return
    }
    var ax = (2000f * event.rawX / context.resources.displayMetrics.widthPixels - 1000).toInt()
    var ay = (2000f * event.rawY / context.resources.displayMetrics.heightPixels - 1000).toInt()
    // 防止超出1000 ，-1000的范围
    if (ay > 900) {
      ay = 900
    } else if (ay < -900) {
      ay = -900
    }
    if (ax < -900) {
      ax = -900
    } else if (ax > 900) {
      ax = 900
    }
    val rect = Rect(ax - 100, ay - 100, ax + 100, ay + 100)
    val area = Camera.Area(rect, 1000)
    val focusAreas = ArrayList<Camera.Area>()
    focusAreas.add(area)
    val params = mCamera!!.parameters
    params.focusAreas = focusAreas
    params.meteringAreas = focusAreas
    mCamera!!.parameters = params
  }

  /**
   * set flash mode
   *
   * @param target the [ImageView] clicked to switch flash mode
   */
  fun setFlashMode(target: ImageView) {
    val params = cameraParameters
    val flashMode = params!!.flashMode
    if (flashMode != null) {
      when (flashMode) {
        Camera.Parameters.FLASH_MODE_OFF -> {
          target.setImageResource(R.mipmap.camera_setting_flash_on_normal)
          params.flashMode = Camera.Parameters.FLASH_MODE_ON
        }
        Camera.Parameters.FLASH_MODE_ON -> {
          target.setImageResource(R.mipmap.camera_setting_flash_auto_normal)
          params.flashMode = Camera.Parameters.FLASH_MODE_AUTO
        }
        Camera.Parameters.FLASH_MODE_AUTO -> {
          target.setImageResource(R.mipmap.camera_setting_flash_off_normal)
          params.flashMode = Camera.Parameters.FLASH_MODE_OFF
        }
        else -> {
          target.setImageResource(R.mipmap.camera_setting_flash_off_normal)
          params.flashMode = Camera.Parameters.FLASH_MODE_OFF
        }
      }
      mCamera!!.parameters = params
    }
  }

  companion object {

    private val INSTANCE = CameraUtils()
    const val MSG_PREVIEW_STARTED = 1000
    const val MSG_SCAN_FACE = 1001

    val instance: CameraUtils
      get() = synchronized(INSTANCE) {
        return INSTANCE
      }
  }
}
