package com.binlee.weight.xrefresh

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.Scroller
import com.binlee.util.UiUtils.dp2px
import com.binlee.weight.xrefresh.interfaces.IFooterView
import com.binlee.weight.xrefresh.interfaces.IHeaderView

/**
 * Created on 18-2-2.
 *
 * @author leebin
 * @version 1.0
 * @description 提供下拉刷新和上拉加载的 layout
 */
class XRefreshLayout @JvmOverloads constructor(
  context: Context?, attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : FrameLayout(context!!, attrs, defStyleAttr) {

  //----------- 外部可以控制的属性------------------
  var isCanPullDown = true // 是否可以下拉
  var isCanPullUp = true // 是否可以上拉
  private var mHeaderHeight = DEFAULT_HEADER_HEIGHT // header 高度
  private var mFooterHeight = DEFAULT_FOOTER_HEIGHT // footer 高度
  private val mMaxHeaderHeight // header 最大高度
      = 0
  private val mMaxFooterHeight // footer 最大高度
      = 0
  private var mHeaderView // header 视图
      : IHeaderView? = null
  private var mFooterView // footer 视图
      : IFooterView? = null
  var mLoadMoreListener // 上拉/下拉加载监听
      : OnLoadMoreListener? = null

  //----------- 外部可以控制的属性------------------

  //----------- 内部使用的属性------------------
  private var mTouchSlop = 0
  private var mScroller: Scroller? = null
  private var mTarget // the target of the gesture
      : View? = null
  private var mTouchY = 0f
  private var mLastY = 0f
  private var mIsPullUp = false
  private var mIsPullDown = false
  private fun init() {
    mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    mScroller = Scroller(context, DecelerateInterpolator())
    Log.d(TAG, "mTouchSlop:$mTouchSlop")
  }

  private fun initView() {
    if (isInEditMode) {
      return
    }
  }

  private fun initFromAttrs(attrs: AttributeSet?) {
    if (attrs == null) {
      return
    }
    var a: TypedArray? = null
    try {
      a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS)
      a.getInt(LAYOUT_ATTRS[0], 0)
      a.getInt(LAYOUT_ATTRS[1], 0)
      a.getInt(LAYOUT_ATTRS[2], 0)
      a.getInt(LAYOUT_ATTRS[3], 0)
    } finally {
      a?.recycle()
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    addHeader()
    addFooter()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    reset()
  }

  private fun reset() {
    // release views
  }

  private fun addHeader() {
    if (mHeaderView == null) {
      mHeaderView = HeaderView(context)
    }
    val header = mHeaderView!!.get()
    val p = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
    header.layoutParams = p
    if (header.parent != null) {
      (header.parent as ViewGroup).removeView(header)
    }
    addView(header, 0)
  }

  private fun addFooter() {
    if (mFooterView == null) {
      mFooterView = FooterView(context)
    }
    val footer = mFooterView!!.get()
    val p = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
    p.gravity = Gravity.BOTTOM
    footer.layoutParams = p
    if (footer.parent != null) {
      (footer.parent as ViewGroup).removeView(footer)
    }
    addView(footer)
  }

  private fun ensureTarget() {
    if (mTarget == null) {
      for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (child !is IFooterView && child !is IHeaderView) {
          mTarget = child
          break
        }
      }
    }
    Log.d(TAG, "mTarget:$mTarget")
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    Log.d(
      TAG, "onMeasure() called with: widthMeasureSpec = [" + widthMeasureSpec
          + "], heightMeasureSpec = [" + heightMeasureSpec + "]"
    )
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    if (mTarget == null) {
      ensureTarget()
    }
    if (mTarget == null) {
      return
    }
    mTarget!!.measure(
      MeasureSpec.makeMeasureSpec(
        measuredWidth - paddingLeft - paddingRight, MeasureSpec.EXACTLY
      ),
      MeasureSpec.makeMeasureSpec(
        measuredHeight - paddingTop - paddingBottom, MeasureSpec.EXACTLY
      )
    )
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    Log.d(
      TAG, "onLayout() called with: changed = [" + changed + "], l = [" + l
          + "], t = [" + t + "], r = [" + r + "], b = [" + b + "]"
    )
    super.onLayout(changed, l, t, r, b)

    // -----------header---------
    // left--------top-------- right
    //     |                 |
    //     |                 |
    //     |                 |
    //     |                 |
    // left------bottom------- right
    // -----------footer--------
  }

  override fun onFinishInflate() {
    Log.d(TAG, "onFinishInflate() called")
    super.onFinishInflate()
    check(childCount == 1) { "child view must be only one" }
    ensureTarget()
  }

  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    /*if (onInterceptTouchEvent(ev)) { // 如果当前 view 拦截了事件
        return onTouchEvent(ev); // 返回当前 view 对事件的处理结果
    } else { // 如果当前 view 没有拦截事件
        return mTarget.dispatchTouchEvent(ev); // 返回 child 对事件的分发结果
    }*/
    return if (onInterceptTouchEvent(ev)) {
      onTouchEvent(ev)
    } else {
      mTarget!!.dispatchTouchEvent(ev)
    }
  }

  override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    when (ev.action) {
      MotionEvent.ACTION_DOWN -> {
        mTouchY = ev.y
        Log.d(TAG, "onInterceptTouchEvent: action down")
      }
      MotionEvent.ACTION_MOVE -> {
        val deltaY = ev.y - mTouchY
        if (deltaY >= 0 && deltaY > mTouchSlop) {
          Log.d(TAG, "intercept pull down event")
          mIsPullDown = true
          //                    return true;
        } else if (deltaY < 0 && -deltaY > mTouchSlop) {
          Log.d(TAG, "intercept pull up event")
          mIsPullUp = true
          //                    return true;
        }
      }
      MotionEvent.ACTION_UP -> {
        mLastY = ev.y
        Log.d(TAG, "onInterceptTouchEvent: action up")
      }
      MotionEvent.ACTION_CANCEL -> {
        mLastY = ev.y
        Log.d(TAG, "onInterceptTouchEvent: action cancel")
      }
    }
    return super.onInterceptTouchEvent(ev)
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        mTouchY = event.y
        Log.d(TAG, "onTouchEvent: action down")
      }
      MotionEvent.ACTION_MOVE -> {
        val deltaY = y - mTouchY
        if (deltaY >= 0 && deltaY > mTouchSlop) {
          Log.d(TAG, "pull down ....")
          if (mIsPullDown) {
            //                        return true;
          } else {
            //                        return mTarget.onTouchEvent(event);
          }
        } else if (deltaY < 0 && -deltaY > mTouchSlop) {
          Log.d(TAG, "pull up ....")
          if (mIsPullUp) {
            //                        return true;
          } else {
            //                        return mTarget.onTouchEvent(event);
          }
        }
      }
      MotionEvent.ACTION_UP -> {
        mLastY = event.y
        Log.d(TAG, "onTouchEvent: action up")
      }
      MotionEvent.ACTION_CANCEL -> {
        mLastY = event.y
        Log.d(TAG, "onTouchEvent: action cancel")
      }
    }
    return super.onTouchEvent(event)
  }

  private fun createTranslationYAnim(state: Int, from: Int, to: Int) {
    Log.d(TAG, "create animation state: $state, from: $from to $to")
    val animator = ValueAnimator.ofInt(from, to)
    animator.duration = ANIM_TIME.toLong()
    animator.addUpdateListener { animation ->
      val value = animation.animatedValue as Int
      Log.d(TAG, "value:$value")
      requestLayout()
    }
    animator.start()
  }

  override fun computeScroll() {
    super.computeScroll()
    if (mScroller!!.computeScrollOffset()) {
      scrollTo(mScroller!!.currX, mScroller!!.currY)
      postInvalidate()
    }
  }

  private fun smoothScrollTo(destX: Int, destY: Int) {
    Log.d(TAG, "smoothScrollTo() destX = $destX, destY = $destY")
    val scrollX = scrollX
    val deltaX = destX - scrollX
    mScroller!!.startScroll(scrollX, 0, deltaX, 500)
  }

  /**
   * @return Return it is possible for the child view of this layout to
   * scroll up. Override this if the child view is a custom view.
   */
  private fun canChildScroll(direction: Int): Boolean {
    require(direction != 0) { "direction only can be negative or positive value: $direction" }
    if (mTarget == null) {
      ensureTarget()
    }
    if (mTarget == null) {
      return false
    }
    return mTarget!!.canScrollVertically(direction)
  }

  fun canChildScrollDown(): Boolean {
    return canChildScroll(1)
  }

  fun canChildScrollUp(): Boolean {
    return canChildScroll(-1)
  }

  /**
   * 上拉/下拉加载更多监听
   */
  interface OnLoadMoreListener {
    /**
     * 上拉加载时回调
     */
    fun onPullUp()

    /**
     * 下拉加载时回调
     */
    fun onPullDown()
  }

  fun setOnLoadMoreListener(loadMoreListener: OnLoadMoreListener?) {
    mLoadMoreListener = loadMoreListener
  }

  fun setHeaderView(headerView: IHeaderView?) {
    mHeaderView = headerView
  }

  fun setFooterView(footerView: IFooterView?) {
    mFooterView = footerView
  }

  fun setHeaderHeight(headerHeight: Int) {
    mHeaderHeight = dp2px(context, headerHeight.toFloat())
  }

  fun setFooterHeight(footerHeight: Int) {
    mFooterHeight = dp2px(context, footerHeight.toFloat())
  }

  companion object {
    private const val TAG = "XRefreshLayout"
    private const val ANIM_TIME = 180
    private const val DEFAULT_HEADER_HEIGHT = 60
    private const val DEFAULT_FOOTER_HEIGHT = 60
    const val STATE_REFRESH = 0x001
    const val STATE_LOAD = 0x002

    //----------- 内部使用的属性------------------
    private val LAYOUT_ATTRS = intArrayOf(
      android.R.attr.paddingLeft, android.R.attr.paddingRight,
      android.R.attr.paddingTop, android.R.attr.paddingBottom
    )
  }

  init {
    init()
    initView()
    initFromAttrs(attrs)
  }
}