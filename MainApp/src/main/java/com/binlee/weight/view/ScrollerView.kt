package com.binlee.weight.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.Scroller

/**
 * Created on 18-6-10.
 *
 * @author leebin
 * @description
 */
class ScrollerView @JvmOverloads constructor(
  context: Context?,
  attrs: AttributeSet? = null,
  defStyle: Int = 0
) : ViewGroup(context, attrs, defStyle) {

  private val mTouchSlop // 最小滑动距离
      : Int = ViewConfiguration.get(context).scaledTouchSlop
  private val mScroller // 辅助滑动
      : Scroller = Scroller(context)
  private var mDownX // 手指按下时 x 的位置
      = 0f
  private var mMoveX // 手指移动时 x 的位置
      = 0f
  private var mUpX // 手指抬起时的 x 位置
      = 0f
  private var mLeftBorder // 左边临界点
      = 0
  private var mRightBorder // 右边临界点
      = 0

  private fun init() {
    //
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        mDownX = event.rawX
        mUpX = mDownX
      }
      MotionEvent.ACTION_MOVE -> {
        mMoveX = event.rawX
        val scrolledX = (mUpX - mMoveX).toInt()
        if (scrollX + scrolledX < mLeftBorder) {
          scrollTo(mLeftBorder, 0)
          return true
        } else if (scrollX + scrolledX + width > mRightBorder) {
          scrollTo(mRightBorder - width, 0)
          return true
        }
        scrollBy(scrolledX, 0)
        mUpX = mMoveX
      }
      MotionEvent.ACTION_UP -> {
        // 当手指抬起时，根据当前的滚动值来判定应该滚动到哪个子控件的界面
        val targetIndex = (scrollX + width / 2) / width
        val dx = targetIndex * width - scrollX
        // 第二步，调用startScroll()方法来初始化滚动数据并刷新界面
        mScroller.startScroll(scrollX, 0, dx, 0)
        invalidate()
      }
      else -> {
      }
    }
    return super.onTouchEvent(event)
  }

  override fun computeScroll() {
    // 第三步，重写computeScroll()方法，并在其内部完成平滑滚动的逻辑
    if (mScroller.computeScrollOffset()) {
      scrollTo(mScroller.currX, mScroller.currY)
      invalidate()
    }
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    val childCount = childCount
    for (i in 0 until childCount) {
      val childView = getChildAt(i)
      // 为ScrollerLayout中的每一个子控件测量大小
      measureChild(childView, widthMeasureSpec, heightMeasureSpec)
    }
  }

  override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
    when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        mDownX = event.rawX
        mUpX = mDownX
      }
      MotionEvent.ACTION_MOVE -> {
        mMoveX = event.rawX
        val deltaX = Math.abs(mMoveX - mDownX)
        // 当手指拖动值大于TouchSlop值时，认为应该进行滚动，拦截子控件的事件
        if (deltaX > mTouchSlop) {
          return true
        }
        mUpX = mMoveX
      }
      MotionEvent.ACTION_UP -> {
      }
      else -> {
      }
    }
    return super.onInterceptTouchEvent(event)
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    if (changed) {
      val childCount = childCount
      for (i in 0 until childCount) {
        val childView = getChildAt(i)
        // 为每一个子控件在水平方向上进行布局
        childView.layout(
          i * childView.measuredWidth, 0,
          (i + 1) * childView.measuredWidth, childView.measuredHeight
        )
      }
      // 初始化左右边界值
      mLeftBorder = getChildAt(0).left
      mRightBorder = getChildAt(childCount - 1).right
    }
  }

  init {
    // 第一步，创建Scroller的实例
    init()
  }
}