package com.example.camera

import android.content.Context
import android.util.Log
import android.view.SurfaceHolder
import com.example.camera.compat.CameraV1
import com.example.camera.compat.CameraV2
import com.example.camera.compat.ICamera

/**
 * Created by renlei
 * DATE: 15-11-5
 * Time: 下午4:57
 * Email: lei.ren@renren-inc.com
 */
class CameraCompat private constructor() {

    // 整体流程：
    // 1、获取相机信息：id、orientation、
    //   Camera.getNumberOfCameras() 拿到所有可用相机 id，遍历并调用以下方法获取 info
    //   Camera.getCameraInfo(id, CameraInfo()/*入参*，相机信息会包含在其中/)
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

    // 推流和拉流
    // 推流指直播端
    //   视频采集：yuv 格式
    //   音频采集：pcm 格式
    //   编码：h.264 格式
    //   rtmp：push 到服务端
    // 拉流指观看直播的客户端
    //   通过指定的直播 url pull 到客户端
    //   解码
    //   播放

    private var camera: ICamera
    private var mUseCamera1 = true

    init {
        camera = if (mUseCamera1) CameraV1() else CameraV2()
        Log.d(TAG, "init() called $camera")
    }

    fun setUseCameraV1(useCamera1: Boolean) {
        Log.d(TAG, "setUseCameraV1() called with: useCamera1 = $useCamera1")
        mUseCamera1 = useCamera1
    }

    fun init(context: Context) {
        Log.d(TAG, "init() called with: context = $context")
        camera.init(context)
    }

    fun open() {
        open(camera.getId())
    }

    /**
     * 打开相机
     *
     * @param id
     */
    fun open(id: Int) {
        Log.d(TAG, "open camera with id: $id")
        camera.open(id)
    }

    /**
     * 开启预览
     */
    fun startPreview(obj: Any?) {
        Log.d(TAG, "startPreview() called with: obj = $obj")
        if (obj == null) return
        camera.startPreview(obj)
    }

    /**
     * 结束预览
     */
    fun stopPreview() {
        Log.d(TAG, "stopPreview() called")
        camera.stopPreview()
    }

    /**
     * 拍照
     */
    fun doTakePic(callback: ICamera.TakePhotoCallback) {
        Log.d(TAG, "doTakePic() called with: callback = $callback")
        camera.takePhoto(callback)
    }

    /**
     * 点击聚焦
     */
    fun autoFocus(callback: ICamera.AutoFocusCallback?): Boolean {
        Log.d(TAG, "autoFocus() called with: callback = $callback")
        return camera.autoFocus(callback)
    }

    /**
     * 设置聚焦的区域
     */
    fun setFocusArea(rawX: Float, rawY: Float) {
        Log.d(TAG, "setFocusArea() called with: rawX = $rawX, rawY = $rawY")
        camera.setFocusArea(rawX, rawY)
    }

    /**
     * 设置闪光灯的模式
     */
    fun setFlashMode(chooser: ICamera.FlashModeChooser) {
        Log.d(TAG, "setFlashMode() called with: chooser = $chooser")
        camera.setFlashMode(chooser)
    }

    fun getId(): Int = camera.getId()

    companion object {
        private val sCamera = CameraCompat()
        private const val TAG = "CameraCompat"

        fun get(): CameraCompat {
            return sCamera
        }
    }
}