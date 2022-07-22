package com.sleticalboy.weight.xrefresh

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

import com.sleticalboy.learning.R
import com.sleticalboy.weight.xrefresh.interfaces.IHeaderView

/**
 * Created on 18-2-3.
 *
 * @author leebin
 * @version 1.0
 * @description
 */
class HeaderView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), IHeaderView {

  private var default_ptr_flip: ImageView? = null
  private var default_ptr_rotate: ProgressBar? = null
  private var refresh_tip: TextView? = null
  private var refresh_time: TextView? = null

  init {
    init()
  }

  private fun init() {
    LayoutInflater.from(context).inflate(R.layout.layout_refresh_header, this)
    default_ptr_flip = findViewById<View>(R.id.default_ptr_flip) as ImageView
    default_ptr_rotate = findViewById<View>(R.id.default_ptr_rotate) as ProgressBar
    refresh_tip = findViewById<View>(R.id.refresh_tip) as TextView
    refresh_time = findViewById<View>(R.id.refresh_time) as TextView
  }

  override fun begin() {

  }

  override fun progress(progress: Long, total: Long) {
    if (progress / total >= 0.9f) {
      default_ptr_flip!!.rotation = 180f
    } else {
      default_ptr_flip!!.rotation = 0f
    }
    if (progress >= total - 10) {
      refresh_tip!!.text = "松开以刷新"
    } else {
      refresh_tip!!.text = "下拉以刷新"
    }
  }

  override fun finish(progress: Long, total: Long) {

  }

  override fun loading() {
    default_ptr_flip!!.visibility = View.GONE
    default_ptr_rotate!!.visibility = View.VISIBLE
    refresh_tip!!.text = "加载中..."
    refresh_time!!.text = "2018-02-03 20:14:50"
  }

  override fun hidden() {
    default_ptr_flip!!.visibility = View.VISIBLE
    default_ptr_rotate!!.visibility = View.GONE
    refresh_tip!!.text = "下拉以刷新"
    refresh_time!!.text = "2018-02-03 20:15:03"
  }

  override fun get(): View {
    return this
  }
}
