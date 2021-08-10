package com.example.camera

import android.opengl.GLSurfaceView
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraV1GlActivity : CameraBase(), GLSurfaceView.Renderer {

    private var mSurfaceView: GLSurfaceView? = null

    override fun getSurfaceView(): View {
        if (mSurfaceView != null) return mSurfaceView!!
        mSurfaceView = GLSurfaceView(this)
        // 必须在 setRenderer() 之前调用，因为此方法会先检查 mGlThread 是否已创建，若已创建则抛出
        // IllegalStateException("setRenderer has already been called for this instance.")
        mSurfaceView!!.setEGLContextClientVersion(2)
        // 必须先调用 setRenderer() 方法，因为 renderMode 是通过 mGlThread 设置下去的
        mSurfaceView!!.setRenderer(this)
        mSurfaceView!!.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        // mSurfaceView!!.holder.addCallback(object : SurfaceHolder.Callback {
        //     override fun surfaceCreated(holder: SurfaceHolder) {
        //         Log.d(logTag, "surfaceCreated() called with: holder = $holder")
        //         openCamera()
        //     }
        //
        //     override fun surfaceChanged(
        //         holder: SurfaceHolder,
        //         format: Int,
        //         width: Int,
        //         height: Int
        //     ) {
        //         Log.d(
        //             logTag,
        //             "surfaceChanged() called with: holder = $holder, format = $format, width = $width, height = $height"
        //         )
        //         startPreview()
        //     }
        //
        //     override fun surfaceDestroyed(holder: SurfaceHolder) {
        //         Log.d(logTag, "surfaceDestroyed() called with: holder = $holder")
        //         stopPreview()
        //     }
        // })
        return mSurfaceView!!
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d(logTag, "onSurfaceCreated() called with: gl = $gl, config = $config")
        openCamera()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.d(logTag, "onSurfaceChanged() called with: gl = $gl, width = $width, height = $height")
        startPreview()
    }

    override fun onDrawFrame(gl: GL10?) {
        Log.d(logTag, "onDrawFrame() called with: gl = $gl")
    }
}