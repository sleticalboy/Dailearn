package com.binlee.weight

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ViewSwitcher
import com.binlee.learning.R
import java.lang.ref.SoftReference
import java.util.ArrayList

/**
 * Created on 18-10-24.
 *
 * @author leebin
 */
class AutoSwitchView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) :
  ViewSwitcher(context, attrs), ViewSwitcher.ViewFactory {

  private val mItemInfos: MutableList<ItemInfo> = ArrayList()
  private var currentId = -1
  private var mHandler: Handler? = null

  override fun makeView(): View {
    return inflate(context, R.layout.auto_switic_item, null)
  }

  /**
   * 间隔时间
   *
   * @param time
   */
  fun setTextStillTime(time: Long) {
    mHandler = SwitchHandler(this, time)
  }

  fun setAnimTime(duration: Long) {
    setFactory(this)
    val `in`: Animation = TranslateAnimation(0F, 0F, duration.toFloat(), 0F)
    `in`.duration = duration
    `in`.interpolator = AccelerateInterpolator()
    val out: Animation = TranslateAnimation(0F, 0F, 0F, (-duration).toFloat())
    out.duration = duration
    out.interpolator = AccelerateInterpolator()
    inAnimation = `in`
    outAnimation = out
  }

  fun setTextList(textList: List<String?>?) {
    if (textList == null || textList.isEmpty()) {
      return
    }
    mItemInfos.clear()
    var itemInfo: ItemInfo
    var i = 0
    val size = textList.size
    while (i < size) {
      itemInfo = ItemInfo()
      itemInfo.firstText = textList[i]
      if (i + 1 < size) {
        itemInfo.secondText = textList[i + 1]
      }
      mItemInfos.add(itemInfo)
      i += 2
    }
    currentId = -1
  }

  fun start() {
    mHandler!!.sendEmptyMessage(START_ANIM)
  }

  fun stop() {
    mHandler!!.sendEmptyMessage(STOP_ANIM)
  }

  private fun setupItemView(itemInfo: ItemInfo) {
    Log.d(TAG, "setupItemView() called with: itemInfo = [$itemInfo]")
    if (nextView !is LinearLayout) {
      return
    }
    val item = nextView as LinearLayout
    (item.getChildAt(0) as TextView).text = itemInfo.firstText
    if (itemInfo.secondText != null) {
      (item.getChildAt(1) as TextView).text = itemInfo.secondText
    }
  }

  class SwitchHandler(view: View, time: Long) : Handler() {

    private val mRefView: SoftReference<View> = SoftReference(view)
    private val mTime: Long = time

    override fun handleMessage(msg: Message) {
      val switchView = mRefView.get() as AutoSwitchView? ?: return
      when (msg.what) {
        START_ANIM -> {
          if (switchView.mItemInfos.size > 0) {
            switchView.currentId++
            switchView.setupItemView(switchView.mItemInfos[switchView.currentId % switchView.mItemInfos.size])
          }
          switchView.mHandler!!.sendEmptyMessageDelayed(START_ANIM, mTime)
        }
        STOP_ANIM -> switchView.mHandler!!.removeMessages(START_ANIM)
        else -> {
        }
      }
    }

  }

  private class ItemInfo {
    var firstText: String? = null
    var secondText: String? = null
  }

  companion object {
    private const val TAG = "AutoSwitchView"
    private const val START_ANIM = 1 shl 1
    private const val STOP_ANIM = 1 shl 2
  }
}