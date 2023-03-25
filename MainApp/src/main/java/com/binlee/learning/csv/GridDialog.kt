package com.binlee.learning.csv

import android.app.Dialog
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import com.binlee.learning.R
import com.binlee.learning.base.BaseDialog
import com.binlee.learning.bean.AudioItem
import com.binlee.learning.databinding.LayoutGridDialogBinding

/**
 * Created on 20-9-3.
 *
 * @author Ben binli@grandstream.cn
 */
class GridDialog : BaseDialog() {

  private var mBind: LayoutGridDialogBinding? = null
  private var mSelection: Int = 0
  private lateinit var mData: List<AudioItem>

  override fun layout(inflater: LayoutInflater, parent: ViewGroup?): View {
    // R.layout.layout_grid_dialog
    mBind = LayoutGridDialogBinding.inflate(inflater, parent, false)
    return mBind!!.root
  }

  override fun initView(view: View) {
    mBind!!.gridView.adapter = object : BaseAdapter() {

      override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val itemView: View
        val holder: ItemHolder
        if (convertView == null) {
          itemView = layoutInflater.inflate(R.layout.item_grid_view, parent, false)
          holder = ItemHolder(itemView)
        } else {
          itemView = convertView
          holder = convertView.tag as ItemHolder
        }
        // Log.d(TAG, "getView() position = $position, data = ${mData[position]}")
        if (mSelection == position) {
          itemView.setTag(R.id.gridView, TAG)
        } else {
          itemView.setTag(R.id.gridView, "")
        }
        holder.mTitle.text = mData[position].mTitle
        holder.mSummary.text = mData[position].mSummary
        holder.mTitle.setTextColor(resources.getColor(mData[position].mColor))
        holder.mSummary.setTextColor(resources.getColor(mData[position].mColor))
        return itemView
      }

      override fun getItem(position: Int): AudioItem = mData[position]

      override fun getItemId(position: Int): Long = position.toLong()

      override fun getCount(): Int = mData.size
    }
    mBind!!.gridView.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
      val left = keyCode == KeyEvent.KEYCODE_DPAD_LEFT
      val right = keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
      if ((left || right) && event.action == KeyEvent.ACTION_DOWN) {
        val child = mBind!!.gridView.selectedView // getSelectedView()
        val pos = mBind!!.gridView.selectedItemPosition // getSelectedItemPosition()
        // Log.d(TAG, "onKey() -> down -> pos: $pos -> left: $left -> right: $right")
        if (child != null && pos > 0 && pos < mData.size - 1) {
          if (left && pos % mBind!!.gridView.numColumns == 0) { // getNumColumns()
            mBind!!.gridView.setSelection(pos - 1)
            Log.d(TAG, "onKey() -> left -> previous: ${pos - 1}")
            return@OnKeyListener true
          }
          if (right && pos % mBind!!.gridView.numColumns == mBind!!.gridView.numColumns - 1) {
            mBind!!.gridView.setSelection(pos + 1)
            Log.d(TAG, "onKey() -> right -> next: ${pos + 1}")
            return@OnKeyListener true
          }
        }
      }
      false
    })

    mBind!!.gridView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

      private var mView: View? = null

      override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Log.d(TAG, "onItemSelected() position = $position, data = ${mData[position]}")
        if (mSelection == position && view?.isSelected!!) {
          mView = view
          updateColor(view, mData[position].mColor)
          return
        }
        if (mSelection != position) {
          if (TAG == mView?.getTag(R.id.gridView)) {
            updateColor(mView, mData[mSelection].mColor2)
          }
          updateColor(view, mData[position].mColor2)
        }
      }

      override fun onNothingSelected(parent: AdapterView<*>?) {
        // do nothing
      }
    }
    mBind!!.gridView.post {
      mBind!!.gridView.setSelection(mSelection)
      val params = mBind!!.gridView.layoutParams
      params.height = (resources.displayMetrics.heightPixels * 0.4).toInt()
      mBind!!.gridView.layoutParams = params
    }
  }

  override fun configureDialog(dialog: Dialog) {
    dialog.setTitle("Grid Dialog")
  }

  private fun updateColor(view: View?, color: Int) {
    val realColor = resources.getColor(color)
    view?.findViewById<TextView>(R.id.tvTitle)?.setTextColor(realColor)
    view?.findViewById<TextView>(R.id.tvSummary)?.setTextColor(realColor)
  }

  fun setSelection(selection: Int) {
    mSelection = selection
  }

  fun setData(data: List<AudioItem>) {
    mData = data
  }

  class ItemHolder(itemView: View) {

    init {
      itemView.tag = this
    }

    val mTitle: TextView = itemView.findViewById(R.id.tvTitle)
    val mSummary: TextView = itemView.findViewById(R.id.tvSummary)
  }
}