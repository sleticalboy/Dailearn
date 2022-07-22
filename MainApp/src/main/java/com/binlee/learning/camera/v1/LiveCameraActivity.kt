package com.sleticalboy.learning.camera.v1

import android.graphics.SurfaceTexture
import android.util.Log
import android.view.View
import com.sleticalboy.learning.base.BaseActivity
import com.sleticalboy.learning.camera.CameraManager
import com.sleticalboy.learning.camera.CameraManager.OnPictureTakenCallback
import com.sleticalboy.learning.camera.CameraManager.SimpleSurfaceTextureListener
import com.sleticalboy.learning.databinding.ActivityLiveCameraBinding
import java.io.File

/**
 * Created on 18-2-27.
 *
 * @author leebin
 * @version 1.0
 */
class LiveCameraActivity : BaseActivity() {

  private var mBind: ActivityLiveCameraBinding? = null

  override fun logTag(): String = "LiveCamera"

  override fun layout(): View {
    // return R.layout.activity_live_camera
    mBind = ActivityLiveCameraBinding.inflate(layoutInflater)
    return mBind!!.root
  }

  override fun initView() {
    mBind!!.liveView.surfaceTextureListener = object : SimpleSurfaceTextureListener() {
      override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        CameraManager.getInstance().startPreview(surface)
      }
    }
    mBind!!.btnTakePic.setOnClickListener { takePicture() }
  }

  override fun onDestroy() {
    super.onDestroy()
    mBind = null
  }

  private fun takePicture() {
    // take photos
    CameraManager.getInstance().takePicture(getExternalFilesDirs(null)[0],
      object : OnPictureTakenCallback {
        override fun onSuccess(picture: File) {
          Log.d(logTag(), picture.path)
        }

        override fun onFailure(e: Throwable?) {
          Log.e(logTag(), "onFailure: ", e)
        }
      }
    )
  }
}