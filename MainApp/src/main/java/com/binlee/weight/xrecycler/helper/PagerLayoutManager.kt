package com.binlee.weight.xrecycler.helper

import android.graphics.Rect
import android.util.SparseArray
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

/**
 * Created on 2016/11/9.
 *
 * @author zhuguohui
 */
class PagerLayoutManager(rows: Int, columns: Int) : RecyclerView.LayoutManager(),
  PageDecorationLastJudge {

  private val totalHeight = 0
  private var totalWidth = 0
  private var offsetY = 0
  private var offsetX = 0
  private val allItemFrames = SparseArray<Rect>()
  private var mRows = 0
  private var mColumns = 0
  private var mPageSize = 0
  private var itemWidth = 0
  private var itemHeight = 0
  private var onePageSize = 0
  private var itemWidthUsed = 0
  private var itemHeightUsed = 0
  private val mMeasuredDimension = IntArray(2)

  override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
    return RecyclerView.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )
  }

  override fun canScrollHorizontally(): Boolean {
    return true
  }

  override fun scrollHorizontallyBy(dx: Int, recycler: Recycler, state: RecyclerView.State): Int {
    detachAndScrapAttachedViews(recycler)
    val newX = offsetX + dx
    var result = dx
    if (newX > totalWidth) {
      result = totalWidth - offsetX
    } else if (newX < 0) {
      result = 0 - offsetX
    }
    offsetX += result
    offsetChildrenHorizontal(-result)
    recycleAndFillItems(recycler, state)
    return result
  }

  private val usableWidth: Int
    get() = width - paddingLeft - paddingRight
  private val usableHeight: Int
    get() = height - paddingTop - paddingBottom

  override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
    if (itemCount == 0) {
      removeAndRecycleAllViews(recycler)
      return
    }
    if (state.isPreLayout) {
      return
    }
    //获取每个Item的平均宽高
    itemWidth = usableWidth / mColumns
    itemHeight = usableHeight / mRows

    //计算宽高已经使用的量，主要用于后期测量
    itemWidthUsed = (mColumns - 1) * itemWidth
    itemHeightUsed = (mRows - 1) * itemHeight

    //计算总的页数
    mPageSize = getPageSize()

    //计算可以横向滚动的最大值
    totalWidth = (mPageSize - 1) * width

    //分离view
    detachAndScrapAttachedViews(recycler)
    val count = itemCount
    var p = 0
    while (p < mPageSize) {
      var r = 0
      while (r < mRows) {
        var c = 0
        while (c < mColumns) {
          val index = p * onePageSize + r * mColumns + c
          if (index == count) {
            //跳出多重循环
            r = mRows
            p = mPageSize
            break
          }
          val view = recycler.getViewForPosition(index)
          addView(view)
          //测量item
          measureChildWithMargins(view, itemWidthUsed, itemHeightUsed)
          val width = getDecoratedMeasuredWidth(view)
          val height = getDecoratedMeasuredHeight(view)
          //记录显示范围
          var rect = allItemFrames[index]
          if (rect == null) {
            rect = Rect()
          }
          val x = p * usableWidth + c * itemWidth
          val y = r * itemHeight
          rect[x, y, width + x] = height + y
          allItemFrames.put(index, rect)
          c++
        }
        r++
      }
      //每一页循环以后就回收一页的View用于下一页的使用
      removeAndRecycleAllViews(recycler)
      p++
    }
    recycleAndFillItems(recycler, state)
  }

  override fun onDetachedFromWindow(view: RecyclerView, recycler: Recycler) {
    super.onDetachedFromWindow(view, recycler)
    offsetX = 0
    offsetY = 0
  }

  private fun recycleAndFillItems(recycler: Recycler, state: RecyclerView.State) {
    if (state.isPreLayout) {
      return
    }
    val displayRect = Rect(
      paddingLeft + offsetX, paddingTop,
      width - paddingLeft - paddingRight + offsetX,
      height - paddingTop - paddingBottom
    )
    val childRect = Rect()
    for (i in 0 until childCount) {
      val child = getChildAt(i)
      childRect.left = getDecoratedLeft(child!!)
      childRect.top = getDecoratedTop(child)
      childRect.right = getDecoratedRight(child)
      childRect.bottom = getDecoratedBottom(child)
      if (!Rect.intersects(displayRect, childRect)) {
        removeAndRecycleView(child, recycler)
      }
    }
    for (i in 0 until itemCount) {
      if (Rect.intersects(displayRect, allItemFrames[i])) {
        val view = recycler.getViewForPosition(i)
        addView(view)
        measureChildWithMargins(view, itemWidthUsed, itemHeightUsed)
        val rect = allItemFrames[i]
        layoutDecorated(
          view, rect.left - offsetX, rect.top,
          rect.right - offsetX, rect.bottom
        )
      }
    }
  }

  /**
   * 计算总页数
   *
   * @return 总页数
   */
  fun getPageSize(): Int {
    val itemCount = itemCount
    return itemCount / onePageSize + if (itemCount % onePageSize == 0) 0 else 1
  }

  override fun isLastRow(position: Int): Boolean {
    if (position in 0 until itemCount) {
      var indexOfPage = position % onePageSize
      indexOfPage++
      return indexOfPage > (mRows - 1) * mColumns && indexOfPage <= onePageSize
    }
    return false
  }

  override fun isLastColumn(position: Int): Boolean {
    var index = position
    if (index in 0 until itemCount) {
      index++
      return index % mColumns == 0
    }
    return false
  }

  override fun isLastLast(position: Int): Boolean {
    var temp = position
    temp++
    return temp % onePageSize == 0
  }

  companion object {
    private const val TAG = "PagerLayoutManager"
  }

  init {
    mRows = rows
    mColumns = columns
    onePageSize = rows * columns
  }
}