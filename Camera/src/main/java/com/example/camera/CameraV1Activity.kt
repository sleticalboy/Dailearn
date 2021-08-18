package com.example.camera

import android.graphics.SurfaceTexture
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.widget.CompoundButton

class CameraV1Activity : CameraBase(), SurfaceHolder.Callback, TextureView.SurfaceTextureListener {

    override fun initView() {
        val listener = CompoundButton.OnCheckedChangeListener { cb, isChecked ->
            if (cb == mBinding!!.cbUseSurface && isChecked) {
                mBinding!!.cbUseTexture.isChecked = false
                updatePreviewView(getSurfaceView(), restart = true)
            } else if (cb == mBinding!!.cbUseTexture && isChecked) {
                mBinding!!.cbUseSurface.isChecked = false
                updatePreviewView(getSurfaceView(), restart = true)
            }
        }
        mBinding!!.cbUseTexture.setOnCheckedChangeListener(listener)
        mBinding!!.cbUseSurface.setOnCheckedChangeListener(listener)
        // 默认使用 SurfaceView 显示
        mBinding!!.cbUseSurface.isChecked = true
        mBinding!!.cbUseSurface.visibility = View.VISIBLE
        mBinding!!.cbUseTexture.visibility = View.VISIBLE
        super.initView()
    }

    override fun getSurfaceView(): View {
        if (mBinding!!.cbUseSurface.isChecked) {
            val view = SurfaceView(this)
            view.holder.addCallback(this)
            return view
        }
        val view = TextureView(this)
        view.surfaceTextureListener = this
        return view
    }

    // surface holder callback start
    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(logTag, "surfaceCreated() called with: holder = $holder")
        openCamera()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(
            logTag, "surfaceChanged() format = $format, width = $width, height = $height"
        )
        startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(logTag, "surfaceDestroyed() called with: holder = $holder")
        stopPreview()
    }

    // surface holder callback end

    // texture listener start
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        Log.d(logTag, "onSurfaceTextureAvailable() width = $width, height = $height")
        openCamera()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        Log.d(logTag, "onSurfaceTextureSizeChanged() width = $width, height = $height")
        startPreview()
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        Log.d(logTag, "onSurfaceTextureDestroyed() called ")
        stopPreview()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        Log.d(logTag, "onSurfaceTextureUpdated() called ")
    }
    // texture listener start
}