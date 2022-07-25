package com.binlee.weight.xrefresh

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.binlee.learning.R

/**
 * Created on 18-2-3.
 *
 * @author leebin
 * @version 1.0
 * @description
 */
class XRefreshView @JvmOverloads constructor(
  private val mContext: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : FrameLayout(mContext, attrs, defStyleAttr) {
  private val mEmptyView: ViewGroup? = null
  private val mErrorView: ViewGroup? = null
  private val mProgressView: ViewGroup? = null
  private var mEmptyId = 0
  private var mProgressId = 0
  private var mErrorId = 0
  private fun init(attrs: AttributeSet?) {
    initView()
    initAttrs(attrs)
  }

  private fun initView() {
    if (isInEditMode) {
      return
    }
  }

  private fun initAttrs(attrs: AttributeSet?) {
    if (attrs == null) {
      return
    }
    val a = context.obtainStyledAttributes(attrs, R.styleable.XRefreshView)
    try {
      mEmptyId = a.getResourceId(R.styleable.XRefreshView_layout_empty, 0)
      mErrorId = a.getResourceId(R.styleable.XRefreshView_layout_error, 0)
      mProgressId = a.getResourceId(R.styleable.XRefreshView_layout_progress, 0)
    } finally {
      a.recycle()
    }
  }

  fun setEmptyView(emptyView: View?) {
    mEmptyView!!.removeAllViews()
    mEmptyView.addView(emptyView)
  }

  fun setEmptyView(emptyView: Int) {
    mEmptyView!!.removeAllViews()
    inflate(mContext, emptyView, mEmptyView)
  }

  fun setErrorView(errorView: View?) {
    mErrorView!!.removeAllViews()
    mErrorView.addView(errorView)
  }

  fun setErrorView(errorView: Int) {
    mErrorView!!.removeAllViews()
    inflate(mContext, errorView, mErrorView)
  }

  fun setProgressView(progressView: View?) {
    mProgressView!!.removeAllViews()
    mProgressView.addView(progressView)
  }

  fun setProgressView(progressView: Int) {
    mProgressView!!.removeAllViews()
    inflate(mContext, progressView, mProgressView)
  }

  companion object {
    private const val TAG = "XRefreshView"
  }

  init {
    init(attrs)
  }
}