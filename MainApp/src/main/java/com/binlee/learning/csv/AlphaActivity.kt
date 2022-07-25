package com.binlee.learning.csv

import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.databinding.ActivityAlphaBinding

/**
 * Created on 18-5-29.
 *
 * @author leebin
 * @description 控件透明度研究
 */
class AlphaActivity : BaseActivity() {

  private var mBind: ActivityAlphaBinding? = null
  private val mLocation = IntArray(2)

  override fun layout(): View {
    // return R.layout.activity_alpha
    mBind = ActivityAlphaBinding.inflate(layoutInflater)
    return mBind!!.root
  }

  override fun initView() {
    mBind!!.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        Log.d(
          logTag(),
          "onProgressChanged() progress = [$progress], fromUser = [$fromUser]"
        )
        mBind!!.tvShow.alpha = progress / 100f
      }

      override fun onStartTrackingTouch(seekBar: SeekBar) {
        Log.d(logTag(), "onStartTrackingTouch() called with: seekBar = [$seekBar]")
      }

      override fun onStopTrackingTouch(seekBar: SeekBar) {
        Log.d(logTag(), "onStopTrackingTouch() called with: seekBar = [$seekBar]")
      }
    })
    mBind!!.touchButton.setOnTouchListener { _: View?, _: MotionEvent? ->
      mBind!!.touchButton.getLocationInWindow(mLocation)
      Log.d(logTag(), "mLocation[0]:" + mLocation[0])
      Log.d(logTag(), "mLocation[1]:" + mLocation[1])
      Log.d(logTag(), "touchButton.getMeasuredWidth():" + mBind!!.touchButton.measuredWidth)
      Log.d(logTag(), "touchButton.getMeasuredHeight():" + mBind!!.touchButton.measuredHeight)
      false
    }
  }

  override fun logTag(): String = "AlphaActivity"
}