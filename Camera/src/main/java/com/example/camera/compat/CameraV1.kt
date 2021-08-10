package com.example.camera.compat

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Build
import android.util.Log
import android.view.SurfaceHolder
import android.view.TextureView
import com.example.camera.ImageUtil
import java.io.File
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.*

/**
 * Created on 2021/8/9
 *
 * @author binli@faceunity.com
 */
@Suppress("Deprecation")
class CameraV1 : AbsCamera() {

    private var camera: Camera? = null
    private var isPreview = false
    private var orientation = 0

    // 0表示后置，1表示前置
    private var cameraId = -1
    private val cameraInfo = Camera.CameraInfo()
    private var mContext: Context? = null

    override fun init(context: Context) {
        mContext = context.applicationContext
        val num = Camera.getNumberOfCameras()
        if (num <= 0) throw IllegalStateException("No camera available!")
        Log.d(TAG, "init() num: $num")
        for (i in 0 until num) {
            // 获取相机信息，cameraInfo 为入参，信息会写入到其中
            Camera.getCameraInfo(i, cameraInfo)
            Log.d(
                TAG,
                "init() id: ${cameraInfo.facing} orientation: ${cameraInfo.orientation} sound: ${cameraInfo.canDisableShutterSound}"
            )
            cameraId = cameraInfo.facing
            orientation = cameraInfo.orientation
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ||
                cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK
            ) {
                continue
            } else {
                cameraId = -1
                orientation = 0
            }
        }
    }

    override fun getId(): Int {
        return cameraId
    }

    override fun getOrientation(): Int {
        return orientation
    }

    override fun open(id: Int) {
        cameraId = id
        try {
            Camera.getCameraInfo(id, cameraInfo)
            camera = Camera.open(id)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    override fun startPreview(obj: Any) {
        Log.d(TAG, "doStartPreview() $camera")
        if (isPreview) camera!!.stopPreview()
        val parameters = camera!!.parameters
        parameters.pictureFormat = PixelFormat.JPEG //设置照片拍摄后的保存格式
        camera!!.setDisplayOrientation(90) //否则方向会有问题
        //前置与后置的不一样，这里暂时只设置前置的，后置的可以相应的去设置
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
        }
        printSupportPreviewSize(parameters)
        printSupportPictureSize(parameters)
        printSupportFocusMode(parameters)
        // parameters.setPictureSize(parameters.getPreviewSize().width,parameters.getPictureSize().height);
        // 设置的这两个size必须时支持的size大小，否则时不可以的，会出现setparameters错误
        parameters.setPreviewSize(
            parameters.supportedPreviewSizes[0].width,
            parameters.supportedPreviewSizes[0].height
        )
        parameters.setPictureSize(
            parameters.supportedPictureSizes[0].width,
            parameters.supportedPictureSizes[0].height
        )
        camera!!.parameters = parameters
        val mParams = camera!!.parameters
        Log.i(
            TAG, "最终设置:PreviewSize--With = " + mParams.previewSize.width
                    + "Height = " + mParams.previewSize.height
        )
        Log.i(
            TAG, "最终设置:PictureSize--With = " + mParams.pictureSize.width
                    + "Height = " + mParams.pictureSize.height
        )
        try {
            if (obj is SurfaceHolder) {
                camera!!.setPreviewDisplay(obj)
            } else if (obj is SurfaceTexture) {
                camera!!.setPreviewTexture(obj)
            } else {
                throw IllegalArgumentException("wrong obj: $obj")
            }
            // 设置预览回调
            camera!!.setPreviewCallbackWithBuffer { data, camera ->
                camera.addCallbackBuffer(data)
                // 拿到当前相机的数据流，可直接进行推流或者按照一定的帧率进行推流
                // Log.d(TAG, "onPreviewFrame() called with: data = ${data.size},")
            }
            val buffer = Array(3) {
                // ByteArray(4/*(width * height * 像素数) / 8*/)
                ByteArray(
                    (mParams.previewSize.width * mParams.previewSize.height * ImageFormat.getBitsPerPixel(
                        ImageFormat.NV21
                    )) / 8
                )
            }
            for (buf in buffer) {
                camera!!.addCallbackBuffer(buf)
            }
            camera!!.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        isPreview = true
    }

    override fun stopPreview() {
        if (isPreview) {
            isPreview = false
            camera!!.stopPreview()
            camera!!.release()
        }
    }

    override fun takePhoto(callback: ICamera.TakePhotoCallback) {
        if (isPreview && camera != null) {
            camera!!.takePicture({ /*拍照时的动作默认会有咔嚓一声*/ }, null, { data, camera ->
                // 拍照后的最主要的返回
                isPreview = false
                val path = "/sdcard/" + System.currentTimeMillis() + "_preview.jpg"
                Thread {
                    try {
                        ImageUtil.saveImage(data, path)
                        if (File(path).exists()) {
                            Log.d(TAG, "onPictureTaken() take success: $path")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }.start()
                // 重新开启预览 ，不然不能继续拍照
                camera.startPreview()
                isPreview = true
            }, )
        }
    }

    override fun release() {
    }

    override fun autoFocus(callback: ICamera.AutoFocusCallback?): Boolean {
        val parameters = camera!!.parameters
        val supportMode = parameters.supportedFocusModes
        if (supportMode.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            val focusMode = parameters.focusMode
            if (Camera.Parameters.FOCUS_MODE_AUTO != focusMode) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                camera!!.parameters = parameters
            }
            camera!!.autoFocus { success, _ ->
                callback?.onAutoFocus(success, this)
            }
            return true
        }
        return false
    }

    override fun setFocusArea(rawX: Float, rawY: Float) {
        if (!isSupportFocusArea || camera == null) return
        val parameters = camera!!.parameters
        var ax = (2000f * rawX / mContext!!.resources.displayMetrics.widthPixels - 1000).toInt()
        var ay =
            (2000f * rawY / mContext!!.resources.displayMetrics.heightPixels - 1000).toInt()
        val rawx = rawX.toInt()
        val rawy = rawY.toInt()
        Log.d(
            TAG, "width pix" + mContext!!.resources.displayMetrics.widthPixels + "height pix"
                    + mContext!!.resources.displayMetrics.heightPixels
        )
        Log.d(TAG, "rawx" + rawx + "rawy" + rawy)
        //防止超出1000 ，-1000的范围
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
        Log.d(TAG, "ax" + ax + "ay" + ay)
        val area = Camera.Area(
            Rect(ax - 100, ay - 100, ax + 100, ay + 100), 1000
        )
        val areas: MutableList<Camera.Area> = ArrayList()
        areas.add(area)
        parameters.focusAreas = areas
        parameters.meteringAreas = areas
        camera!!.parameters = parameters
    }

    override fun setFlashMode(chooser: ICamera.FlashModeChooser) {
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) return
        val parameters = camera!!.parameters
        val prev = parameters.flashMode
        val current = chooser.choose(parameters.flashMode)
        parameters.flashMode = current
        camera!!.parameters = parameters
        Log.d(TAG, "setFlashMode() prev: $prev, current: $current")
    }

    /**
     * 打印支持的聚焦模式
     */
    private fun printSupportFocusMode(params: Camera.Parameters) {
        val focusModes = params.supportedFocusModes
        for (mode in focusModes) {
            Log.i(TAG, "focusModes--$mode")
        }
    }

    /**
     * 打印支持的previewSizes
     */
    private fun printSupportPreviewSize(params: Camera.Parameters) {
        val previewSizes = params.supportedPreviewSizes
        for (i in previewSizes.indices) {
            val size = previewSizes[i]
            Log.i(TAG, "previewSizes:width = " + size.width + " height = " + size.height)
        }
    }

    /**
     * 打印支持的pictureSizes
     */
    private fun printSupportPictureSize(params: Camera.Parameters) {
        val pictureSizes = params.supportedPictureSizes
        for (i in pictureSizes.indices) {
            val size = pictureSizes[i]
            Log.i(TAG, "pictureSizes:width = " + size.width + " height = " + size.height)
        }
    }

    companion object {
        private const val TAG = "CameraV1"

        /**
         * 是否符合设置对焦区域的SDK版本
         */
        val isSupportFocusArea: Boolean get() = Build.VERSION.SDK_INT >= 14
    }
}