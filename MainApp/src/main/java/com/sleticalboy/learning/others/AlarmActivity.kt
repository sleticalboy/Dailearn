package com.sleticalboy.learning.others

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.sleticalboy.learning.R
import com.sleticalboy.learning.base.BaseActivity
import com.sleticalboy.learning.databinding.ActivityAlarmBinding
import com.sleticalboy.util.DevicesUtils
import com.sleticalboy.util.TimeUtils

/**
 * Created on 18-5-11.
 *
 * @author leebin
 */
class AlarmActivity : BaseActivity() {

  private var mAction: String? = null

  private val alarmReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      if (mAction == intent.action) {
        intent.action?.let { Log.d(logTag(), it) }
      }
    }
  }

  private var mAlarmManager: AlarmManager? = null
  private var mBind: ActivityAlarmBinding? = null

  override fun logTag(): String = "AlarmActivity"

  override fun layout(): View {
    // return R.layout.activity_alarm
    mBind = ActivityAlarmBinding.inflate(layoutInflater)
    return mBind!!.root
  }

  override fun initView() {
    val tvTime = findViewById<TextView>(R.id.tvTime)
    val start = TimeUtils.str2millis("2018-05-11T00:00:00+08:00")
    val end = TimeUtils.str2millis("2018-05-11T09:03:00+08:00")
    Log.d("AlarmActivity", "start:$start")
    Log.d("AlarmActivity", "end:$end")
    val time = "end - start = " + (end - start)
    Log.d("AlarmActivity", time)
    tvTime.text = time
    val msg = TimeUtils.millis2str(System.currentTimeMillis())
    msg?.let { Log.d(logTag(), it) }
    tvTime.append("$msg".trimIndent())
    val distance =
      TimeUtils.getDistance(34.7704267, 113.7584882, 34.7703974, 113.7583287).toDouble()
    tvTime.append("\ndistance = $distance")
    tvTime.append("mac address = ${DevicesUtils.getMacAddress(this)}")
    tvTime.append("wifi mac address = ${DevicesUtils.getConnectedWifiMacAddress(this)}")
  }

  override fun initData() {
    val filter = IntentFilter(mAction)
    registerReceiver(alarmReceiver, filter)
    val intent = Intent(mAction)
    val operation = PendingIntent.getBroadcast(this, 0, intent, 0)
    mAlarmManager!!.setRepeating(
      AlarmManager.RTC_WAKEUP,
      System.currentTimeMillis(),
      60000,
      operation
    )
  }

  override fun prepareWork(savedInstanceState: Bundle?) {
    mAction = "$packageName.TIMER_ATTEND"
    mAlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
  }

  override fun onDestroy() {
    super.onDestroy()
    mBind = null
    unregisterReceiver(alarmReceiver)
  }
}