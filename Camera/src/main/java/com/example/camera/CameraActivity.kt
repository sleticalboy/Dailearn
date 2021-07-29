package com.example.camera

import android.Manifest
import android.graphics.PixelFormat
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.camera.util.CameraInstance
import java.lang.ref.WeakReference

class CameraActivity : AppCompatActivity() {

    private lateinit var takeBtn: ImageButton
    private lateinit var surfacePreview: SurfaceView
    private lateinit var focusLayout: FrameLayout
    private lateinit var changeFlashModeIV: ImageView
    private lateinit var switchCameraIV: ImageView
    private var width = 0
    private var height = 0
    private val mainHandler = MainHandler(this)

    /**
     * Called when the activity was first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        setContentView(R.layout.main)
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

    private fun initView() {
        surfacePreview = findViewById(R.id.surface_view)
        //translucent半透明 transparent透明
        surfacePreview.holder.setFormat(PixelFormat.TRANSPARENT)
        surfacePreview.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                CameraInstance.get().doOpenCamera(Camera.CameraInfo.CAMERA_FACING_BACK)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                CameraInstance.get().doStartPreview(holder)
                mainHandler.sendEmptyMessageDelayed(CameraInstance.PREVIEW_HAS_STARTED, 1000)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                CameraInstance.get().doStopPreview()
            }
        })

        takeBtn = findViewById(R.id.take_btn)
        focusLayout = findViewById(R.id.camera_focus_layout)

        focusLayout.post {
            width = focusLayout.width / 2
            height = focusLayout.height / 2
        }

        changeFlashModeIV = findViewById(R.id.flash_iv)
        switchCameraIV = findViewById(R.id.swich_camera_iv)
    }

    private fun bindListeners() {
        takeBtn.setOnClickListener { v: View? -> CameraInstance.get().doTakePic() }
        surfacePreview.setOnTouchListener { _: View?, event: MotionEvent? ->
            if (CameraInstance.get().cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return@setOnTouchListener gestureDetector.onTouchEvent(event)
            } else {
                return@setOnTouchListener false
            }
        }
        changeFlashModeIV.setOnClickListener {
            CameraInstance.get().setFlashMode(changeFlashModeIV)
        }
        switchCameraIV.setOnClickListener { changeCamera() }
    }

    private var gestureDetector =
        GestureDetector(object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                Log.d("MyGestureDetector", "onDown")
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                Log.d("MyGestureDetector", "onSingleTapUp")
                CameraInstance.get().autoFocus { success: Boolean, _: Camera? ->
                    if (success) {
                        Log.d(TAG, "聚焦成功")
                    } else {
                        Log.d(TAG, "聚焦失败")
                    }
                    focusLayout.visibility = View.GONE
                }
                CameraInstance.get().setFocusArea(this@CameraActivity, e)
                showFocusIcon(e)
                return true
            }
        })

    private fun showFocusIcon(e: MotionEvent) {
        val x = e.x.toInt()
        val y = e.y.toInt()
        val lp = focusLayout.layoutParams as FrameLayout.LayoutParams
        lp.leftMargin = (x - width + 0.5).toInt()
        lp.topMargin = (y - height + 0.5 + switchCameraIV.height).toInt()
        Log.d(
            TAG, "showFocusIcon x" + x + "y" + y + "params.width" + lp.width
                    + "params.height" + lp.height
        )
        focusLayout.layoutParams = lp
        focusLayout.visibility = View.VISIBLE
    }

    private fun changeCamera() {
        CameraInstance.get().doStopPreview()
        val newCameraId: Int = (CameraInstance.get().cameraId + 1) % 2
        CameraInstance.get().doOpenCamera(newCameraId)
        CameraInstance.get().doStartPreview(surfacePreview.holder)
        if (newCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            switchCameraIV.setImageResource(R.drawable.camera_setting_switch_back)
        } else {
            switchCameraIV.setImageResource(R.drawable.camera_setting_switch_front)
        }
    }

    private fun startGoogleDetect() {
        val parameters: Camera.Parameters = CameraInstance.get().cameraParameters ?: return
        val camera: Camera = CameraInstance.get().camera ?: return
        if (parameters.maxNumDetectedFaces <= 0) return
        camera.setFaceDetectionListener { faces, _/*camera*/ ->
            val msg = mainHandler.obtainMessage()
            msg.what = CameraInstance.RECEIVE_FACE_MSG
            msg.obj = faces
            msg.sendToTarget()
        }
        camera.startFaceDetection()
    }

    private class MainHandler(host: CameraActivity) : Handler() {

        private val mHost: WeakReference<CameraActivity> = WeakReference(host)

        override fun handleMessage(msg: Message) {
            val host = mHost.get() ?: return
            when (msg.what) {
                CameraInstance.PREVIEW_HAS_STARTED -> {
                    host.startGoogleDetect()
                    Log.e(TAG, "开启人脸识别")
                }
                CameraInstance.RECEIVE_FACE_MSG -> {
                    Log.e(TAG, "收到人脸识别的信息")
                }
            }
        }

    }

    companion object {
        private const val TAG = "CameraActivity"
    }
}