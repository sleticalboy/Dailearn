package com.sleticalboy.learning.components

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.view.View
import com.sleticalboy.learning.base.BaseActivity
import com.sleticalboy.learning.components.service.LocalService
import com.sleticalboy.learning.databinding.ActivityServiceBinding

class ServicePractise : BaseActivity() {

  private var mBind: ActivityServiceBinding? = null
  private var mService: LocalService? = null

  private val connection = object : ServiceConnection {

    override fun onServiceDisconnected(name: ComponentName?) {
      mBind!!.tvBindProgress.text = "Disconnected"
      mService = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
      mBind!!.tvBindProgress.text = "Connected"
      mService = (service as LocalService.LocalBinder).getService()
    }
  }

  override fun layout(): View {
    // R.layout.activity_service
    mBind = ActivityServiceBinding.inflate(layoutInflater)
    return mBind!!.root
  }

  override fun initView() {
    mBind!!.btnStart.setOnClickListener {
      Log.d(logTag(), "start service")
    }
    mBind!!.btnStop.setOnClickListener {
      Log.d(logTag(), "stop service")
    }

    mBind!!.tvBindProgress.text = "Idle"
    mBind!!.btnBind.setOnClickListener {
      mBind!!.tvBindProgress.text = "Connecting..."
      doBindService()
    }
    mBind!!.btnUnbind.setOnClickListener {
      mBind!!.tvBindProgress.text = "Disconnecting..."
      doUnbindService()
    }
    mBind!!.serviceFoo.setOnClickListener {
    }
  }

  private fun doBindService() {
    val service = Intent(this, LocalService::class.java)
    bindService(service, connection, Context.BIND_AUTO_CREATE)
  }

  private fun doUnbindService() {
    unbindService(connection)
  }
}
