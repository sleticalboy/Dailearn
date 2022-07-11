package com.sleticalboy.learning.components.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log

/**
 * Created on 20-9-12.
 *
 * @author binlee sleticalboy@gmail.com
 */
class UpgradeService : Service() {

  private val mHandler = ServerHandler()
  private val mServerMessenger = Messenger(mHandler)
  private var mClientMessenger: Messenger? = null

  override fun onCreate() {
    Log.d(TAG, "onCreate() called")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val mgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
      mgr.createNotificationChannel(
        NotificationChannel(
          CHANNEL_ID,
          CHANNEL_ID,
          NotificationManager.IMPORTANCE_DEFAULT
        )
      )
      startForeground(1, Notification.Builder(this, CHANNEL_ID).build())
    }
  }

  override fun onBind(intent: Intent?): IBinder? {
    Log.d(TAG, "onBind() called with: intent = $intent")
    return null
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val mac = intent?.getStringExtra("_mac")
    val fileUrl = intent?.getStringExtra("_file_url")
    mClientMessenger = intent?.getParcelableExtra("_messenger")
    Log.d(
      TAG, "onStartCommand() intent = $intent, mac = $mac, file url = $fileUrl" +
          ", messenger: $mClientMessenger"
    )
    postRemote(1, "client binds to server")
    return START_STICKY
  }

  private fun postRemote(what: Int, obj: String) {
    val msg = Message.obtain()
    msg.what = what
    msg.replyTo = mServerMessenger
    // 不能使用 msg.obj 传递数据，否则："Can't marshal non-Parcelable objects across processes."
    msg.data.putString("msg_obj", obj)
    try {
      mClientMessenger?.send(msg)
    } catch (e: RemoteException) {
      Log.d(TAG, "post() error", e)
    }
  }

  override fun onDestroy() {
    Log.d(TAG, "onDestroy() called")
  }

  companion object {
    private const val CHANNEL_ID = "Upgrade channel"
    private const val TAG = "UpgradeService"
  }

  private inner class ServerHandler(looper: Looper? = Looper.myLooper()) : Handler() {

    override fun handleMessage(msg: Message) {
      Log.d(TAG, "handleMessage() called with: msg = $msg")
      if (msg.what == 2) {
        Log.d(TAG, "handleMessage() obj: " + msg.data.getString("msg_obj"))
      }
    }
  }
}