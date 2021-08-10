package com.example.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.hardware.Camera
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.camera.compat.ICamera
import com.example.camera.databinding.CameraBaseBinding
import java.lang.IllegalArgumentException

abstract class CameraBase : AppCompatActivity() {

    protected val logTag: String = javaClass.simpleName
    private var mGranted = false
    private var mBinding: CameraBaseBinding? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CameraCompat.get().init(applicationContext)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFormat(PixelFormat.TRANSPARENT)
        supportActionBar?.hide()
        mGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        mBinding = CameraBaseBinding.inflate(layoutInflater)
        setContentView(mBinding!!.root)
        initView()
    }

    abstract fun getSurfaceView(): View

    private fun initView() {
        val surfaceView = getSurfaceView()
        mBinding!!.root.addView(surfaceView, 0, ConstraintLayout.LayoutParams(-1, -1))
        // translucent半透明 transparent透明
        if (surfaceView is SurfaceView) {
            surfaceView.holder.setFormat(PixelFormat.TRANSPARENT)
        } else if (surfaceView is TextureView) {
            //
        } else {
            throw IllegalArgumentException("wrong surface view")
        }

        // set listeners
        mBinding!!.takeBtn.setOnClickListener {
            CameraCompat.get().doTakePic(object : ICamera.TakePhotoCallback {
                override fun onTaken(data: ByteArray, camera: ICamera) {
                }
            })
        }
        surfaceView.setOnTouchListener { _: View?, event: MotionEvent? ->
            if (CameraCompat.get().getId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return@setOnTouchListener gestureDetector.onTouchEvent(event)
            } else {
                return@setOnTouchListener false
            }
        }
        mBinding!!.flashIv.setOnClickListener {
            CameraCompat.get().setFlashMode(object : ICamera.FlashModeChooser {
                override fun choose(previousMode: String): String {
                    val imageView = mBinding!!.flashIv
                    when (previousMode) {
                        Camera.Parameters.FLASH_MODE_OFF -> {
                            imageView.setImageResource(R.drawable.camera_setting_flash_on_normal)
                            return Camera.Parameters.FLASH_MODE_ON
                        }
                        Camera.Parameters.FLASH_MODE_ON -> {
                            imageView.setImageResource(R.drawable.camera_setting_flash_auto_normal)
                            return Camera.Parameters.FLASH_MODE_AUTO
                        }
                        Camera.Parameters.FLASH_MODE_AUTO -> {
                            imageView.setImageResource(R.drawable.camera_setting_flash_off_normal)
                            return Camera.Parameters.FLASH_MODE_OFF
                        }
                        else -> {
                            imageView.setImageResource(R.drawable.camera_setting_flash_off_normal)
                            return Camera.Parameters.FLASH_MODE_OFF
                        }
                    }
                }
            })
        }
        mBinding!!.swichCameraIv.setOnClickListener { changeCamera() }
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || mGranted) return
        requestPermissions(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 0x10
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != 0x10) return
        Log.d(
            logTag,
            "onRequestPermissionsResult() permissions = ${permissions.contentToString()}, grantResults = ${grantResults.contentToString()}"
        )
        mGranted = true
        openCamera()
        startPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }

    private val gestureDetector =
        GestureDetector(object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                Log.d("MyGestureDetector", "onDown")
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                Log.d("MyGestureDetector", "onSingleTapUp")
                CameraCompat.get().autoFocus(object : ICamera.AutoFocusCallback {
                    override fun onAutoFocus(success: Boolean, camera: ICamera) {
                        Log.d(logTag, if (success) "聚焦成功" else "聚焦失败")
                        hideFocusIcon()
                    }
                })
                CameraCompat.get().setFocusArea(e.rawX, e.rawY)
                showFocusIcon(e)
                return true
            }
        })

    private fun hideFocusIcon() {
        val lp = mBinding!!.cameraFocus.layoutParams as ConstraintLayout.LayoutParams
        lp.leftMargin = 0
        lp.topMargin = 0
        mBinding!!.cameraFocus.layoutParams = lp
        mBinding!!.cameraFocus.visibility = View.GONE
    }

    private fun showFocusIcon(e: MotionEvent) {
        val x = e.x.toInt()
        val y = e.y.toInt()
        val lp = mBinding!!.cameraFocus.layoutParams as ConstraintLayout.LayoutParams
        lp.leftMargin = x - mBinding!!.cameraFocus.width / 2
        lp.topMargin = y - mBinding!!.cameraFocus.height / 2
        Log.d(
            logTag, "showFocusIcon x" + x + "y" + y + "params.width" + lp.width
                    + "params.height" + lp.height
        )
        mBinding!!.cameraFocus.layoutParams = lp
        mBinding!!.cameraFocus.visibility = View.VISIBLE
    }

    private fun changeCamera() {
        stopPreview()
        val newCameraId: Int = (CameraCompat.get().getId() + 1) % 2
        CameraCompat.get().open(newCameraId)
        startPreview()
        if (newCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mBinding!!.swichCameraIv.setImageResource(R.drawable.camera_setting_switch_back)
        } else {
            mBinding!!.swichCameraIv.setImageResource(R.drawable.camera_setting_switch_front)
        }
    }

    protected fun openCamera() {
        if (!mGranted) return
        CameraCompat.get().open()
    }

    protected fun startPreview() {
        if (!mGranted) return
        val surfaceView = getSurfaceView()
        if (surfaceView is SurfaceView) {
            CameraCompat.get().startPreview(surfaceView.holder)
        } else if (surfaceView is TextureView) {
            CameraCompat.get().startPreview(surfaceView)
        }
    }

    protected fun stopPreview() {
        CameraCompat.get().stopPreview()
    }
}