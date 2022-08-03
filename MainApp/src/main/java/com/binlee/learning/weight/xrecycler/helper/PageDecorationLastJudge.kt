package com.binlee.learning.weight.xrecycler.helper

/**
 * Created on 2016/11/15.
 * @author zhuguohui
 */
interface PageDecorationLastJudge {
  /**
   * Is the last row in one page
   *
   * @param position
   * @return
   */
  fun isLastRow(position: Int): Boolean

  /**
   * Is the last Colum in one row;
   *
   * @param position
   * @return
   */
  fun isLastColumn(position: Int): Boolean

  fun isLastLast(position: Int): Boolean
}
