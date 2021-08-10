package com.example.camera

import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout

class CameraV1Activity : CameraBase() {

    private var mSurfaceView: SurfaceView? = null

    override fun getSurfaceView(): View {
        if (mSurfaceView != null) return mSurfaceView!!
        mSurfaceView = SurfaceView(this)
        mSurfaceView!!.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.d(logTag, "surfaceCreated() called with: holder = $holder")
                openCamera()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                Log.d(
                    logTag,
                    "surfaceChanged() called with: format = $format, width = $width, height = $height"
                )
                startPreview()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Log.d(logTag, "surfaceDestroyed() called with: holder = $holder")
                stopPreview()
            }
        })
        return mSurfaceView!!
    }
}