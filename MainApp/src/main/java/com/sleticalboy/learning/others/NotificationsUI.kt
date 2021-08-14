package com.sleticalboy.learning.others

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.sleticalboy.learning.R
import com.sleticalboy.learning.base.BaseActivity
import com.sleticalboy.learning.databinding.ActivityNotificationsBinding
import com.sleticalboy.util.NotificationHelper
import com.sleticalboy.util.ThreadHelper

/**
 * Created on 20-4-2.
 *
 * @author binlee sleticalboy@gmail.com
 */
class NotificationsUI : BaseActivity() {

    private var mManager: NotificationManager? = null
    private var mHandler: Handler? = null

    override fun prepareWork(savedInstanceState: Bundle?) {
        mManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mHandler = Handler(Looper.getMainLooper())
    }

    override fun layout(): View {
        // R.layout.activity_notifications
        return ActivityNotificationsBinding.inflate(layoutInflater).root
    }

    override fun initView() {
        findViewById<View>(R.id.notifyDelayWithLight).setOnClickListener { v: View? -> notifyDelayWithLight() }
        findViewById<View>(R.id.notifyWithProgress).setOnClickListener { v: View? -> notifyWithProgress() }
    }

    private fun notifyWithProgress() {
        val mgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val b = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "$TAG@${hashCode()}")
        } else {
            Notification.Builder(this)
        }
        b.setContentTitle("test progress notification")
        b.setSubText("progress: 0%")
        b.setProgress(100, 0, false)
        b.setSmallIcon(android.R.drawable.stat_sys_upload)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            mgr.createNotificationChannel(NotificationChannel(TAG, TAG, NotificationManager.IMPORTANCE_DEFAULT))
            b.setChannelId(TAG)
        }
        mgr.notify(this@NotificationsUI.hashCode(), b.build())
        val progress = intArrayOf(0)
        mHandler!!.postDelayed(object : Runnable {
            override fun run() {
                val p = progress[0]
                if (p == 100) {
                    mgr.cancel(this@NotificationsUI.hashCode())
                    return
                }
                b.setSubText("progress: $p%")
                b.setProgress(100, p, false)
                mgr.notify(this@NotificationsUI.hashCode(), b.build())
                progress[0] = p + 10
                mHandler!!.postDelayed(this, 1000L)
            }
        }, 100L)
    }

    private fun notifyDelayWithLight() {
        val builder = Notification.Builder(this)
        builder.setContentTitle("测试呼吸灯闪烁")
        builder.setContentText("这是一个伴随着呼吸灯闪烁的通知.")
        builder.setLights(Color.RED, 3000, 3000)
        builder.setSmallIcon(R.drawable.ic_sms_light_24dp)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NotificationHelper.SPECIAL_TAG)
        }
        builder.setDefaults(Notification.DEFAULT_ALL)
        val not = builder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            not.color = Color.RED
        }
        ThreadHelper.runOnMain(Runnable {
            mManager!!.notify(NotificationHelper.SPECIAL_TAG, -1, not)
        }, 3000L)
    }

    companion object {
        private const val TAG = "NotificationsUI"
    }
}