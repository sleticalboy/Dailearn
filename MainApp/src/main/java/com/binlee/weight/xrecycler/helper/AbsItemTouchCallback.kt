package com.binlee.weight.xrecycler.helper

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Created on 18-2-7.
 *
 * @author leebin
 * @version 1.0
 * @description Abstract ItemTouchCallback for ItemTouchHelper
 */
abstract class AbsItemTouchCallback : ItemTouchHelper.Callback() {

  override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
    if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
      if (viewHolder is ItemTouchHelperViewHolder) {
        (viewHolder as ItemTouchHelperViewHolder).onItemSelected()
      }
    }
    super.onSelectedChanged(viewHolder, actionState)
  }

  override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
    if (viewHolder is ItemTouchHelperViewHolder) {
      (viewHolder as ItemTouchHelperViewHolder).onItemClear()
    }
    super.clearView(recyclerView, viewHolder)
  }
}
