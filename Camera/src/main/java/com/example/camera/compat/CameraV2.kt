package com.example.camera.compat

import android.content.Context
import android.hardware.camera2.CameraDevice
import android.view.SurfaceHolder

class CameraV2 : AbsCamera() {

    private var mContext: Context? = null
    private var mCamera: CameraDevice? = null

    override fun init(context: Context) {
        mContext = context.applicationContext
    }

    override fun getId(): Int {
        // mCamera.id
        return 0
    }

    override fun getOrientation(): Int {
        return 0
    }

    override fun open(id: Int) {
    }

    override fun startPreview(obj: Any) {
    }

    override fun stopPreview() {
    }

    override fun takePhoto(callback: ICamera.TakePhotoCallback) {
    }

    override fun release() {
    }

    override fun autoFocus(callback: ICamera.AutoFocusCallback?): Boolean {
        return false
    }

    override fun setFocusArea(rawX: Float, rawY: Float) {
    }

    override fun setFlashMode(chooser: ICamera.FlashModeChooser) {
    }
}