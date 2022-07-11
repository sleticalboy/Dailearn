package com.sleticalboy.weight.xrecycler.helper

/**
 * Created on 18-2-7.
 *
 * @author leebin
 * @version 1.0
 * @description 用于 RecyclerView item 拖拽辅助
 */
interface ItemTouchDragAdapter {

  /**
   * Called when the item was moved
   *
   * @param from start position
   * @param to   end position
   */
  fun onItemMove(from: Int, to: Int)
}
