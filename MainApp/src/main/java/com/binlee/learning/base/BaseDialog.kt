package com.binlee.learning.base

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

abstract class BaseDialog : DialogFragment() {

  protected val TAG: String = javaClass.simpleName

  final override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = super.onCreateDialog(savedInstanceState)
    configureDialog(dialog)
    return dialog
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return layout(inflater, container)
  }

  final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    initView(view)
  }

  override fun onStart() {
    super.onStart()
    if (fullScreen()) {
      // 移除四周 padding，使全屏
      dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
      // 设置宽高
      dialog!!.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
      )
    }
  }

  protected open fun fullScreen(): Boolean = false

  abstract fun initView(view: View)

  abstract fun layout(inflater: LayoutInflater, parent: ViewGroup?): View

  protected open fun configureDialog(dialog: Dialog) {
  }
}