package com.binlee.learning.weight.xrecycler.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

/**
 * Created on 18-2-7.
 *
 * @author leebin
 * @version 1.0
 * @description
 */
abstract class XBaseHolder<M>(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

  constructor(parent: ViewGroup, @LayoutRes res: Int)
      : this(LayoutInflater.from(parent.context).inflate(res, parent, false))

  protected fun <V : View?> getView(@IdRes id: Int): V {
    return itemView.findViewById<View>(id) as V
  }

  protected val dataPosition: Int
    get() {
      val adapter = getOwnerAdapter<RecyclerView.Adapter<*>>()
      return if (adapter != null && adapter is XRecyclerAdapter<*>) {
        adapterPosition - adapter.headersCount
      } else adapterPosition
    }

  protected fun <A : RecyclerView.Adapter<*>?> getOwnerAdapter(): A? {
    val recyclerView = ownerRecyclerView
    return if (recyclerView == null) null else recyclerView.adapter as A?
  }

  private val ownerRecyclerView: RecyclerView?
    get() {
      try {
        val mOwnerRecyclerView = RecyclerView.ViewHolder::class.java
          .getDeclaredField("mOwnerRecyclerView")
        mOwnerRecyclerView.isAccessible = true
        return mOwnerRecyclerView[this] as RecyclerView
      } catch (e: NoSuchFieldException) {
        e.printStackTrace()
      } catch (e: IllegalAccessException) {
        e.printStackTrace()
      }
      return null
    }

  protected val context: Context get() = itemView.context

  abstract fun bindData(data: M)
}