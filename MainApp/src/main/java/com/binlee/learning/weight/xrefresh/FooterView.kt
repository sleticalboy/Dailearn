package com.binlee.learning.weight.xrefresh

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout

import com.binlee.learning.R
import com.binlee.learning.weight.xrefresh.interfaces.IFooterView

/**
 * Created on 18-2-3.
 *
 * @author leebin
 * @version 1.0
 * @description
 */
class FooterView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IFooterView {

  init {
    init()
  }

  private fun init() {
    LayoutInflater.from(context).inflate(R.layout.layout_refresh_footer, this)
  }

  override fun begin() {

  }

  override fun progress(progress: Long, total: Long) {

  }

  override fun finish(progress: Long, total: Long) {

  }

  override fun loading() {

  }

  override fun hidden() {

  }

  override fun get(): View {
    return this
  }
}
