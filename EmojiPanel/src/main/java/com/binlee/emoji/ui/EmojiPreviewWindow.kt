package com.binlee.emoji.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import com.binlee.emoji.ImageAdapter
import com.binlee.emoji.R
import com.binlee.emoji.helper.UrlHelper
import kotlin.math.max

/**
 * Created on 19-7-21.
 *
 * @author leebin
 */
internal class EmojiPreviewWindow(context: Context?) : PopupWindow(context) {

  private val mImage: ImageView
  private val mRight: Int
  private val mLeft: Int

  private fun updateInternal(params: Params) {
    width = max(params.width, params.height)
    height = width
    val lp = mImage.layoutParams
    lp.height = params.size
    lp.width = lp.height
    mImage.layoutParams = lp
    ImageAdapter.engine().show(UrlHelper.inspectUrl(params.url), mImage)
    var resId = R.drawable.mx_emoji_preview_fg_center
    if (params.position % 4 == 0) {
      resId = R.drawable.mx_emoji_preview_fg_left
    } else if ((params.position + 1) % 4 == 0) {
      resId = R.drawable.mx_emoji_preview_fg_right
    }
    mImage.setBackgroundResource(resId)
  }

  fun show(anchor: View?, params: Params) {
    dismiss()
    updateInternal(params)
    var x = params.loc[0]
    if (x <= mLeft) {
      x = mLeft
    } else if (x + width >= mRight) {
      x = mRight - width
    }
    val y = params.loc[1] - height
    showAtLocation(anchor, Gravity.NO_GRAVITY, if (x <= 0) 0 else x, y)
  }

  class Params {
    var position = 0

    // 表情尺寸
    var size = 0

    // 窗口宽高
    var width = 0
    var height = 0
    var text: String? = null
    var url: String? = null

    // 预览窗口显示位置
    val loc = IntArray(2)
  }

  private class BubbleDrawable : Drawable() {
    override fun draw(canvas: Canvas) {
      canvas.drawColor(Color.BLUE)
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.TRANSPARENT", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int {
      return PixelFormat.TRANSPARENT
    }
  }

  init {
    isClippingEnabled = false
    val content = View.inflate(context, R.layout.emoji_preview_layout, null)
    mImage = content.findViewById(R.id.emojiIcon)
    mImage.cropToPadding = true
    mImage.setPadding(32, 32, 32, 32)
    val lp = mImage.layoutParams as ConstraintLayout.LayoutParams
    lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
    mImage.layoutParams = lp
    content.findViewById<View>(R.id.emojiName).visibility = View.GONE
    content.isClickable = false
    content.isLongClickable = false
    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    content.setBackgroundColor(Color.TRANSPARENT)
    contentView = content
    mLeft = 16
    mRight = content.resources.displayMetrics.widthPixels - 16
  }
}