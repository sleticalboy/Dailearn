package com.sleticalboy.learning.rv.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import com.sleticalboy.learning.R
import com.sleticalboy.weight.xrecycler.adapter.XBaseHolder
import com.sleticalboy.weight.xrecycler.adapter.XRecyclerAdapter

/**
 * Created on 18-2-7.
 *
 * @author leebin
 * @version 1.0
 * @description
 */
class ItemAdapter(context: Context, private val mObjects: Array<Int>) :
  XRecyclerAdapter<Int>(context, mObjects) {

  override fun onCreateItemHolder(parent: ViewGroup, viewType: Int): XBaseHolder<Int> {
    return ViewHolder(parent, R.layout.item_wheel_layout)
  }

  override fun getCount(): Int {
    return Int.MAX_VALUE
  }

  override fun getItemData(position: Int): Int {
    return mObjects[getCount() % mObjects.size]
  }

  internal class ViewHolder(parent: ViewGroup, res: Int) : XBaseHolder<Int>(parent, res) {

    private var mImageView: ImageView? = getView(R.id.image_view)

    override fun bindData(resId: Int) {
      mImageView!!.setImageResource(resId)
    }

  }
}