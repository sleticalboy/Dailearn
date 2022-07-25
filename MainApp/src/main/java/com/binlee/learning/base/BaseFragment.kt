package com.binlee.learning.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

  final override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return layout(inflater, container)
  }

  final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    initView(view)
  }

  abstract fun initView(view: View)

  abstract fun layout(inflater: LayoutInflater, container: ViewGroup?): View

  protected open fun logTag(): String = "BaseFragment"
}