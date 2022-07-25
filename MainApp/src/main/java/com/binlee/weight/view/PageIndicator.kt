package com.binlee.weight.view

import androidx.viewpager.widget.ViewPager

/**
 * Created on 18-3-15.
 *
 * @author leebin
 * @description 页面指示器
 */
interface PageIndicator {
  /**
   * @param viewPager
   * @param initialPos
   */
  fun setupWithViewPager(viewPager: ViewPager?, initialPos: Int)

  /**
   * @param pageIndex
   */
  fun setCurrentPage(pageIndex: Int)

  /**
   *
   */
  fun notifyDataSetChanged()
}