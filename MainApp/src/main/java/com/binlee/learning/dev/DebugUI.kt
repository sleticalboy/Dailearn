package com.binlee.learning.dev

import android.view.View
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.databinding.ActivityDebugBinding
import com.binlee.util.DebugUtils

class DebugUI : BaseActivity() {

  private var mBind: ActivityDebugBinding? = null

  override fun layout(): View {
    // R.layout.activity_debug
    mBind = ActivityDebugBinding.inflate(layoutInflater)
    return mBind!!.root
  }

  override fun initView() {
    mBind!!.openSettings.setOnClickListener { DebugUtils.openSettings(application) }
    mBind!!.debugLayout.setOnCheckedChangeListener { _, isChecked ->
      DebugUtils.debugLayout(isChecked)
    }
  }
}
