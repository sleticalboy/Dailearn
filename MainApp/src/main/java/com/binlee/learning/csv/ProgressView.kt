package com.binlee.learning.csv

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.binlee.learning.R

class ProgressView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
  private annotation class Status {
    companion object {
      var STATUS_IDLE = 0x10
      var STATUS_LOADING = 0x11
      var STATUS_SUCCESS = 0x12
    }
  }

  @Status
  private var mStatus = Status.STATUS_IDLE
  private val progressWidth = 8f
  private var progressRadius = 0f

  //开始角度
  private var startAngle = -90

  //最小角度
  private var minAngle = -90

  //扫描角度
  private var sweepAngle = 120

  //当前角度
  private var curAngle = 0

  //追踪Path的坐标
  private var mPathMeasure: PathMeasure? = null

  //画圆的Path
  private var mPathCircle: Path? = null

  // 截取 PathMeasure 中的 path
  private var mPathCircleDst: Path? = null
  private var successPath: Path? = null
  private var circleValue = 0f
  private var successValue = 0f
  private var mPaint: Paint? = null
  private lateinit var mValueAnimator: ValueAnimator
  private var mRectF: RectF? = null
  private var mLoadingColor = 0
  private var mFinishColor = 0
  private fun initPaint() {
    mPaint = Paint()
    mPaint!!.style = Paint.Style.STROKE
    mPaint!!.strokeWidth = progressWidth
    mPaint!!.isDither = true
    mPaint!!.isAntiAlias = true
    mPaint!!.strokeCap = Paint.Cap.ROUND
    mPaint!!.strokeJoin = Paint.Join.ROUND
  }

  private fun initPath() {
    mPathCircle = Path()
    mPathMeasure = PathMeasure()
    mPathCircleDst = Path()
    successPath = Path()
  }

  private fun initAnim() {
    mValueAnimator = ValueAnimator.ofFloat(0f, 1f)
    mValueAnimator.addUpdateListener { animation: ValueAnimator ->
      circleValue = animation.animatedValue as Float
      invalidate()
    }
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val width: Int
    val height: Int
    var mode = MeasureSpec.getMode(widthMeasureSpec)
    var size = MeasureSpec.getSize(widthMeasureSpec)
    width = if (mode == MeasureSpec.EXACTLY) {
      size
    } else {
      //直径
      (2 * progressRadius + progressWidth + paddingLeft + paddingRight).toInt()
    }
    mode = MeasureSpec.getMode(heightMeasureSpec)
    size = MeasureSpec.getSize(heightMeasureSpec)
    height = if (mode == MeasureSpec.EXACTLY) {
      size
    } else {
      (2 * progressRadius + progressWidth + paddingTop + paddingBottom).toInt()
    }
    setMeasuredDimension(width, height)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (mStatus == Status.STATUS_LOADING) {
      canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
      if (startAngle == minAngle) {
        sweepAngle += 6
      }
      if (sweepAngle >= 300 || startAngle > minAngle) {
        startAngle += 6
        if (sweepAngle > 20) {
          //保持结束位置不变
          sweepAngle -= 6
        }
      }
      if (startAngle > minAngle + 300) {
        startAngle %= 360
        minAngle = startAngle
        sweepAngle = 20
      }
      canvas.rotate(
        4.let { curAngle += it; curAngle }.toFloat(),
        progressRadius,
        progressRadius
      )
      // 定义的圆弧的形状和大小的界限
      if (mRectF == null) {
        mRectF = RectF()
      }
      mRectF!![0f, 0f, progressRadius * 2] = progressRadius * 2
      mPaint!!.style = Paint.Style.STROKE
      mPaint!!.strokeWidth = progressWidth
      mPaint!!.color = mLoadingColor
      canvas.drawArc(mRectF!!, startAngle.toFloat(), sweepAngle.toFloat(), false, mPaint!!)
      invalidate()
    } else if (mStatus == Status.STATUS_SUCCESS) {

      // canvas.translate(0, 0);
      // mPathCircle.addCircle(getWidth() >> 1, getWidth() >> 1, progressRadius, Path.Direction.CW);
      // mPathMeasure.setPath(mPathCircle, false);
      // 截取 path 并保存到 mPathCircleDst 中
      // mPathMeasure.getSegment(0, circleValue * mPathMeasure.getLength(), mPathCircleDst, true);
      // canvas.drawPath(mPathCircleDst, mPaint);

      // 画圆形背景
      mPaint!!.color = mFinishColor
      mPaint!!.style = Paint.Style.FILL_AND_STROKE
      canvas.drawCircle(
        (width shr 1).toFloat(),
        (width shr 1).toFloat(),
        progressRadius,
        mPaint!!
      )

      // 表示圆画完了,可以画钩了
      // if (circleValue == 1) {
      if (circleValue >= 0) {
        // 在 progressRadius = 70 的标准上计算的
        successPath!!.moveTo(50 - offset(), 70 - offset())
        successPath!!.lineTo(71 - offset(), 90 - offset())
        successPath!!.lineTo(108 - offset(), 55 - offset())
        //
        // mPathMeasure.nextContour();
        // mPathMeasure.setPath(successPath, false);
        // mPathMeasure.getSegment(0, successValue * mPathMeasure.getLength(), mPathCircleDst, true);
        mPaint!!.color = ContextCompat.getColor(context, android.R.color.white)
        mPaint!!.style = Paint.Style.STROKE
        mPaint!!.strokeWidth = progressWidth
        // canvas.drawPath(mPathCircleDst, mPaint);
        canvas.drawPath(successPath!!, mPaint!!)
      }
    }
  }

  private fun offset(): Float {
    return progressRadius / 70f * (70 - progressRadius)
  }

  private fun clearState() {
    startAngle = -90
    minAngle = -90
    sweepAngle = 120
    curAngle = 0
    circleValue = 0f
    successValue = 0f
    initPath()
  }

  private fun setStatus(@Status status: Int) {
    mStatus = status
  }

  fun loadLoading() {
    clearState()
    setStatus(Status.STATUS_LOADING)
    invalidate()
  }

  fun loadSuccess() {
    setStatus(Status.STATUS_SUCCESS)
    // startSuccessAnim();
  }

  private fun startSuccessAnim() {
    val success = ValueAnimator.ofFloat(0f, 1.0f)
    success.addUpdateListener { animation: ValueAnimator ->
      successValue = animation.animatedValue as Float
      invalidate()
    }
    val animatorSet = AnimatorSet()
    animatorSet.play(success).after(mValueAnimator)
    animatorSet.duration = 500
    animatorSet.start()
  }

  init {
    var ta: TypedArray? = null
    try {
      ta = context.obtainStyledAttributes(attrs, R.styleable.ProgressView)
      progressRadius =
        ta.getDimensionPixelSize(R.styleable.ProgressView_progress_size, 70).toFloat()
      mLoadingColor = ta.getColor(
        R.styleable.ProgressView_loading_color,
        ContextCompat.getColor(context, R.color.progress_loading)
      )
      mFinishColor = ta.getColor(
        R.styleable.ProgressView_finish_color,
        ContextCompat.getColor(context, R.color.progress_finish)
      )
    } finally {
      ta?.recycle()
    }
    initPaint()
    initPath()
    initAnim()
  }
}