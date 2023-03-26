package com.binlee.learning.weight.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import com.binlee.learning.R

/**
 * Created on 18-3-1.
 *
 * @author leebin
 * @version 1.0
 * @description 绘图：Path 类练习使用
 */
class PathView @JvmOverloads constructor(
  context: Context?, attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
  private val mPaint: Paint = Paint()
  private val mPath: Path = Path()
  private val mOval: RectF = RectF()
  private var mBitmap: Bitmap? = null
  private var mShader: Shader? = null

  override fun onDraw(canvas: Canvas) {
    // 颜色填充
    canvas.drawColor(Color.parseColor("#88880000"))

    // 绘制一条线
    line(canvas)

    // 绘制一个正方形
    line2Rect(canvas)
    // 或者
    rect(canvas)

    // 绘制一个圆形
    circle(canvas)

    // 曲线
    bezierlLine(canvas)

    // 点
    point(canvas)

    // 画椭圆
    oval(canvas)

    // 圆角矩形
    roundRect(canvas)

    // 扇形 弧
    drawArc(canvas)

    // 自定义图形
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      drawHeart(canvas)
    }

    // 绘制文字
    drawText(canvas)

    // 绘制 Bitmap
    canvas.drawBitmap(mBitmap!!, 100f, 1400f, mPaint)
    mPaint.shader = mShader
    mPaint.style = Paint.Style.STROKE
    canvas.drawCircle(800f, 600f, 200f, mPaint)
  }

  private fun drawText(canvas: Canvas) {
    mPaint.color = Color.RED
    mPaint.textSize = 72f
    mPaint.style = Paint.Style.FILL_AND_STROKE
    mPaint.strokeWidth = 2f
    canvas.drawText("hello paint, path and canvas", 0f, 1080f, mPaint)
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  private fun drawHeart(canvas: Canvas) {
    mPath.reset()
    mPath.addArc(200f, 200f, 400f, 400f, -225f, 225f)
    mPath.arcTo(400f, 200f, 600f, 400f, -180f, 225f, false)
    mPath.lineTo(400f, 542f)
    canvas.drawPath(mPath, mPaint)
  }

  private fun drawArc(canvas: Canvas) {
    mOval.left += 200f
    mOval.top += 300f
    mOval.right -= 200f
    mOval.bottom += 300f
    mPaint.color = Color.parseColor("#99988812")
    mPaint.style = Paint.Style.FILL
    // mOval: 范围，100：起始，100：扫过的度数
    // userCenter：true 使用圆心，画出来就是扇形， false 画出来就是弧形
    canvas.drawArc(mOval, 100f, 100f, true, mPaint)
    mPaint.style = Paint.Style.STROKE
    mPaint.color = Color.MAGENTA
    canvas.drawArc(mOval, -100f, 180f, false, mPaint)
  }

  private fun roundRect(canvas: Canvas) {
    mPaint.color = Color.parseColor("#233298")
    mOval.left += 200f
    mOval.top += 300f
    mOval.right += 200f
    mOval.bottom += 300f
    canvas.drawRoundRect(mOval, 50f, 50f, mPaint)
  }

  private fun oval(canvas: Canvas) {
    mPaint.style = Paint.Style.STROKE
    mPaint.color = Color.parseColor("#982378")
    mOval[50f, 800f, 800f] = 1000f
    canvas.drawOval(mOval, mPaint)
  }

  private fun point(canvas: Canvas) {
    //        mPaint.release(); // 重置画笔
    mPaint.color = Color.BLACK
    mPaint.strokeWidth = 20f // 设置线条宽度
    mPaint.strokeCap = Paint.Cap.ROUND // 设置形状
    canvas.drawPoint(400f, 400f, mPaint)
  }

  private fun bezierlLine(canvas: Canvas) {
    mPaint.color = Color.YELLOW
    mPath.reset()
    mPath.moveTo(0f, 300f) // 起点
    mPath.quadTo(150f, 750f, 300f, 300f) // 第一个点：控制点，第二个点：终点
    canvas.drawPath(mPath, mPaint)
  }

  private fun circle(canvas: Canvas) {
    mPath.reset()
    mPaint.color = Color.GREEN
    canvas.drawCircle(800f, 175f, 125f, mPaint)
  }

  private fun rect(canvas: Canvas) {
    mPath.reset()
    mPaint.color = Color.BLUE
    canvas.drawRect(350f, 0f, 610f, 300f, mPaint)
  }

  private fun line2Rect(canvas: Canvas) {
    mPaint.color = Color.RED
    mPath.reset()
    mPath.moveTo(0f, 0f)
    mPath.lineTo(0f, 0f)
    mPath.lineTo(300f, 0f)
    mPath.lineTo(300f, 300f)
    mPath.lineTo(0f, 300f)
    canvas.drawPath(mPath, mPaint)
  }

  private fun line(canvas: Canvas) {
    mPaint.color = Color.YELLOW
    mPaint.style = Paint.Style.STROKE
    mPath.reset()
    mPath.moveTo(0f, 600f)
    mPath.lineTo(1000f, 600f) // 绝对坐标
    mPath.rLineTo(-200f, -300f) // 相对于上一个坐标点坐标
    mOval[400f, 400f, 900f] = 900f
    // forceMoveTo: false 连笔，true，抬起画笔
    mPath.arcTo(mOval, -90f, 270f, false)
    canvas.drawPath(mPath, mPaint)
  }

  init {
    mPaint.style = Paint.Style.FILL_AND_STROKE // 画线，填充，默认是填充
    mPaint.isAntiAlias = true
    mPaint.strokeWidth = 4f
    mBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_test_drawable)
    mShader = BitmapShader(mBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
  }
}