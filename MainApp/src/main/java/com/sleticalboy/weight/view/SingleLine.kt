package com.sleticalboy.weight.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorRes
import com.sleticalboy.learning.R

/**
 * Created on 18-3-1.
 *
 * @author leebin
 */
class SingleLine @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

  private val mPaint: Paint = Paint()
  private var circlePadding = 20f
  private var radius = 10f
  private var lineWidth = 4f
  private var mainColor = 0

  private fun readAttrs(context: Context, attrs: AttributeSet?) {
    if (attrs != null) {
      var a: TypedArray? = null
      try {
        a = context.obtainStyledAttributes(attrs, R.styleable.SingleLine)
        circlePadding = a.getDimension(R.styleable.SingleLine_circle_padding, 20f)
        radius = a.getInteger(R.styleable.SingleLine_circle_radius, 10).toFloat()
        lineWidth = a.getInteger(R.styleable.SingleLine_line_width, 4).toFloat()
        mainColor = a.getColor(R.styleable.SingleLine_line_color, DEF_MAIN_COLOR)
      } finally {
        a?.recycle()
      }
    }
  }

  override fun onDraw(canvas: Canvas) {
    mPaint.color = mainColor
    mPaint.strokeWidth = lineWidth
    mPaint.style = Paint.Style.STROKE
    val cx = width shr 1
    val cy = height shr 1
    canvas.drawLine(paddingLeft.toFloat(), cy.toFloat(), cx - circlePadding, cy.toFloat(), mPaint)
    mPaint.style = Paint.Style.FILL
    canvas.drawCircle(cx.toFloat(), cy.toFloat(), radius, mPaint)
    mPaint.style = Paint.Style.STROKE
    canvas.drawLine(
      cx + circlePadding,
      cy.toFloat(),
      cx * 2 - paddingRight.toFloat(),
      cy.toFloat(),
      mPaint
    )
  }

  fun setRadius(radius: Int) {
    this.radius = radius.toFloat()
    invalidate()
  }

  fun setCirclePadding(circlePadding: Int) {
    this.circlePadding = circlePadding.toFloat()
    invalidate()
  }

  fun setLineWidth(lineWidth: Int) {
    this.lineWidth = lineWidth.toFloat()
    invalidate()
  }

  fun setMainColor(@ColorRes mainColor: Int) {
    this.mainColor = resources.getColor(mainColor)
    invalidate()
  }

  companion object {
    private val DEF_MAIN_COLOR = Color.parseColor("#e5e5e5")
  }

  init {
    mPaint.isAntiAlias = true
    readAttrs(context, attrs)
  }
}