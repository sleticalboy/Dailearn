package com.binlee.weight.xrecycler.decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * Created on 18-2-6.
 *
 * @author leebin
 * @version 1.0
 * @description RecyclerView Item 的分割线
 */
class SimpleDecoration(private val mContext: Context, orientation: Int) : ItemDecoration() {

  // 分割线
  private var mDivider: Drawable? = null

  // 方向
  private var mOrientation = 0

  // 边界 left top right bottom
  private val mBounds = Rect()

  /**
   * Draw any appropriate decorations into the Canvas supplied to the RecyclerView.
   * Any content drawn by this method will be drawn before the item views are drawn,
   * and will thus appear underneath the views.
   *
   * @param canvas Canvas to draw into
   * @param parent RecyclerView this ItemDecoration is drawing into
   * @param state  The current state of RecyclerView
   */
  override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
    if (parent.layoutManager == null || mDivider == null) {
      return
    }
    if (mOrientation == VERTICAL) {
      drawVertical(canvas, parent)
    } else {
      drawHorizontal(canvas, parent)
    }
  }

  private fun drawHorizontal(canvas: Canvas, parent: RecyclerView) {
    canvas.save()
    val top: Int
    val bottom: Int
    if (parent.clipToPadding) { // 判断是否设置了 layout_margin 属性
      top = parent.paddingTop
      bottom = parent.height - parent.paddingBottom
      canvas.clipRect(
        parent.paddingLeft, top,
        parent.width - parent.paddingRight, bottom
      )
    } else {
      top = 0
      bottom = parent.height
    }
    val childCount = parent.childCount
    for (i in 0 until childCount) {
      val child = parent.getChildAt(i)
      parent.getDecoratedBoundsWithMargins(child, mBounds)
      val right = mBounds.right + Math.round(child.translationX)
      val left = right - mDivider!!.intrinsicWidth
      mDivider!!.setBounds(left, top, right, bottom)
      mDivider!!.draw(canvas)
    }
    canvas.restore()
  }

  private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
    canvas.save()
    val left: Int
    val right: Int
    if (parent.clipToPadding) { // 判断是否设置了 layout_margin 属性
      left = parent.paddingLeft
      right = parent.width - parent.paddingRight
      canvas.clipRect(
        left, parent.paddingTop, right,
        parent.height - parent.paddingBottom
      )
    } else {
      left = 0
      right = parent.width
    }
    val childCount = parent.childCount
    for (i in 0 until childCount) {
      val child = parent.getChildAt(i)
      parent.getDecoratedBoundsWithMargins(child, mBounds)
      val bottom = mBounds.bottom + Math.round(child.translationY)
      val top = mBounds.top - mDivider!!.intrinsicHeight
      mDivider!!.setBounds(left, top, right, bottom)
      mDivider!!.draw(canvas)
    }
    canvas.restore()
  }

  /**
   * Retrieve any offsets for the given item. Each field of `outRect` specifies
   * the number of pixels that the item view should be inset by, similar to padding or margin.
   * The default implementation sets the bounds of outRect to 0 and returns.
   *
   *
   *
   *
   * If this ItemDecoration does not affect the positioning of item views, it should set
   * all four fields of `outRect` (left, top, right, bottom) to zero
   * before returning.
   *
   *
   *
   *
   * If you need to access Adapter for additional data, you can call
   * [RecyclerView.getChildAdapterPosition] to get the adapter position of the
   * View.
   *
   * @param outRect Rect to receive the output.
   * @param view    The child view to decorate
   * @param parent  RecyclerView this ItemDecoration is decorating
   * @param state   The current state of RecyclerView.
   */
  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
    state: RecyclerView.State
  ) {
    if (mDivider == null) {
      outRect[0, 0, 0] = 0
      return
    }
    if (mOrientation == VERTICAL) {
      outRect[0, 0, 0] = mDivider!!.intrinsicHeight
    } else {
      outRect[0, 0, mDivider!!.intrinsicWidth] = 0
    }
  }

  private fun setOrientation(orientation: Int) {
    require(!(orientation != VERTICAL && orientation != HORIZONTAL)) { "Invalid orientation. It should be either HORIZONTAL or VERTICAL" }
    mOrientation = orientation
  }

  fun setDrawable(divider: Drawable) {
    mDivider = divider
  }

  fun setDrawable(@DrawableRes drawableId: Int) {
    mDivider = mContext.resources.getDrawable(drawableId)
  }

  companion object {
    private const val TAG = "SimpleDecoration"
    const val VERTICAL = LinearLayout.VERTICAL
    const val HORIZONTAL = LinearLayout.HORIZONTAL
  }

  init {
    setOrientation(orientation)
  }
}