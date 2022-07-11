package com.sleticalboy.learning.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sleticalboy.learning.R
import com.sleticalboy.learning.base.BaseDialog
import com.sleticalboy.learning.databinding.DialogFullScreenBinding

/**
 * Created on 2021-08-14.
 * @author binlee
 */
class FullScreenDialog : BaseDialog() {

  private var bind: DialogFullScreenBinding? = null

  override fun getTheme(): Int {
    return R.style.DialogFullScreen
  }

  override fun layout(inflater: LayoutInflater, parent: ViewGroup?): View {
    bind = DialogFullScreenBinding.inflate(inflater, parent, false)
    return bind!!.root
  }

  override fun initView(view: View) {
  }

  companion object {
    var TAG = "FullScreenDialog"
  }
}