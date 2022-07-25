package com.binlee.learning.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

/**
 * Created on 20-8-23.
 *
 * @author binlee sleticalboy@gmail.com
 */
abstract class BaseRVHolder<DATA>(parent: ViewGroup, @LayoutRes layout: Int) :
  RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(layout, parent, false)
  ) {

  abstract fun bindData(data: DATA, position: Int)
}