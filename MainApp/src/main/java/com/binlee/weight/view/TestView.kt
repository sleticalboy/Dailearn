package com.sleticalboy.weight.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View

class TestView @JvmOverloads constructor(
  context: Context?,
  attrs: AttributeSet? = null,
  defStyle: Int = 0
) : View(context, attrs, defStyle) {

  private var mNumber = 1
  private val mPaint: Paint = Paint()

  override fun onDraw(canvas: Canvas) {
    Log.d(TAG, "onDraw() called number: $mNumber")
    for (i in 0 until mNumber) {
      var x = 60 * i.toFloat()
      if (x > width) {
        x -= width
      }
      canvas.drawText(i.toString(), x, 30 * i.toFloat(), mPaint)
    }
  }

  fun update() {
    mNumber++
    invalidate()
  }

  companion object {
    private const val TAG = "TestView"
  }

  init {
    mPaint.color = Color.RED
    mPaint.textSize = 32f
    mPaint.isAntiAlias = true
  }
}