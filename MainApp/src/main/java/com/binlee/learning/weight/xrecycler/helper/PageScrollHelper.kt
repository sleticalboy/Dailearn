package com.binlee.learning.weight.xrecycler.helper

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import kotlin.math.abs

/**
 * 实现 RecycleView 分页滚动的工具类
 * Created by zhuguohui on 2016/11/10.
 */
class PageScrollHelper {

  private val mOnScrollListener: MyOnScrollListener = MyOnScrollListener()
  private val mOnFlingListener = MyOnFlingListener()
  private val mOnTouchListener: MyOnTouchListener = MyOnTouchListener()
  private var mRecyclerView: RecyclerView? = null
  private var mOnPageSelectedListener: OnPageSelectedListener? = null
  private var mOrientation = HORIZONTAL
  private var offsetY = 0
  private var offsetX = 0
  private var startY = 0
  private var startX = 0
  private var mAnimator: ValueAnimator? = null

  fun setUpWithRecycleView(recycleView: RecyclerView) {
    mRecyclerView = recycleView
    //处理滑动
    recycleView.onFlingListener = mOnFlingListener
    //设置滚动监听，记录滚动的状态，和总的偏移量
    recycleView.setOnScrollListener(mOnScrollListener)
    //记录滚动开始的位置
    recycleView.setOnTouchListener(mOnTouchListener)
    //获取滚动的方向
    updateLayoutManger()
  }

  private fun updateLayoutManger() {
    if (mRecyclerView == null || mRecyclerView!!.layoutManager == null) {
      return
    }
    val layoutManager = mRecyclerView!!.layoutManager
    mOrientation = if (layoutManager!!.canScrollVertically()) {
      VERTICAL
    } else if (layoutManager.canScrollHorizontally()) {
      HORIZONTAL
    } else {
      ORIENTATION_NULL
    }
    if (mAnimator != null) {
      mAnimator!!.cancel()
    }
    startX = 0
    startY = 0
    offsetX = 0
    offsetY = 0
  }

  private val pageIndex: Int
    private get() {
      val p: Int
      p = if (mOrientation == VERTICAL) {
        offsetY / mRecyclerView!!.height
      } else {
        offsetX / mRecyclerView!!.width
      }
      return p
    }
  private val startPageIndex: Int
    private get() {
      val p: Int
      p = if (mOrientation == VERTICAL) {
        startY / mRecyclerView!!.height
      } else {
        startX / mRecyclerView!!.width
      }
      return p
    }

  fun setOnPageSelectedListener(listener: OnPageSelectedListener?) {
    mOnPageSelectedListener = listener
  }

  interface OnPageSelectedListener {
    fun onPageChanged(pageIndex: Int)
  }

  private inner class MyOnFlingListener : OnFlingListener() {
    override fun onFling(velocityX: Int, velocityY: Int): Boolean {
      if (mOrientation == ORIENTATION_NULL) {
        return false
      }
      //获取开始滚动时所在页面的index
      var p = startPageIndex

      //记录滚动开始和结束的位置
      var endPoint = 0
      var startPoint = 0

      //如果是垂直方向
      if (mOrientation == VERTICAL) {
        startPoint = offsetY
        if (velocityY < 0) {
          p--
        } else if (velocityY > 0) {
          p++
        }
        //更具不同的速度判断需要滚动的方向
        //注意，此处有一个技巧，就是当速度为0的时候就滚动会开始的页面，即实现页面复位
        endPoint = p * mRecyclerView!!.height
      } else {
        startPoint = offsetX
        if (velocityX < 0) {
          p--
        } else if (velocityX > 0) {
          p++
        }
        endPoint = p * mRecyclerView!!.width
      }
      if (endPoint < 0) {
        endPoint = 0
      }

      //使用动画处理滚动
      if (mAnimator == null) {
        val anim = ValueAnimator.ofInt(startPoint, endPoint)
        anim.duration = 300
        anim.addUpdateListener(AnimatorUpdateListener { animation ->
          val nowPoint = animation.animatedValue as Int
          if (mOrientation == VERTICAL) {
            val dy = nowPoint - offsetY
            //这里通过RecyclerView的scrollBy方法实现滚动。
            mRecyclerView!!.scrollBy(0, dy)
          } else {
            val dx = nowPoint - offsetX
            mRecyclerView!!.scrollBy(dx, 0)
          }
        })
        // 回调监听
        if (mOnPageSelectedListener != null) {
          anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
              mOnPageSelectedListener!!.onPageChanged(pageIndex)
            }
          })
        }
        mAnimator = anim
      } else {
        mAnimator!!.cancel()
        mAnimator!!.setIntValues(startPoint, endPoint)
      }
      mAnimator!!.start()
      return true
    }
  }

  private inner class MyOnScrollListener : RecyclerView.OnScrollListener() {
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
      //newState==0表示滚动停止，此时需要处理回滚
      if (newState == 0 && mOrientation != ORIENTATION_NULL) {
        val move: Boolean
        var vX = 0
        var vY = 0
        if (mOrientation == VERTICAL) {
          val absY = Math.abs(offsetY - startY)
          //如果滑动的距离超过屏幕的一半表示需要滑动到下一页
          move = absY > recyclerView.height / 2
          vY = 0
          if (move) {
            vY = if (offsetY - startY < 0) -1000 else 1000
          }
        } else {
          val absX = abs(offsetX - startX)
          move = absX > recyclerView.width / 2
          if (move) {
            vX = if (offsetX - startX < 0) -1000 else 1000
          }
        }
        mOnFlingListener.onFling(vX, vY)
      }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
      //滚动结束记录滚动的偏移量
      offsetY += dy
      offsetX += dx
    }
  }

  private inner class MyOnTouchListener : OnTouchListener {

    override fun onTouch(v: View, event: MotionEvent): Boolean {
      //手指按下的时候记录开始滚动的坐标
      if (event.action == MotionEvent.ACTION_DOWN) {
        startY = offsetY
        startX = offsetX
      }
      return false
    }
  }

  companion object {
    const val HORIZONTAL = LinearLayout.HORIZONTAL
    const val VERTICAL = LinearLayout.VERTICAL
    const val ORIENTATION_NULL = 0xfff
  }
}