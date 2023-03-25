package com.binlee.learning.camera

import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.*
import android.text.TextUtils
import android.util.Log
import android.util.Size
import android.view.*
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
@Suppress("DEPRECATION")
class CameraWrapper(private val activity: Activity, private val callback: Callback?) {

  private var mCamera: Camera? = null
  private var mOpened = false
  private var mCameraId = ID_BACK
  private var mInitParams: Parameters? = null

  private val mFocusCallback = AutoFocusCallback { success, camera ->
    val params = camera.parameters
    val focusMode = params.focusMode
    val areas = if (params.focusMode == Parameters.FOCUS_MODE_FIXED) {
      dumpAreas(params.meteringAreas)
    } else {
      dumpAreas(params.focusAreas)
    }
    Log.d(TAG, "onAutoFocus() success: $success, mode: $focusMode, area: $areas")
  }

  // open/startPreview/stopPreview/close

  interface Callback {
    /**
     * 相机打开回调
     *
     * @param [preferredPreviewSize] 首选预览大小
     */
    fun onOpened(preferredPreviewSize: Size?, camera: CameraWrapper?)

    /**
     * 上一个相机关闭回调
     *
     * @param [cameraId] 相机 id
     */
    fun onLastClosed(cameraId: Int) {}

    fun onFaceDetected(faces: Array<Face>) {}
  }

  /**
   * 打开相机
   *
   * @param [cameraId] 相机 id
   */
  fun open(cameraId: Int) {
    // 同一个相机，不重复打开
    if (mOpened && cameraId == mCameraId) return

    // 已打开，先关闭
    if (mOpened) close()

    // 根据 id 打开对应相机，返回首选预览窗口大小
    val preferredPreviewSize = openCamera(cameraId)
    mOpened = mCamera != null
    mCameraId = cameraId

    callback?.onOpened(preferredPreviewSize, this)
  }

  /**
   * 开启预览
   *
   * @param surface
   */
  @Throws(IOException::class)
  fun startPreview(surface: SurfaceTexture?) {
    // 先打开，然后才能预览
    if (!mOpened) throw java.lang.RuntimeException("Missed calling open(cameraId: Int)")

    mCamera?.setPreviewTexture(surface)
    mCamera?.startPreview()
    mCamera?.autoFocus(mFocusCallback)

    // 开启人脸检测
    if (mInitParams!!.maxNumDetectedFaces > 0) {
      mCamera?.startFaceDetection()
      mCamera?.setFaceDetectionListener { faces, _ ->
        if (faces.isNotEmpty()) {
          Log.d(TAG, "onFaceDetection() ${faces.size} -> faces: ${dumpFace(faces[0])}")
        }
        // 回调检测到的人脸
        callback?.onFaceDetected(Util.convertFaces(faces))
      }
    }
  }

  private fun dumpFace(face: Camera.Face): String {
    return "{id: ${face.id}, score: ${face.score}, rect: ${face.rect}}"
  }

  /**
   * 关闭预览
   */
  fun stopPreview() {
    mCamera?.stopPreview()
    // cameraInternal?.release()
    // cameraInternal = null
  }

  /**
   * 关闭相机
   */
  fun close() {
    mCamera?.stopPreview()
    mCamera?.release()
    mCamera = null

    // 回调
    callback?.onLastClosed(mCameraId)

    mOpened = false
    mCameraId = CameraInfo.CAMERA_FACING_BACK
  }

  /**
   * 打开相机
   *
   * @param [cameraId] 相机id
   * @return [Size?] 首选预览窗口大小
   */
  private fun openCamera(cameraId: Int): Size? {
    val camera = Camera.open(mapCameraId(cameraId)) ?: return null

    camera.setDisplayOrientation(getDisplayOrientation(cameraId))

    mInitParams = camera.parameters

    val params = camera.parameters
    params.pictureFormat = ImageFormat.JPEG

    // 设置预览大小
    val size = params.preferredPreviewSizeForVideo
    Log.d(TAG, "openCamera() preview size(${size.width}, ${size.height})")
    params.setPreviewSize(size.width, size.height)

    // 设置保存图片宽高
    val sizes = params.supportedPictureSizes
    if (sizes.contains(size)) {
      params.setPictureSize(size.width, size.height)
    } else {
      params.setPictureSize(sizes[0].width, sizes[1].height)
    }
    params.setRotation(getCameraRotation(cameraId))
    return try {
      camera.parameters = params
      mCamera = camera
      dumpParameters(params, "openCamera", null)
      Size(size.height, size.width)
    } catch (tr: Exception) {
      dumpParameters(params, "openCamera", tr)
      null
    }
  }

  /**
   * 拍照
   *
   * @param dir 照片保存目录
   * @param callback 照片保存回调
   */
  fun takePicture(dir: String, callback: OnPictureTakenCallback?) {
    mCamera!!.takePicture(/* shutter = */{}, /* raw = */null, /* jpeg = */{ data: ByteArray, camera: Camera ->
      callback?.let {
        val file = File(dir, "${System.currentTimeMillis()}.jpg")
        file.writeBytes(data)
        if (file.exists() && file.length() > 0) {
          Log.d(TAG, "onPictureTaken() path: ${file.path}, size: ${file.length()}")
          it.onSuccess(file)
        }
      }
      camera.startPreview()
    })
  }

  /**
   * 自动对焦
   *
   * @param [context] 上下文
   * @param [focusView] 焦点视图
   * @param [event] 事件
   */
  fun autoFocus(context: Context, focusView: View, event: MotionEvent) {
    // 先设置聚焦区域，再调用 autoFocus() 接口

    // 显示聚焦图标
    showFocusIcon(focusView, event)
    // 设置聚焦区域
    setFocusArea(context, event)
    // 调用接口自动对焦
    mCamera?.autoFocus(mFocusCallback)
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
      val focusAreas = listOf(Area(rect, 1000))
      val params = it.parameters
      if (mInitParams!!.maxNumFocusAreas > 0
        && params.supportedFocusModes.contains(Parameters.FOCUS_MODE_AUTO)) {
        params.focusAreas = focusAreas
      }
      if (mInitParams!!.maxNumMeteringAreas > 0) {
        params.meteringAreas = focusAreas
      }

      try {
        it.parameters = params
      } catch (tr: Throwable) {
        Log.e(TAG, "setFocusArea() failed! ${params.focusMode} -> ${params.supportedFocusModes}")
      }
    }
  }

  private fun dumpParameters(params: Parameters?, where: String, tr: Throwable? = null) {
    tr?.let { Log.e(TAG, "$where() update camera params failed!", it) }

    params?.let {
      val priority = if (tr == null) Log.INFO else Log.ERROR
      val splitter = TextUtils.SimpleStringSplitter(';')
      splitter.setString(params.flatten())
      for (kv in splitter) {
        Log.println(priority, TAG, "$where() try to update camera param: $kv")
      }
    }
  }

  private fun dumpAreas(areas: List<Area>?): String {
    return if (areas == null) {
      "(0,0,0,0,0)"
    } else {
      val buffer = StringBuilder()
      for (i in areas.indices) {
        val area: Area = areas[i]
        val rect = area.rect
        buffer.append('(')
        buffer.append(rect.left)
        buffer.append(',')
        buffer.append(rect.top)
        buffer.append(',')
        buffer.append(rect.right)
        buffer.append(',')
        buffer.append(rect.bottom)
        buffer.append(',')
        buffer.append(area.weight)
        buffer.append(')')
        if (i != areas.size - 1) buffer.append(',')
      }
      buffer.toString()
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

  companion object {
    private const val TAG = "CameraWrapper"

    const val ID_FRONT = 0x10
    const val ID_BACK = 0x11

    private var cameraInfos: Array<CameraInfo?>

    init {
      val number = getNumberOfCameras()
      cameraInfos = arrayOfNulls(number)
      for (i in 0 until number) {
        cameraInfos[i] = CameraInfo()
        getCameraInfo(i, cameraInfos[i])
      }
    }

    private fun mapCameraId(rawId: Int): Int =
      if (rawId == ID_FRONT) CameraInfo.CAMERA_FACING_FRONT else CameraInfo.CAMERA_FACING_BACK

    fun getDisplayRotation(activity: Activity): Int {
      when (activity.windowManager.defaultDisplay.rotation) {
        Surface.ROTATION_0 -> return 0
        Surface.ROTATION_90 -> return 90
        Surface.ROTATION_180 -> return 180
        Surface.ROTATION_270 -> return 270
      }
      return 0
    }

    fun getDisplayOrientation(cameraId: Int): Int {
      // See android.hardware.Camera.setDisplayOrientation for
      // documentation.
      val raw = getCameraOrientation(cameraId)
      val real = if (cameraId == ID_FRONT) {
        (360 - raw) % 360 // compensate the mirror
      } else {  // back-facing
        (raw + 360) % 360
      }
      Log.d(TAG, "getDisplayOrientation() id: $cameraId -> raw: $raw -> real: $real")
      return real
    }

    private fun getCameraOrientation(cameraId: Int): Int {
      return cameraInfos[mapCameraId(cameraId)]!!.orientation
    }

    fun getCameraRotation(cameraId: Int): Int {
      val raw = getCameraOrientation(cameraId)
      val real = if (cameraId == ID_FRONT) {
        (raw + 360) % 360
      } else {  // back-facing camera
        raw % 360
      }
      Log.d(TAG, "getCameraRotation() id: $cameraId -> raw: $raw -> real: $real")
      return real
    }
  }
}