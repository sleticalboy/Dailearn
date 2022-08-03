package com.binlee.learning.weight.view

import android.content.Context
import android.content.res.TypedArray
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.binlee.learning.R
import com.binlee.learning.util.UiUtils.dp2px
import com.binlee.learning.weight.xrecycler.helper.PageScrollHelper
import com.binlee.learning.weight.xrecycler.helper.PageScrollHelper.OnPageSelectedListener
import com.binlee.learning.weight.xrecycler.helper.PagerLayoutManager

/**
 * 使用 RecyclerView 实现的 ViewPager，支持单页翻动，支持自适应高度
 */
class PagerView @JvmOverloads constructor(
  context: Context?,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  private var mTextView: TextView? = null
  private var mRecyclerView: RecyclerView? = null
  private var mIndicatorLayout: LinearLayout? = null
  private var mRows = 0
  private var mColumns = 0
  private var mIndicatorSize = INDICATOR_SIZE.toInt()
  private var mIndicatorDrawableResId = INDICATOR_DRAWABLE
  var pageSize = 0
    private set
  private var mCurrentPage = 0
  private var mOnPageSelectedListener: OnPageSelectedListener? = null

  constructor(context: Context?, rows: Int, columns: Int) : this(context) {
    setRowsAndColumns(rows, columns)
  }

  fun setRowsAndColumns(rows: Int, columns: Int) {
    mRows = if (rows <= 0) {
      DEFAULT_ROWS
    } else {
      rows
    }
    mColumns = if (columns <= 0) {
      DEFAULT_COLUMNS
    } else {
      columns
    }
  }

  private fun initView() {
    LayoutInflater.from(context).inflate(R.layout.custom_pager_view_layout, this)
    mTextView = findViewById(R.id.tv_title)
    mRecyclerView = findViewById(R.id.recycler_view)
    mIndicatorLayout = findViewById(R.id.ll_indicators)
  }

  private fun initFromAttrs(attrs: AttributeSet?) {
    if (attrs == null) {
      return
    }
    var a: TypedArray? = null
    try {
      a = context.obtainStyledAttributes(attrs, R.styleable.PagerView)
      mRows = a.getInteger(R.styleable.PagerView_page_rows, DEFAULT_ROWS)
      mColumns = a.getInteger(R.styleable.PagerView_page_columns, DEFAULT_COLUMNS)
      mIndicatorSize = a.getDimensionPixelSize(
        R.styleable.PagerView_page_indicator_size, INDICATOR_SIZE.toInt()
      )
      mIndicatorDrawableResId = a.getResourceId(
        R.styleable.PagerView_page_indicator_drawable, INDICATOR_DRAWABLE
      )
      val gravity = a.getLayoutDimension(
        R.styleable.PagerView_page_indicator_gravity, DEFAULT_INDICATOR_LAYOUT_GRAVITY
      )
      if (gravity > 0) {
        setIndicatorLayoutGravity(gravity)
      }
    } finally {
      a?.recycle()
    }
  }

  private fun ensurePageSize() {
    calc()
  }

  fun setIndicatorLayoutGravity(gravity: Int) {
    val lp = mIndicatorLayout!!.layoutParams as FrameLayout.LayoutParams
    lp.gravity = gravity
    mIndicatorLayout!!.layoutParams = lp
  }

  private fun calc() {
    if (layoutManager == null) {
      return
    }
    if (layoutManager is PagerLayoutManager) {
      val pageSize = (layoutManager as PagerLayoutManager).getPageSize()
      this.pageSize = if (pageSize > MAX_PAGE_SIZE) MAX_PAGE_SIZE else pageSize
    }
  }

  private val layoutManager: RecyclerView.LayoutManager?
    get() = if (mRecyclerView == null || mRecyclerView!!.layoutManager == null) {
      null
    } else mRecyclerView!!.layoutManager

  private fun setLayoutManager(layoutManager: RecyclerView.LayoutManager) {
    if (mRecyclerView == null || mRecyclerView!!.layoutManager != null) {
      return
    }
    layoutManager.isAutoMeasureEnabled = true
    mRecyclerView!!.layoutManager = layoutManager
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    if (pageSize == 0) {
      ensurePageSize()
    }
    if (pageSize == 0) {
      return
    }
    mIndicatorLayout!!.removeAllViews()
    // 初始化 indicators
    for (i in 0 until pageSize) {
      val indicator: View = ImageView(context)
      if (mCurrentPage == i) {
        indicator.isPressed = true
        indicator.isSelected = true
        val params = LayoutParams(
          2 * mIndicatorSize, 2 * mIndicatorSize
        )
        params.setMargins(2 * mIndicatorSize, 0, 0, 0)
        indicator.layoutParams = params
      } else {
        indicator.isPressed = false
        indicator.isSelected = false
        val params = LayoutParams(
          mIndicatorSize, mIndicatorSize
        )
        params.setMargins(2 * mIndicatorSize, 0, 0, 0)
        indicator.layoutParams = params
      }
      indicator.setBackgroundResource(mIndicatorDrawableResId)
      mIndicatorLayout!!.addView(indicator)
    }
  }

  fun setIndicatorSize(indicatorSize: Int) {
    mIndicatorSize = if (indicatorSize < INDICATOR_SIZE) {
      INDICATOR_SIZE.toInt()
    } else {
      dp2px(context, indicatorSize.toFloat())
    }
  }

  fun setIndicatorDrawable(indicatorDrawableResId: Int) {
    mIndicatorDrawableResId = if (indicatorDrawableResId < 0) {
      INDICATOR_DRAWABLE
    } else {
      indicatorDrawableResId
    }
  }

  fun setTitle(title: CharSequence?) {
    if (!TextUtils.isEmpty(title)) {
      mTextView!!.text = title
      mTextView!!.visibility = VISIBLE
    }
  }

  fun scrollToPage(pageIndex: Int) {
    mCurrentPage = pageIndex
  }

  var adapter: RecyclerView.Adapter<*>?
    get() = if (mRecyclerView == null || mRecyclerView!!.adapter == null) {
      null
    } else mRecyclerView!!.adapter
    set(adapter) {
      if (mRecyclerView == null || adapter == null) {
        return
      }
      if (mRecyclerView!!.adapter != null) {
        return
      }
      mRecyclerView!!.adapter = adapter
      setLayoutManager(PagerLayoutManager(mRows, mColumns))
      setScrollHelper(PageScrollHelper())
      ensurePageSize()
    }

  private fun setScrollHelper(helper: PageScrollHelper?) {
    if (mRecyclerView == null || helper == null) {
      return
    }
    // 将 ScrollHelper 与 RecyclerView 关联起来
    helper.setUpWithRecycleView(mRecyclerView!!)
    if (mOnPageSelectedListener == null) {
      mOnPageSelectedListener = SimpleOnPageSelectedListener()
    }
    helper.setOnPageSelectedListener(mOnPageSelectedListener)
  }

  fun setOnPageSelectedListener(onPageSelectedListener: OnPageSelectedListener?) {
    mOnPageSelectedListener = onPageSelectedListener
  }

  inner class SimpleOnPageSelectedListener : OnPageSelectedListener {
    override fun onPageChanged(pageIndex: Int) {
      mCurrentPage = pageIndex % pageSize
      for (i in 0 until pageSize) {
        val view = mIndicatorLayout!!.getChildAt(i)
        if (mCurrentPage == i) {
          view.isSelected = true
          val params = LayoutParams(
            2 * mIndicatorSize, 2 * mIndicatorSize
          )
          params.setMargins(2 * mIndicatorSize, 0, 0, 0)
          view.layoutParams = params
        } else {
          view.isSelected = false
          val params = LayoutParams(
            mIndicatorSize, mIndicatorSize
          )
          params.setMargins(2 * mIndicatorSize, 0, 0, 0)
          view.layoutParams = params
        }
      }
    }
  }

  companion object {
    private const val TAG = "PagerView"
    private const val DEFAULT_ROWS = 1
    private const val DEFAULT_COLUMNS = DEFAULT_ROWS
    private const val INDICATOR_SIZE = 3.0f
    private const val INDICATOR_DRAWABLE = R.drawable.mx_page_indicator
    private const val MAX_PAGE_SIZE = 25
    private const val DEFAULT_INDICATOR_LAYOUT_GRAVITY = Gravity.END
  }

  init {
    orientation = VERTICAL
    initView()
    initFromAttrs(attrs)
    ensurePageSize()
  }
}