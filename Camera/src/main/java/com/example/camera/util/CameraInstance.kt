package com.example.camera.util

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.Camera
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.widget.ImageView
import com.example.camera.R
import java.io.IOException
import java.util.*

/**
 * Created by renlei
 * DATE: 15-11-5
 * Time: 下午4:57
 * Email: lei.ren@renren-inc.com
 */
class CameraInstance {

    var camera: Camera? = null
        private set
    private var isPreview = false
    var cameraId = -1 //0表示后置，1表示前置
        private set
    val cameraInfo = Camera.CameraInfo()

    /**
     * 打开相机
     *
     * @param cameraId
     */
    fun doOpenCamera(cameraId: Int) {
        Log.d(TAG, "open camera$cameraId")
        try {
            this.cameraId = cameraId
            camera = Camera.open(cameraId)
            Camera.getCameraInfo(cameraId, cameraInfo) ///这里的mCamerainfo必须是new出来的，不能是个null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 开启预览
     *
     * @param holder
     */
    fun doStartPreview(holder: SurfaceHolder?) {
        Log.d(TAG, "doStartPreview")
        if (isPreview) {
            camera!!.stopPreview()
        }
        if (camera != null) {
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
            //            parameters.setPictureSize(parameters.getPreviewSize().width,parameters.getPictureSize().height);
            //设置的这两个size必须时支持的size大小，否则时不可以的，会出现setparameters错误
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
                camera!!.setPreviewDisplay(holder)
                camera!!.startPreview()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            isPreview = true
        }
    }

    /**
     * 结束预览
     */
    fun doStopPreview() {
        if (isPreview) {
            isPreview = false
            camera!!.stopPreview()
            camera!!.release()
            camera = null
        }
    }

    /**
     * 拍照
     */
    fun doTakePic() {
        if (isPreview && camera != null) {
            camera!!.takePicture(ShutCallBackImpl(), null, PicCallBacKImpl())
        }
    }

    /**
     * 拍照时的动作
     * 默认会有咔嚓一声
     */
    private class ShutCallBackImpl : Camera.ShutterCallback {
        override fun onShutter() {}
    }

    /**
     * 拍照后的最主要的返回
     */
    private inner class PicCallBacKImpl : Camera.PictureCallback {
        override fun onPictureTaken(data: ByteArray, camera: Camera) {
            isPreview = false
            Thread {
                try {
                    ImageUtil.saveImage(data, "/sdcard/preview.jpg")
                } catch (e: IOException) {
                }
            }.start()
            //重新开启预览 ，不然不能继续拍照
            camera.startPreview()
            isPreview = true
        }
    }

    /**
     * 打印支持的previewSizes
     *
     * @param params
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
     *
     * @param params
     */
    private fun printSupportPictureSize(params: Camera.Parameters) {
        val pictureSizes = params.supportedPictureSizes
        for (i in pictureSizes.indices) {
            val size = pictureSizes[i]
            Log.i(
                TAG, "pictureSizes:width = " + size.width
                        + " height = " + size.height
            )
        }
    }

    /**
     * 点击聚焦
     *
     * @param autoFocusCallback
     * @return
     */
    fun autoFocus(autoFocusCallback: Camera.AutoFocusCallback?): Boolean {
        Log.d(TAG, "autoFouce")
        val parameters = camera!!.parameters
        val supportMode = parameters.supportedFocusModes
        if (supportMode.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            val focusMode = parameters.focusMode
            if (Camera.Parameters.FOCUS_MODE_AUTO != focusMode) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                camera!!.parameters = parameters
            }
            if (autoFocusCallback != null) {
                camera!!.autoFocus(autoFocusCallback)
            }
            return true
        }
        return false
    }

    /**
     * 设置聚焦的区域
     *
     * @param mContext
     * @param event
     */
    fun setFocusArea(mContext: Context, event: MotionEvent) {
        if (!isSupportFocusArea || camera == null) return
        val parameters = camera!!.parameters
        var ax = (2000f * event.rawX / mContext.resources.displayMetrics.widthPixels - 1000).toInt()
        var ay =
            (2000f * event.rawY / mContext.resources.displayMetrics.heightPixels - 1000).toInt()
        //        Log.d("renlei",parameters.getMaxNumFocusAreas()+"");
        val rawx = event.rawX.toInt()
        val rawy = event.rawY.toInt()
        Log.d(
            TAG, "widthpix" + mContext.resources.displayMetrics.widthPixels + "heightpix"
                    + mContext.resources.displayMetrics.heightPixels
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

    /**
     * 设置闪光灯的模式
     *
     * @param imageView
     */
    fun setFlashMode(imageView: ImageView?) {
        val parameters = camera!!.parameters
        val flashMode = parameters.flashMode
        Log.d(TAG, "setFlashMode  $flashMode")
        when (flashMode) {
            Camera.Parameters.FLASH_MODE_OFF -> {
                imageView!!.setImageResource(R.drawable.camera_setting_flash_on_normal)
                parameters.flashMode = Camera.Parameters.FLASH_MODE_ON
            }
            Camera.Parameters.FLASH_MODE_ON -> {
                imageView!!.setImageResource(R.drawable.camera_setting_flash_auto_normal)
                parameters.flashMode = Camera.Parameters.FLASH_MODE_AUTO
            }
            Camera.Parameters.FLASH_MODE_AUTO -> {
                parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
                imageView!!.setImageResource(R.drawable.camera_setting_flash_off_normal)
            }
            else -> {
                imageView!!.setImageResource(R.drawable.camera_setting_flash_off_normal)
                parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
            }
        }
        camera!!.parameters = parameters
    }

    /**
     * 打印支持的聚焦模式
     *
     * @param params
     */
    private fun printSupportFocusMode(params: Camera.Parameters) {
        val focusModes = params.supportedFocusModes
        for (mode in focusModes) {
            Log.i(TAG, "focusModes--$mode")
        }
    }

    val cameraParameters: Camera.Parameters?
        get() = if (camera != null) camera!!.parameters else null

    companion object {
        private val sCamera = CameraInstance()
        const val PREVIEW_HAS_STARTED = 110
        const val RECEIVE_FACE_MSG = 111
        private const val TAG = "CameraInstance"

        fun get(): CameraInstance {
            return sCamera
        }

        /**
         * 是否符合设置对焦区域的SDK版本
         *
         * @return
         */
        val isSupportFocusArea: Boolean
            get() = Build.VERSION.SDK_INT >= 14
    }
}