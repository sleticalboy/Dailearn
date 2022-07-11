package com.sleticalboy.weight.xrecycler.helper

import android.content.Context
import android.view.View.MeasureSpec
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

/**
 * Created on 18-2-23.
 *
 * @author leebin
 * @version 1.0
 * @description
 */
class MyLinearLayoutManager(context: Context?) : LinearLayoutManager(context, HORIZONTAL, false) {

  private val mMeasuredDimension = IntArray(2)

  override fun onMeasure(
    recycler: Recycler, state: RecyclerView.State,
    widthSpec: Int, heightSpec: Int
  ) {
    val widthMode = MeasureSpec.getMode(widthSpec)
    val heightMode = MeasureSpec.getMode(heightSpec)
    val widthSize = MeasureSpec.getSize(widthSpec)
    val heightSize = MeasureSpec.getSize(heightSpec)
    var width = 0
    var height = 0
    for (i in 0 until itemCount) {
      measureScrapChild(
        recycler, i,
        MeasureSpec.makeMeasureSpec(i, MeasureSpec.UNSPECIFIED),
        MeasureSpec.makeMeasureSpec(i, MeasureSpec.UNSPECIFIED),
        mMeasuredDimension
      )
      if (orientation == HORIZONTAL) {
        width = width + mMeasuredDimension[0]
        if (i == 0) {
          height = mMeasuredDimension[1]
        }
      } else {
        height = height + mMeasuredDimension[1]
        if (i == 0) {
          width = mMeasuredDimension[0]
        }
      }
    }
    when (widthMode) {
      MeasureSpec.EXACTLY -> width = widthSize
      MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
      }
    }
    when (heightMode) {
      MeasureSpec.EXACTLY -> height = heightSize
      MeasureSpec.AT_MOST, MeasureSpec.UNSPECIFIED -> {
      }
    }
    setMeasuredDimension(width, height)
  }

  private fun measureScrapChild(
    recycler: Recycler, position: Int, widthSpec: Int,
    heightSpec: Int, measuredDimension: IntArray
  ) {
    val view = recycler.getViewForPosition(position)
    val p = view.layoutParams as RecyclerView.LayoutParams
    val childWidthSpec = ViewGroup.getChildMeasureSpec(
      widthSpec,
      paddingLeft + paddingRight, p.width
    )
    val childHeightSpec = ViewGroup.getChildMeasureSpec(
      heightSpec,
      paddingTop + paddingBottom, p.height
    )
    view.measure(childWidthSpec, childHeightSpec)
    measuredDimension[0] = view.measuredWidth + p.leftMargin + p.rightMargin
    measuredDimension[1] = view.measuredHeight + p.bottomMargin + p.topMargin
    recycler.recycleView(view)
  }
}