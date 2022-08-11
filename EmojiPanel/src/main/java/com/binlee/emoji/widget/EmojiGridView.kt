package com.binlee.emoji.widget

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.GridView

/**
 * Created on 19-7-20.
 *
 * @author leebin
 */
class EmojiGridView @JvmOverloads constructor(
  context: Context?, attrs: AttributeSet? = null,
  defStyle: Int = 0
) : GridView(context, attrs, defStyle) {

  private var mOnPress: OnPressListener? = null

  // private int mActivePid = MotionEvent.INVALID_POINTER_ID;
  private var mLastPos = INVALID_POSITION
  private var mDownTime: Long = -1
  private var mIsLongPressUp = false

  private val mEventHandler = Handler { msg: Message ->
    if (msg.what == LONG_PRESS) {
      handlePressEvent(msg.arg1, msg.arg2)
    }
    true
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    // Log.d(TAG, "onTouchEvent() called with: event = [" + event + "]");
    when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        // mActivePid = event.getPointerId(0);
        mDownTime = event.downTime
        mIsLongPressUp = false
        mEventHandler.removeMessages(LONG_PRESS)
        val msg = Message.obtain(mEventHandler, LONG_PRESS)
        msg.arg1 = event.x.toInt()
        msg.arg2 = event.y.toInt()
        mEventHandler.sendMessageAtTime(msg, mDownTime + 500L)
      }
      MotionEvent.ACTION_MOVE -> {
        mIsLongPressUp = false
        mEventHandler.removeMessages(LONG_PRESS)
        val duration = event.eventTime - mDownTime
        // Log.d(TAG, "onTouchEvent() duration: " + duration);
        if (duration > 500L && handlePressEvent(event.x.toInt(), event.y.toInt())) {
          return true
        }
      }
      MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
        mEventHandler.removeMessages(LONG_PRESS)
        // mActivePid = MotionEvent.INVALID_POINTER_ID;
        mIsLongPressUp = event.eventTime - mDownTime > 500L
        mLastPos = INVALID_POSITION
        mDownTime = -1
        if (mOnPress != null) {
          mOnPress!!.onCancelPress()
        }
      }
    }
    return super.onTouchEvent(event)
  }

  override fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
    super.setOnItemLongClickListener(null)
  }

  override fun performItemClick(view: View, position: Int, id: Long): Boolean {
    return if (mIsLongPressUp) {
      /* 返回 true：长按抬起时不触发 onItemClick 事件 */
      true
    } else super.performItemClick(view, position, id)
  }

  fun setOnPressListener(listener: OnPressListener?) {
    mOnPress = listener
  }

  private fun handlePressEvent(x: Int, y: Int): Boolean {
    val pos = if (mOnPress != null) pointToPosition(x, y) else INVALID_POSITION
    Log.d(TAG, "handlePressEvent() pos: $pos, pressCallback: $mOnPress")
    if (pos != INVALID_POSITION && pos != mLastPos) {
      mOnPress!!.onLongPress(pos)
      mLastPos = pos
      return true
    } else if (pos == INVALID_POSITION && mOnPress != null) {
      mOnPress!!.onCancelPress()
      mLastPos = pos
      return true
    }
    return false
  }

  interface OnPressListener {
    /**
     * 长按回调
     *
     * @param position 按住的位置
     */
    fun onLongPress(position: Int)

    /**
     * 手指离开屏幕回调
     */
    fun onCancelPress()
  }

  companion object {
    private const val TAG = "EmojiGridView"
    private const val LONG_PRESS = 2
  }
}