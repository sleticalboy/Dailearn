package com.binlee.learning.camera.v1

import android.app.Activity
import android.content.Intent
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.*
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.util.Size
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.VisibleForTesting
import com.binlee.learning.camera.CameraX
import com.binlee.learning.camera.Face
import com.binlee.learning.camera.errorStr
import java.io.File
import java.io.IOException
import kotlin.math.round
import kotlin.math.roundToInt

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
class CameraV1(private val activity: Activity, private val callback: Callback?) {

  /* // 相机整体流程：
  // 1、获取相机信息：id、orientation、
  //   Camera.getNumberOfCameras() 拿到所有可用相机 id，遍历并调用以下方法获取 info
  //   Camera.getCameraInfo(id, CameraInfo()/* 入参，相机信息会包含在其中 */)
  // 2、打开相机：需要 id，返回 Camera 实例
  //   Camera.open(id)
  // 3、开始预览：
  //   设置预览参数：图片格式/大小、orientation、闪关灯、对焦、fps、预览格式/大小
  //      camera1 默认预览格式为 NV21；camera2 默认预览格式为 YUV_420_888
  //   设置预览 SurfaceHolder/SurfaceTexture：显示相机画面
  //      Camera.setPreviewDisplay(SurfaceHolder)
  //      Camera.setPreviewTexture(SurfaceTexture)
  //   设置预览回调：获取相机数据流，可用于推流或其他操作(数据流是从 native 层回调到 java 层的)
  //      Camera.setPreviewCallbackWithBuffer()
  //   开始预览
  //      Camera.startPreview()
  */

  /* // 推流和拉流
  // 推流指直播端
  //   视频采集：yuv 格式 & 编码：h.264 格式
  //   音频采集：pcm 格式 & 编码：aac 格式
  //   rtmp：push 到服务端
  // 拉流指观看直播的客户端
  //   通过指定的直播 url pull 到客户端
  //   解码
  //   播放
  */

  /* // 相机参数
  // video-size=1920x1080
  // 首选预览尺寸、支持的预览尺寸、视频预览格式、fps
  // preferred-preview-size-for-video=1920x1080
  // preview-size=1920x1080
  // preview-size-values=1920x1080,1600x1200,1440x1080,1280x960,1280x720,1200x1200,1024x768,800x600,720x480,640x480,640x360,480x360,480x320,352x288,320x240,176x144,160x120
  // video-size-values=3840x2160,2592x1944,2688x1512,2048x1536,1920x1080,1600x1200,1440x1080,1280x960,1280x720,1200x1200,1024x768,800x600,720x480,640x480,640x360,480x360,480x320,352x288,320x240,176x144,160x120
  // preview-format=yuv420sp
  // preview-format-values=yuv420p,yuv420sp,
  // preview-fps-range=7000,30000
  // preview-fps-range-values=(15000,15000),(24000,24000),(7000,30000),(30000,30000)
  // preview-frame-rate=30
  // preview-frame-rate-values=15,24,30
  // 拍照格式及尺寸
  // picture-format-values=jpeg
  // picture-size-values=4032x3024,4000x3000,3840x2160,3264x2448,3200x2400,2976x2976,2592x1944,2688x1512,2048x1536,1920x1080,1600x1200,1440x1080,1280x960,1280x720,1200x1200,1024x768,800x600,720x480,640x480,640x360,480x360,480x320,352x288,320x240,176x144,160x120
  // picture-format=jpeg
  // picture-size=1920x1080
  // 缩略图相关
  // jpeg-thumbnail-width=320
  // jpeg-thumbnail-height=240
  // jpeg-thumbnail-size-values=0x0,176x144,240x144,256x144,240x160,256x154,240x240,320x240
  // jpeg-thumbnail-quality=90
  // jpeg-quality=90
  // 白平衡
  // whitebalance=auto
  // whitebalance-values=auto,incandescent,fluorescent,warm-fluorescent,daylight,cloudy-daylight,twilight,shade,
  // 特效
  // effect=none
  // effect-values=none,mono,negative,solarize,sepia,posterize,whiteboard,blackboard,aqua
  // antibanding=auto
  // antibanding-values=off,60hz,50hz,auto
  // scene-mode=auto
  // scene-mode-values=auto,landscape,snow,beach,sunset,night,portrait,sports,steadyphoto,candlelight,fireworks,party,night-portrait,theatre,action,
  // 闪光模式
  // flash-mode=off
  // flash-mode-values=off,auto,on,torch
  // 聚焦 & 聚焦区域
  // focus-mode=auto
  // focus-mode-values=infinity,auto,macro,continuous-video,continuous-picture
  // max-num-focus-areas=1
  // focus-areas=(0,0,0,0,0)
  // max-num-metering-areas=1
  // metering-areas=(0,0,0,0,0)
  // 焦距
  // focal-length=4.4589
  // horizontal-view-angle=64.6661
  // vertical-view-angle=50.7907
  // 曝光
  // exposure-compensation=0
  // max-exposure-compensation=12
  // min-exposure-compensation=-12
  // exposure-compensation-step=0.166667
  // auto-exposure-lock=false
  // auto-exposure-lock-supported=true
  // 白平衡
  // auto-whitebalance-lock=false
  // auto-whitebalance-lock-supported=true
  // 缩放
  // zoom=0
  // max-zoom=99
  // zoom-ratios=100,106,112,118,124,130,136,142,148,154,160,166,172,178,184,190,196,203,209,215,221,227,233,239,245,251,257,263,269,275,281,287,293,299,306,312,318,324,330,336,342,348,354,360,366,372,378,384,390,396,403,409,415,421,427,433,439,445,451,457,463,469,475,481,487,493,499,506,512,518,524,530,536,542,548,554,560,566,572,578,584,590,596,603,609,615,621,627,633,639,645,651,657,663,669,675,681,687,693,699
  // zoom-supported=true
  // smooth-zoom-supported=false
  // 对焦距离
  // focus-distances=Infinity,Infinity,Infinity
  // 人脸检测
  // max-num-detected-faces-hw=10
  // max-num-detected-faces-sw=0
  // video-frame-format=android-opaque
  // recording-hint=false
  // video-snapshot-supported=true
  // video-stabilization=false
  // video-stabilization-supported=true
  */

  private var mCamera: Camera? = null
  private var mOpened = false
  private var mCameraId = ID_BACK
  
  fun getId() = mCameraId

  private var mInitParams: Parameters? = null

  private var mDisplayOrientation = 0
  private var mMatrix: Matrix? = null
  private var focusAreas = listOf(Area(null, 1))
  private var meteringAreas = listOf(Area(null, 1))

  private val mFocusCallback = AutoFocusCallback { success, camera ->
    val params = camera.parameters
    val focusMode = params.focusMode
    val areas = if (params.focusMode == Parameters.FOCUS_MODE_FIXED) {
      dumpAreas(params.meteringAreas)
    } else {
      dumpAreas(params.focusAreas)
    }
    Log.w(TAG, "onAutoFocus() success: $success, mode: $focusMode, area: $areas")
  }
  @VisibleForTesting private var lastFaceId = -1
  private val mFaceCallback = FaceDetectionListener { faces, _ ->
    if (faces != null && faces.isNotEmpty() && lastFaceId != faces[0].id) {
      Log.w(TAG, "onFaceDetection() ${faces.size} -> faces: ${dumpFace(faces[0])}")
      lastFaceId = faces[0].id
    }
    // 回调检测到的人脸
    callback?.onFaceDetected(CameraX.convertFaces(faces), getDisplayOrientation(mCameraId))
  }

  // open/startPreview/stopPreview/close

  interface Callback {
    /**
     * 相机打开回调
     *
     * @param [preferredPreviewSize] 首选预览大小
     */
    fun onOpened(preferredPreviewSize: Size?, camera: CameraV1?)

    /**
     * 上一个相机关闭回调
     *
     * @param [cameraId] 相机 id
     */
    fun onLastClosed(cameraId: Int) {}

    /**
     * 人脸检测回调
     *
     * @param [faces] 检测到的人脸
     */
    fun onFaceDetected(faces: Array<Face>, displayOrientation: Int) {}

    /**
     * 拍照回调
     *
     * @param [path] 文件路径，为 null 时表示失败
     */
    fun onTakePictureDone(path: File?) {}
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
    val focusMode = mInitParams!!.focusMode
    if (focusMode == Parameters.FOCUS_MODE_AUTO) {
      mCamera?.autoFocus(mFocusCallback)
    } else {
      Log.w(TAG, "startPreview() focus modes: ${mInitParams?.supportedFocusModes} -> $focusMode")
    }

    // 开启人脸检测
    if (mInitParams!!.maxNumDetectedFaces > 0) {
      mCamera?.startFaceDetection()
      mCamera?.setFaceDetectionListener(mFaceCallback)
    }
  }

  private fun dumpFace(face: Camera.Face): String {
    return "{id: ${face.id}, score: ${face.score}, rect: ${face.rect}}"
  }

  /**
   * 关闭预览
   */
  fun stopPreview() {
    mCamera?.stopFaceDetection()
    mCamera?.stopPreview()
  }

  /**
   * 关闭相机
   */
  fun close() {
    stopPreview()
    mCamera?.release()
    mCamera = null
    mMatrix = null
    mMatrix = null

    // 回调
    callback?.onLastClosed(mCameraId)

    mOpened = false
    mCameraId = ID_BACK
  }

  /**
   * 打开相机
   *
   * @param [cameraId] 相机id
   * @return [Size?] 首选预览窗口大小
   */
  private fun openCamera(cameraId: Int): Size? {
    val camera = Camera.open(rawId(cameraId)) ?: return null
    
    camera.setErrorCallback { error, _ ->
      Log.e(TAG, "onError() error: ${camera.errorStr(error)}")
    }
    
    // 显示角度
    mDisplayOrientation = getDisplayOrientation(cameraId)
    camera.setDisplayOrientation(mDisplayOrientation)

    mInitParams = camera.parameters
    dumpParameters(mInitParams, "default", null)

    val params = camera.parameters
    params.pictureFormat = ImageFormat.JPEG

    // 选择拍照时保存的图片尺寸
    CameraX.setupPictureSize(activity, params, cameraId)
    val pictureSize = params.pictureSize
    Log.d(TAG, "openCamera() picture size(${pictureSize.height}, ${pictureSize.width})")

    Log.d(TAG, "openCamera() select preview size from: ${params.get("preview-size-values")}")
    // 根据图片尺寸选择预览尺寸
    val previewSize = params.previewSize
    val optimizedSize = CameraX.optimizePreviewSize(
      activity, params.supportedPreviewSizes,
      pictureSize.width * 1f / pictureSize.height
    )
    val size = if (!previewSize.equals(optimizedSize)) {
      params.setPreviewSize(optimizedSize.width, optimizedSize.height)
      Size(optimizedSize.height, optimizedSize.width)
    } else {
      Size(previewSize.height, previewSize.width)
    }
    Log.d(TAG, "openCamera() preview size(${size.width}, ${size.height})")
    // 旋转
    params.setRotation(getCameraRotation(cameraId))
    return try {
      camera.parameters = params
      mCamera = camera
      Size(size.width, size.height)
    } catch (tr: Exception) {
      dumpParameters(params, "openCamera()", tr)
      null
    }
  }

  /**
   * 拍照
   *
   * @param dir 照片保存目录
   */
  fun takePicture(dir: String) {
    mCamera!!.takePicture(/* shutter = */{}, /* raw = */null, /* jpeg = */{ data: ByteArray, camera: Camera ->
      // 数据放入子线程处理
      callback?.let {
        // 保存文件
        val file = File(dir, CameraX.generateFilepath(".jpg"))
        file.writeBytes(data)
        if (file.exists() && file.length() > 0) {
          Log.d(TAG, "onPictureTaken() path: ${file.path}, size: ${file.length()}")
          it.onTakePictureDone(file)
        }
        // 刷新相册
        activity.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(Uri.fromFile(file)))
      }
      // 重新开启预览 ，不然不能继续拍照
      camera.startPreview()
    })
  }

  /**
   * 自动对焦
   *
   * @param [focusView] 焦点 view
   * @param [surfaceView] 预览 view
   */
  fun autoFocus(focusView: View, surfaceView: View, x: Float, y: Float) {
    // 先设置聚焦区域，再调用 autoFocus() 接口

    val width = (focusView.context as Activity).windowManager.defaultDisplay.width / 4
    val point = PointF(round(x), round(y))
    val surfaceSpec = Point(surfaceView.width, surfaceView.height)
    val focusSpec = Point(width, width)
    Log.d(
      TAG, "autoFocus() pos: $point, surface: $surfaceSpec, focus: $focusSpec" +
        ", region: [${surfaceView.left}, ${surfaceView.top}, ${surfaceView.right}, ${surfaceView.bottom}]")
    if (mMatrix == null) {
      mMatrix = Matrix()
      val matrix = Matrix()
      CameraX.prepareMatrix(matrix, mCameraId == ID_FRONT, mDisplayOrientation, surfaceSpec)
      matrix.invert(mMatrix)
    }

    // 计算对焦区域
    focusAreas[0].rect  = CameraX.getTapArea(surfaceSpec, focusSpec, 1f, point, mMatrix)
    meteringAreas[0].rect = CameraX.getTapArea(surfaceSpec, focusSpec, 1.5f, point, mMatrix)
    Log.d(TAG, "autoFocus() areas: ${dumpAreas(focusAreas)}, ${dumpAreas(meteringAreas)}")

    // 是否需要更新参数
    var needSet = false
    val params = mCamera!!.parameters
    if (mInitParams!!.maxNumFocusAreas > 0
      && params.supportedFocusModes.contains(Parameters.FOCUS_MODE_AUTO)) {
      params.focusAreas = focusAreas
      needSet = true
    }
    if (mInitParams!!.maxNumMeteringAreas > 0) {
      params.meteringAreas = meteringAreas
      needSet = true
    }
    if (needSet) {
      // 设置聚焦区域
      try {
        mCamera!!.parameters = params
      } catch (tr: Throwable) {
        Log.e(TAG, "setFocusArea() failed! ${params.focusMode}:${params.supportedFocusModes}")
        dumpParameters(mCamera?.parameters, "autoFocus", tr)
        return
      }
      // 调用接口自动对焦
      val focusMode = mInitParams!!.focusMode
      if (focusMode == Parameters.FOCUS_MODE_AUTO) {
        mCamera!!.autoFocus(mFocusCallback)
      } else {
        Log.w(TAG, "autoFocus() focus modes: ${mInitParams?.supportedFocusModes} -> $focusMode")
        return
      }
    }

    // 更新对焦 view 位置
    val lp = focusView.layoutParams as MarginLayoutParams
    lp.width = focusSpec.x
    lp.height = focusSpec.y
    // 触点 - 宽或高的一半
    val left = CameraX.clamp(point.x - focusSpec.x / 2 + 0.5f, 0f, surfaceSpec.x - focusSpec.x + 0f)
    val top = CameraX.clamp(point.y - focusSpec.y / 2 + 0.5f, 0f, surfaceSpec.y - focusSpec.y + 0f)
    lp.setMargins(left.roundToInt(), top.roundToInt(), 0, 0)
    focusView.visibility = View.VISIBLE
    focusView.postDelayed(
      {
        lp.setMargins(0, 0, 0, 0)
        focusView.visibility = View.GONE
      },
      500L
    )
  }

  fun updateSurface(surface: SurfaceTexture) {
    try {
      mCamera?.setPreviewTexture(surface)
    } catch (_: IOException) {
    }
  }

  private fun dumpParameters(params: Parameters?, where: String, tr: Throwable? = null) {
    tr?.let { Log.e(TAG, "$where() update camera params failed!", it) }

    params?.let {
      val priority = if (tr == null) Log.INFO else Log.ERROR
      val splitter = TextUtils.SimpleStringSplitter(';')
      splitter.setString(params.flatten())
      for (kv in splitter) {
        Log.println(priority, TAG, "$where camera param: $kv")
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

  fun filterSceneModes(map: Map<String, String>): Map<String, String> {
    // 这个值获取出来可能为 null
    val supported = mInitParams!!.supportedSceneModes
    return if (supported == null) emptyMap() else map.filter { supported.contains(it.key) }
  }

  fun setSceneMode(mode: String?) {
    val params = mCamera!!.parameters
    params.sceneMode = mode
    mCamera!!.parameters = params
    Log.d(TAG, "setSceneMode() mode: $mode")
  }

  fun getInitialParams(): Parameters? {
    return mInitParams
  }

  fun setFlashMode(mode: String?) {
    val params = mCamera!!.parameters
    params.flashMode = mode
    mCamera!!.parameters = params
    Log.d(TAG, "setFlashMode() mode: $mode")
  }

  fun getFlashMode(): String {
    return mCamera!!.parameters.flashMode
  }

  fun getWhiteBalance(): String {
    return mCamera!!.parameters.whiteBalance
  }

  fun setWhiteBalance(value: String?) {
    val params = mCamera!!.parameters
    params.whiteBalance = value
    mCamera!!.parameters = params
    Log.d(TAG, "setWhiteBalance() value: $value")
  }

  companion object {
    private const val TAG = "CameraWrapper"

    const val ID_FRONT = 0x10
    const val ID_BACK = 0x11

    private val cameraInfos: Array<CameraInfo?> = Array(getNumberOfCameras()) { i: Int ->
      val info = CameraInfo()
      getCameraInfo(i, info)
      info
    }

    private fun rawId(cameraId: Int): Int =
      if (cameraId == ID_FRONT) CameraInfo.CAMERA_FACING_FRONT else CameraInfo.CAMERA_FACING_BACK

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
      // Log.d(TAG, "getDisplayOrientation() id: $cameraId -> raw: $raw -> real: $real")
      return real
    }

    private fun getCameraOrientation(cameraId: Int): Int {
      return cameraInfos[rawId(cameraId)]!!.orientation
    }

    fun getCameraRotation(cameraId: Int): Int {
      val raw = getCameraOrientation(cameraId)
      val real = if (cameraId == ID_FRONT) {
        (raw + 360) % 360
      } else {  // back-facing camera
        raw % 360
      }
      // Log.d(TAG, "getCameraRotation() id: $cameraId -> raw: $raw -> real: $real")
      return real
    }
  }
}