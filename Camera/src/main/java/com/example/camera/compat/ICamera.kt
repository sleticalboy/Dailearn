package com.example.camera.compat

import android.content.Context
import android.view.SurfaceHolder

/**
 * Created on 2021/8/9
 *
 * @author binli@faceunity.com
 */
interface ICamera {

    fun init(context: Context)

    fun getId(): Int

    fun getOrientation(): Int

    fun open(id: Int)

    fun startPreview(obj: Any/*SurfaceView or TextureView*/)

    fun stopPreview()

    fun release()

    fun takePhoto(callback: TakePhotoCallback)

    fun autoFocus(callback: AutoFocusCallback?): Boolean

    fun setFocusArea(rawX: Float, rawY: Float)

    fun setFlashMode(chooser: FlashModeChooser)

    interface FlashModeChooser {
        fun choose(previousMode: String): String/*currentMode*/
    }

    interface AutoFocusCallback {
        fun onAutoFocus(success: Boolean, camera: ICamera)
    }

    interface TakePhotoCallback {
        fun onTaken(data: ByteArray, camera: ICamera)
    }
}