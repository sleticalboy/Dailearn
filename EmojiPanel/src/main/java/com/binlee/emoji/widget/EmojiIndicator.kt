package com.binlee.emoji.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.tan

/**
 * Created on 19-7-19.
 *
 *
 * 7-22 完工
 *
 * @author leebin
 */
class EmojiIndicator @JvmOverloads constructor(
  context: Context?, attrs: AttributeSet? = null,
  defStyle: Int = 0
) : View(context, attrs, defStyle) {

  /**
   * 动态指示器圆心位置
   */
  private var mCx = 0f
  private var mCy = 0f
  private var mStartX = 0f

  /**
   * 指示器间距
   */
  private var mSectionSize = DOT_SIZE * 6
  private val mPaint = Paint()

  /**
   * 页数
   */
  private var mCount = 0

  /**
   * 是否有最近使用表情
   */
  private var mHasRecently = false
  private var mAttachedVp: ViewPager? = null

  override fun onDraw(canvas: Canvas) {
    if (mCount == 0) {
      return
    }
    checkInit()
    if (mCount >= SHOW_SEEK_BAR_PAGE_LIMIT) {
      drawSeekBar(canvas)
    } else {
      drawIndicator(canvas)
    }
  }

  private fun drawSeekBar(canvas: Canvas) {
    // 绘制线
    mPaint.color = STATIC_DOT_COLOR
    mPaint.style = Paint.Style.STROKE
    mPaint.strokeWidth = DOT_SIZE / 2
    canvas.drawLine(mStartX, height / 2.toFloat(), width - mStartX, height / 2.toFloat(), mPaint)
    // 绘制活动 indicator 间距，尺寸（比静态的大3个像素），颜色;（随着 ViewPager 滑动, 位置实时更新）
    // 0---1---2---3---④---⑤---6---7---8---9
    // 绘制表盘
    mPaint.color = DOT_COLOR
    mPaint.style = Paint.Style.FILL
    mCx = max(mCx, mStartX)
    canvas.drawCircle(mCx, mCy, DOT_SIZE + 3, mPaint)
    // 不知为何有这么一种 case： mCx: 128.00006, mStartX: 128.0， 导致表盘指针画不出来
    // 所以：Math.abs(mCx - mStartX) <= 0.1
    // if (mHasRecently && mCx == mStartX) {
    if (mHasRecently && abs(mCx - mStartX) <= 0.1) {
      // 绘制表盘指针
      drawDialPointer(canvas)
    }
  }

  private fun drawIndicator(canvas: Canvas) {
    // 绘制起点圆心 cx = (View 宽度 - (个数 × (静态尺寸 * 2 + 间距) - 间距)) / 2 + 尺寸
    // 绘制起点圆心 cy = View 高度 / 2
    // 〇---〇---〇---〇---〇---〇---〇---〇---〇---〇
    // 静止 indicator 个数，间距，尺寸，颜色（需要考虑最近使用表情）
    for (i in 0 until mCount) {
      if (i == 0 && mHasRecently) {
        // 绘制最近使用标识：绘制表盘
        mPaint.color = STATIC_DOT_COLOR
        canvas.drawCircle(mStartX, mCy, DOT_SIZE + 3, mPaint)
        // 绘制表盘指针
        drawDialPointer(canvas)
      } else {
        mPaint.color = STATIC_DOT_COLOR
        mPaint.style = Paint.Style.FILL
        canvas.drawCircle(mStartX + i * mSectionSize, mCy, DOT_SIZE, mPaint)
      }
    }

    // 绘制活动 indicator 间距，尺寸（比静态的大3个像素），颜色;（随着 ViewPager 滑动, 位置实时更新）
    mPaint.color = DOT_COLOR
    canvas.drawCircle(if (mCx <= DOT_SIZE) mStartX else mCx, mCy, DOT_SIZE + 3, mPaint)
  }

  private fun drawDialPointer(canvas: Canvas) {
    mPaint.color = Color.WHITE
    mPaint.style = Paint.Style.STROKE
    mPaint.strokeWidth = DOT_SIZE / 4
    canvas.drawLine(mStartX, mCy - DOT_SIZE, mStartX, mCy, mPaint)
    val deltaX = (tan(Math.PI / 6) * DOT_SIZE).toFloat()
    val deltaY = (sin(Math.PI / 6) * DOT_SIZE).toFloat()
    canvas.drawLine(mStartX, mCy, mStartX + deltaX, mCy + deltaY, mPaint)
  }

  private fun checkInit() {
    if (mStartX != 0f) {
      return
    }
    mCy = height * 1f / 2
    if (mCount >= SHOW_SEEK_BAR_PAGE_LIMIT) {
      mStartX = DOT_SPACING * 4
      val endX = width - mStartX
      mSectionSize = (endX - mStartX) / (mCount - 1)
    } else {
      mSectionSize = DOT_SIZE * 6
      mStartX = (width - (mCount * mSectionSize - DOT_SPACING)) / 2f + DOT_SIZE
    }
  }

  fun attachViewPager(vp: ViewPager, hasRecently: Boolean) {
    if (vp.adapter == null) {
      return
    }
    mCount = vp.adapter!!.count
    mHasRecently = hasRecently
    mAttachedVp = vp
    // 分页指示器与 ViewPager 联动
    vp.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
      override fun onPageScrolled(
        position: Int, positionOffset: Float,
        positionOffsetPixels: Int
      ) {
        mCx = mStartX + (position + positionOffset) * mSectionSize
        invalidate()
      }

      override fun onPageSelected(position: Int) {
        mCx = mStartX + position * mSectionSize
        invalidate()
      }
    })
    // 显示上次退出时的页面，但是再次进入时初始值还没初始化话，所以用了 post
    post {
      checkInit()
      mCx = mStartX + vp.currentItem * mSectionSize
      invalidate()
    }
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    val isValid = isValidTouch(event.x, event.y)
    if (mCount <= 1 || !isValid) {
      return false
    }
    when (event.action) {
      MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> setClosestItem(event.x, event.y)
    }
    return true
  }

  private fun setClosestItem(x: Float, y: Float) {
    if (!isValidTouch(x, y)) {
      return
    }
    val position = pointToIndex(x)
    if (position == -1) {
      return
    }
    mAttachedVp!!.currentItem = position
  }

  private fun isValidTouch(x: Float, y: Float): Boolean {
    // 边界值： 左 右 （左右范围各增大3个指示器尺寸大小）
    val l = mStartX - DOT_SIZE * 3
    val r = width - l
    // 边界值： 上 下
    val t = 0f
    val b = height.toFloat()
    Log.d(TAG, "isValidTouch: {l: $l, t: $t, r: $r, b: $b, x: $x, y: $y}")
    return x > l && x < r && y > t && y < b
  }

  private fun pointToIndex(x: Float): Int {
    // 触摸点落在哪个指示器范围内就返回哪个指示器的索引
    var index = -1
    // 校正值：
    val adjustValue = mSectionSize / 2
    for (i in 0 until mCount) {
      // 边界值： 左 右 （左右范围各增大3个指示器尺寸大小）
      val pxl = mStartX + i * mSectionSize - adjustValue
      val pxr = pxl + adjustValue * 2
      // 边界值：上 下 暂时先不考虑
      // final float pyt = mCy - DOT_SIZE;
      // final float pyb = mCy + DOT_SIZE;
      if (x > pxl && x < pxr /*&& y > pyt && y < pyb*/) {
        index = i
        Log.d(TAG, "pointToIndex() index: $i, {l: $pxl, x: $x, r: $pxr}")
        break
      }
    }
    Log.d(TAG, "finally we returned: $index, x: $x")
    return index
  }

  override fun setOnClickListener(l: OnClickListener?) {
    // empty implementation
  }

  override fun setOnLongClickListener(l: OnLongClickListener?) {
    // empty implementation
  }

  companion object {
    private const val TAG = "EmojiIndicator"

    /**
     * 当页数超过此限制时指示器切换成 SeekBar 样式
     */
    private const val SHOW_SEEK_BAR_PAGE_LIMIT = 10

    /**
     * 指示器尺寸，即圆的半径
     */
    private const val DOT_SIZE = 8f

    /**
     * 指示器之间间距
     */
    private const val DOT_SPACING = DOT_SIZE * 4

    /**
     * 静态指示器颜色
     */
    private val STATIC_DOT_COLOR = Color.parseColor("#ffa8a8a8")

    /**
     * 动态指示器颜色
     */
    private val DOT_COLOR = Color.parseColor("#ff646464")
  }

  init {
    mPaint.isAntiAlias = true
    mPaint.style = Paint.Style.FILL
  }
}