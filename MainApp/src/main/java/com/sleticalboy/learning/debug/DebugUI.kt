package com.sleticalboy.learning.debug

import android.view.View
import com.sleticalboy.learning.base.BaseActivity
import com.sleticalboy.learning.databinding.ActivityDebugBinding
import com.sleticalboy.util.DebugUtils

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