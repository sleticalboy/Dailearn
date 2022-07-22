package com.sleticalboy.weight.xrecycler.decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * Date: 2017/2/16 0016.
 *
 * @author Administrator
 */
class DividerGridItemDecoration(private val mContext: Context, private val mSpace: Int) :
  ItemDecoration() {

  private var mDivider: Drawable?

  override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
    drawHorizontal(c, parent)
    drawVertical(c, parent)
  }

  private fun getSpanCount(parent: RecyclerView): Int {
    // 列数
    var spanCount = -1
    val layoutManager = parent.layoutManager
    if (layoutManager is GridLayoutManager) {
      spanCount = layoutManager.spanCount
    } else if (layoutManager is StaggeredGridLayoutManager) {
      spanCount = layoutManager
        .spanCount
    }
    return spanCount
  }

  private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
    val childCount = parent.childCount
    for (i in 0 until childCount) {
      val child = parent.getChildAt(i)
      val params = child.layoutParams as RecyclerView.LayoutParams
      val left = child.left - params.leftMargin
      val right = (child.right + params.rightMargin
          + mDivider!!.intrinsicWidth)
      val top = child.bottom + params.bottomMargin
      mDivider!!.setBounds(left, top, right, top)
      mDivider!!.draw(c)
    }
  }

  private fun drawVertical(c: Canvas, parent: RecyclerView) {
    val childCount = parent.childCount
    for (i in 0 until childCount) {
      val child = parent.getChildAt(i)
      val params = child.layoutParams as RecyclerView.LayoutParams
      val top = child.top - params.topMargin
      val bottom = child.bottom + params.bottomMargin
      val left = child.right + params.rightMargin
      mDivider!!.setBounds(left, top, left, bottom)
      mDivider!!.draw(c)
    }
  }

  /**
   * @param parent
   * @param pos
   * @param spanCount
   * @param childCount
   * @return 是否是第一列
   */
  private fun isFirstColumn(
    parent: RecyclerView,
    pos: Int,
    spanCount: Int,
    childCount: Int
  ): Boolean {
    val layoutManager = parent.layoutManager
    if (layoutManager is GridLayoutManager) {
      // 如果是最后一列，则不需要绘制右边
      return pos % spanCount == 0
    } else if (layoutManager is StaggeredGridLayoutManager) {
      val orientation = layoutManager
        .orientation
      return if (orientation == StaggeredGridLayoutManager.VERTICAL) {
        // 如果是最后一列，则不需要绘制右边
        pos % spanCount == 0
      } else {
        pos < spanCount
      }
    }
    return false
  }

  /**
   * @param parent
   * @param pos
   * @param spanCount
   * @param childCount
   * @return 是否是最后一列
   */
  private fun isLastColumn(
    parent: RecyclerView,
    pos: Int,
    spanCount: Int,
    childCount: Int
  ): Boolean {
    var count = childCount
    val layoutManager = parent.layoutManager
    if (layoutManager is GridLayoutManager) {
      // 如果是最后一列，则不需要绘制右边
      return (pos + 1) % spanCount == 0
    } else if (layoutManager is StaggeredGridLayoutManager) {
      val orientation = layoutManager
        .orientation
      return if (orientation == StaggeredGridLayoutManager.VERTICAL) {
        // 如果是最后一列，则不需要绘制右边
        (pos + 1) % spanCount == 0
      } else {
        count -= count % spanCount
        // 如果是最后一列，则不需要绘制右边
        pos >= count
      }
    }
    return false
  }

  /**
   * @param parent
   * @param pos
   * @param spanCount
   * @param childCount
   * @return 是否第一行
   */
  private fun isFirstRaw(parent: RecyclerView, pos: Int, spanCount: Int, childCount: Int): Boolean {
    val layoutManager = parent.layoutManager
    if (layoutManager is GridLayoutManager) {
      return pos <= getSpanCount(parent) - 1
    } else if (layoutManager is StaggeredGridLayoutManager) {
      val orientation = layoutManager
        .orientation
      // StaggeredGridLayoutManager 且纵向滚动
      return if (orientation == StaggeredGridLayoutManager.VERTICAL) {
        pos <= getSpanCount(parent) - 1
      } else  // StaggeredGridLayoutManager 且横向滚动
      {
        // 如果是最后一行，则不需要绘制底部
        pos % spanCount == 0
      }
    }
    return false
  }

  /**
   * @param parent
   * @param pos
   * @param spanCount
   * @param childCount
   * @return 判断item是否在最后一行
   */
  private fun isLastRaw(parent: RecyclerView, pos: Int, spanCount: Int, childCount: Int): Boolean {
    var count = childCount
    val layoutManager = parent.layoutManager
    if (layoutManager is GridLayoutManager) {
      count -= count % spanCount
      // 如果是最后一行，则不需要绘制底部
      return pos >= count
    } else if (layoutManager is StaggeredGridLayoutManager) {
      val orientation = layoutManager
        .orientation
      // StaggeredGridLayoutManager 且纵向滚动
      return if (orientation == StaggeredGridLayoutManager.VERTICAL) {
        count -= count % spanCount
        // 如果是最后一行，则不需要绘制底部
        pos >= count
      } else  // StaggeredGridLayoutManager 且横向滚动
      {
        // 如果是最后一行，则不需要绘制底部
        (pos + 1) % spanCount == 0
      }
    }
    return false
  }

  override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {   //列数
    val spanCount = getSpanCount(parent)
    //item个数
    val childCount = parent.adapter!!.itemCount
    if (isLastRaw(parent, itemPosition, spanCount, childCount)) // 如果是最后一行，则不需要绘制底部
    {
      outRect[0, 0, mSpace] = 0
    } else if (isLastColumn(parent, itemPosition, spanCount, childCount)) // 如果是最后一列，则不需要绘制右边
    {
      outRect[0, 0, mSpace] = mSpace
    } /*else if (isFirstRaw(parent, itemPosition, spanCount, childCount)) {// 如果是第一行，则只需要绘制上边
            outRect.set(10, 10, 0, 10);
        } else if (isFirstColumn(parent, itemPosition, spanCount, childCount)){// 如果是第一列，则只需要绘制上边
            outRect.set(10, 0, 10, 0);
        } else if (isFirstRaw(parent, itemPosition, spanCount, childCount)
                && isLastColumn(parent, itemPosition, spanCount, childCount)){
            outRect.set(10, 10, 10, 10);
        }*/ else {
      outRect[0, 0, mSpace] = mSpace
    }
  }

  fun setDivider(@DrawableRes divider: Int) {
    mDivider = ContextCompat.getDrawable(mContext, divider)
  }

  companion object {
    private val ATTRS = intArrayOf(android.R.attr.listDivider)
  }

  init {
    val a = mContext.obtainStyledAttributes(ATTRS)
    mDivider = a.getDrawable(0)
    a.recycle()
  }
}