package com.binlee.learning.csv

import android.view.View
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.databinding.ActivityScrollerBinding

/**
 * Created on 18-6-10.
 *
 * @author leebin
 * @description Scroller 使用练习
 */
class ScrollerActivity : BaseActivity() {

  private var bind: ActivityScrollerBinding? = null

  override fun layout(): View {
    // R.layout.activity_scroller
    bind = ActivityScrollerBinding.inflate(layoutInflater)
    return bind!!.root
  }

  override fun initView() {
    bind!!.scrollToBtn.setOnClickListener { bind!!.layout.scrollTo(-60, -100) }
    bind!!.scrollByBtn.setOnClickListener { bind!!.layout.scrollBy(-60, -100) }
  }

  override fun initData() {}
}