package com.binlee.emoji.span

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan

class EmojiSpan(d: Drawable?, source: String?) : ImageSpan(d!!, source!!) {

  private var bgColor = Color.TRANSPARENT

  override fun draw(
    canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int,
    paint: Paint
  ) {
    val fm = paint.fontMetricsInt
    val d = drawable
    canvas.save()
    // 计算y方向的位移
    val transY = (y + fm.descent + y + fm.ascent) / 2 - d.bounds.bottom / 2
    val oldColor = paint.color
    paint.color = bgColor
    canvas.drawRect(x, top.toFloat(), x + d.bounds.right, bottom.toFloat(), paint)
    // 绘制图片位移一段距离
    canvas.translate(x, transY.toFloat())
    paint.color = oldColor
    d.draw(canvas)
    canvas.restore()
  }

  override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: FontMetricsInt?): Int {
    val d = drawable
    val rect = d.bounds
    if (fm != null) {
      val fontMetricsInt = paint.fontMetricsInt
      val fontHeight = fontMetricsInt.bottom - fontMetricsInt.top
      val drawableHeight = rect.bottom - rect.top
      val top = drawableHeight / 2 - fontHeight / 4
      val bottom = drawableHeight / 2 + fontHeight / 4
      fm.ascent = -bottom
      fm.top = -bottom
      fm.bottom = top
      fm.descent = top
    }
    return rect.right
  }

  fun updateColor(color: Int) {
    bgColor = color
    drawable.invalidateSelf()
  }
}