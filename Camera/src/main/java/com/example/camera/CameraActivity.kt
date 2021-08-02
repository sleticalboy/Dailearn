package com.example.camera

import android.Manifest
import android.graphics.PixelFormat
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.camera.databinding.MainBinding

class CameraActivity : AppCompatActivity() {

    private var binding: MainBinding? = null
    private var width = 0
    private var height = 0

    /**
     * Called when the activity was first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        binding = MainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        initView()
        bindListeners()
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        requestPermissions(arrayOf(Manifest.permission.CAMERA), 0x10)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != 0x10) return
        Log.d(
            TAG,
            "onRequestPermissionsResult() permissions = ${permissions.contentToString()}, grantResults = ${grantResults.contentToString()}"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun initView() {
        //translucent半透明 transparent透明
        binding!!.surfaceView.holder.setFormat(PixelFormat.TRANSPARENT)
        binding!!.surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                CameraWrapper.get().open()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                Log.d(
                    TAG,
                    "surfaceChanged() called with: format = $format, width = $width, height = $height"
                )
                CameraWrapper.get().preview(holder)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                CameraWrapper.get().stopPreview()
            }
        })

        binding!!.cameraFocusLayout.post {
            width = binding!!.cameraFocusLayout.width / 2
            height = binding!!.cameraFocusLayout.height / 2
        }
    }

    private fun bindListeners() {
        binding!!.takeBtn.setOnClickListener { v: View? -> CameraWrapper.get().doTakePic() }
        binding!!.surfaceView.setOnTouchListener { _: View?, event: MotionEvent? ->
            if (CameraWrapper.get().cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return@setOnTouchListener gestureDetector.onTouchEvent(event)
            } else {
                return@setOnTouchListener false
            }
        }
        binding!!.flashIv.setOnClickListener {
            CameraWrapper.get().setFlashMode(binding!!.flashIv)
        }
        binding!!.swichCameraIv.setOnClickListener { changeCamera() }
    }

    private var gestureDetector =
        GestureDetector(object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                Log.d("MyGestureDetector", "onDown")
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                Log.d("MyGestureDetector", "onSingleTapUp")
                CameraWrapper.get().autoFocus { success: Boolean, _: Camera? ->
                    if (success) {
                        Log.d(TAG, "聚焦成功")
                    } else {
                        Log.d(TAG, "聚焦失败")
                    }
                    binding!!.cameraFocusLayout.visibility = View.GONE
                }
                CameraWrapper.get().setFocusArea(this@CameraActivity, e)
                showFocusIcon(e)
                return true
            }
        })

    private fun showFocusIcon(e: MotionEvent) {
        val x = e.x.toInt()
        val y = e.y.toInt()
        val lp = binding!!.cameraFocusLayout.layoutParams as FrameLayout.LayoutParams
        lp.leftMargin = (x - width + 0.5).toInt()
        lp.topMargin = (y - height + 0.5 + binding!!.swichCameraIv.height).toInt()
        Log.d(
            TAG, "showFocusIcon x" + x + "y" + y + "params.width" + lp.width
                    + "params.height" + lp.height
        )
        binding!!.cameraFocusLayout.layoutParams = lp
        binding!!.cameraFocusLayout.visibility = View.VISIBLE
    }

    private fun changeCamera() {
        CameraWrapper.get().stopPreview()
        val newCameraId: Int = (CameraWrapper.get().cameraId + 1) % 2
        CameraWrapper.get().open(newCameraId)
        CameraWrapper.get().preview(binding!!.surfaceView.holder)
        if (newCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            binding!!.swichCameraIv.setImageResource(R.drawable.camera_setting_switch_back)
        } else {
            binding!!.swichCameraIv.setImageResource(R.drawable.camera_setting_switch_front)
        }
    }

    companion object {
        private const val TAG = "CameraActivity"
    }
}