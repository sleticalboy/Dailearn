package com.binlee.emoji.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.GridView
import androidx.viewpager.widget.ViewPager

/**
 * Created on 19-7-17.
 *
 * @author leebin
 */
class EmojiViewPager @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) :
  ViewPager(context!!, attrs) {

  private var mParentVp: ViewPager? = null

  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (mParentVp == null) {
      mParentVp = findParentViewPager(this)
    }
    when (event.action) {
      MotionEvent.ACTION_DOWN -> if (childCount > 1 && mParentVp != null) {
        mParentVp!!.requestDisallowInterceptTouchEvent(true)
      }
      MotionEvent.ACTION_MOVE -> {
      }
      MotionEvent.ACTION_UP -> {
      }
    }
    // Log.d(TAG, "onTouchEvent() handled: " + superHandled + ", event: " + event + ", vp: " + mParentVp);
    return super.onTouchEvent(event)
  }

  private fun findParentViewPager(child: View): ViewPager? {
    val parent = child.parent
    if (parent is ViewPager) {
      return parent
    } else if (parent is View) {
      return findParentViewPager(parent as View)
    }
    return null
  }

  private fun hasChildHandled(event: MotionEvent): Boolean {
    var i = 0
    val count = childCount
    while (i < count) {
      val child = getChildAt(i)
      if (child is GridView) {
        Log.d(TAG, "$i child:$child")
        if (child.onTouchEvent(event)) {
          return true
        }
      }
      i++
    }
    return false
  }

  companion object {
    private const val TAG = "EmojiViewPager"
  }
}