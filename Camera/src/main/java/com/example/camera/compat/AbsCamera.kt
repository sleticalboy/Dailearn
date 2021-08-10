package com.example.camera.compat

import android.content.Context
import android.view.SurfaceHolder

abstract class AbsCamera : ICamera {

    private var mContext: Context? = null

    override fun init(context: Context) {
        mContext = context
    }

    override fun getId(): Int {
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

    override fun release() {
    }

    override fun takePhoto(callback: ICamera.TakePhotoCallback) {
    }

    override fun autoFocus(callback: ICamera.AutoFocusCallback?): Boolean {
        return false
    }

    override fun setFocusArea(rawX: Float, rawY: Float) {
    }

    override fun setFlashMode(chooser: ICamera.FlashModeChooser) {
    }
}