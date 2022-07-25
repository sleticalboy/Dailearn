package com.binlee.learning.dialogs

import android.view.View
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.databinding.ActivityDialogsBinding

/**
 * Created on 2021-08-14.
 * @author binlee
 */
class DialogsUI : BaseActivity() {

  private var bind: ActivityDialogsBinding? = null

  override fun layout(): View {
    bind = ActivityDialogsBinding.inflate(layoutInflater)
    return bind!!.root
  }

  override fun initView() {
    bind!!.btnFullScreen.setOnClickListener { showFragment(FullScreenDialog()) }
  }

  private fun showFragment(dialog: FullScreenDialog) {
    dialog.show(supportFragmentManager, dialog.javaClass.name)
  }
}