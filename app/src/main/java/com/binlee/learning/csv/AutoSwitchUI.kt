package com.binlee.learning.csv

import android.view.View
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.databinding.ActivityAutoSwitchBinding
import java.util.ArrayList

/**
 * Created on 18-10-23.
 *
 * @author leebin
 */
class AutoSwitchUI : BaseActivity() {

  private var mBind: ActivityAutoSwitchBinding? = null

  override fun layout(): View {
    // return R.layout.activity_auto_switch
    mBind = ActivityAutoSwitchBinding.inflate(layoutInflater)
    return mBind!!.root
  }

  override fun initView() {
  }

  override fun initData() {
    val textList: MutableList<String?> = ArrayList()
    for (i in 0..30) {
      textList.add("第 $i 条数据")
    }
    mBind!!.autoSwitchView.setTextList(textList)
    mBind!!.autoSwitchView.setAnimTime(300)
    mBind!!.autoSwitchView.setTextStillTime(3000)
  }

  override fun onPause() {
    super.onPause()
    mBind!!.autoSwitchView.stop()
  }

  override fun onResume() {
    super.onResume()
    mBind!!.autoSwitchView.start()
  }
}