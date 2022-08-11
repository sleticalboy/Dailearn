package com.binlee.emoji.span

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.text.style.ReplacementSpan

/**
 * Created on 19-7-29.
 *
 * @author leebin
 */
class CustomAtSpan(private val mSource: CharSequence) : ReplacementSpan() {

  override fun getSize(
    paint: Paint, text: CharSequence,
    start: Int, end: Int,
    fm: FontMetricsInt?
  ): Int {
    return paint.measureText(mSource, 0, mSource.length).toInt()
  }

  override fun draw(
    canvas: Canvas, text: CharSequence,
    start: Int, end: Int,
    x: Float, top: Int, y: Int, bottom: Int,
    paint: Paint
  ) {
    canvas.drawText(mSource, 0, mSource.length, x, y.toFloat(), paint)
  }
}