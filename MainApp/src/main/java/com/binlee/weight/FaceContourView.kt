package com.sleticalboy.weight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatImageView

/**
 * Created on 18-3-1.
 *
 * @author leebin
 * @version 1.0
 * @description
 */
class FaceContourView @JvmOverloads constructor(
  context: Context?, attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AppCompatImageView(context!!, attrs, defStyleAttr) {

  private var mPaint: Paint? = null
  private var mPath: Path? = null
  private var mScreenWidthCenter = 0f

  override fun onDraw(canvas: Canvas) {
    drawBeforeSuper(canvas)
    super.onDraw(canvas)
    drawFaceContour(canvas)
  }

  private fun drawBeforeSuper(canvas: Canvas) {
    canvas.drawColor(Color.parseColor("#5c5c5c"))

    // 画脸部
    // 起点 天灵
    mPath!!.moveTo(mScreenWidthCenter, 200f)
    // 左上四分之一 绝对移动：第一个点是控制点，第二个点是终点
    mPath!!.quadTo(mScreenWidthCenter / 2, 200f, mScreenWidthCenter / 2, 400f) // 左耳
    // 左下四分之一 相对移动：第一个点是控制点，第二个点是终点
    mPath!!.rQuadTo(0f, 500f, mScreenWidthCenter / 2, 500f) // 下巴
    // 右下四分之一 相对移动
    mPath!!.rQuadTo(mScreenWidthCenter / 2, 0f, mScreenWidthCenter / 2, -500f) // 右耳
    // 右上四分之一 相对移动
    mPath!!.rQuadTo(0f, -200f, -mScreenWidthCenter / 2, -200f) // 天灵
    mPath!!.close()
    mPaint!!.color = Color.TRANSPARENT
    mPaint!!.style = Paint.Style.FILL
    mPath!!.fillType = Path.FillType.EVEN_ODD
    canvas.drawPath(mPath!!, mPaint!!)
  }

  private fun drawFaceContour(canvas: Canvas) {
    // 画脸部边缘轮廓
    mPath!!.reset()
    // 起点 天灵
    mPath!!.moveTo(mScreenWidthCenter, 200f)
    // 左上四分之一 绝对移动：第一个点是控制点，第二个点是终点
    mPath!!.quadTo(mScreenWidthCenter / 2, 200f, mScreenWidthCenter / 2, 400f) // 左耳
    // 左下四分之一 相对移动：第一个点是控制点，第二个点是终点
    mPath!!.rQuadTo(0f, 500f, mScreenWidthCenter / 2, 500f) // 下巴
    // 右下四分之一 相对移动
    mPath!!.rQuadTo(mScreenWidthCenter / 2, 0f, mScreenWidthCenter / 2, -500f) // 右耳
    // 右上四分之一 相对移动
    mPath!!.rQuadTo(0f, -200f, -mScreenWidthCenter / 2, -200f) // 天灵
    mPaint!!.color = Color.BLUE
    mPaint!!.style = Paint.Style.STROKE
    canvas.drawPath(mPath!!, mPaint!!)
  }

  init {
    mPaint = Paint()
    mPaint!!.color = Color.BLACK
    mPaint!!.style = Paint.Style.STROKE
    mPaint!!.flags = Paint.ANTI_ALIAS_FLAG
    mPaint!!.strokeWidth = 4f
    mPath = Path()
    val wm = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val dm = DisplayMetrics()
    wm.defaultDisplay.getMetrics(dm)
    Log.d("FaceContourView", "dm.density:" + dm.density) // 3
    Log.d("FaceContourView", "dm.densityDpi:" + dm.densityDpi) // 480
    Log.d("FaceContourView", "dm.widthPixels:" + dm.widthPixels) // 1080
    Log.d("FaceContourView", "dm.heightPixels:" + dm.heightPixels) // 1920
    mScreenWidthCenter = dm.widthPixels / 2.toFloat()
  }
}