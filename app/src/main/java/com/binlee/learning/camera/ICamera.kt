package com.binlee.learning.camera

import android.content.Context

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

  fun startPreview(surface: Any/*SurfaceView or TextureView*/)

  fun stopPreview()

  fun release()

  fun setFocusArea(rawX: Float, rawY: Float)
}