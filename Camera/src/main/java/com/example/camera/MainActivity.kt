package com.example.camera

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.camera.databinding.ActivityMainBinding

/**
 * Created on 2021/8/9
 *
 * @author binli@faceunity.com
 */
class MainActivity : AppCompatActivity() {

    private var mBinding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding!!.root)
        mBinding?.cameraV1!!.setOnClickListener {
            startActivity(Intent(this, CameraV1Activity::class.java))
        }
        mBinding?.cameraV1Gl!!.setOnClickListener {
            startActivity(Intent(this, CameraV1GlActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}