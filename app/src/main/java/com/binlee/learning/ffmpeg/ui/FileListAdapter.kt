package com.binlee.learning.ffmpeg.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckedTextView
import com.binlee.learning.ffmpeg.model.FileItem
import java.io.File

class FileListAdapter : BaseAdapter() {

  private val dataSet = ArrayList<FileItem>()

  fun setChecked(position: Int) {
    for (i in dataSet.indices) {
      dataSet[i].checked = if (i == position) !dataSet[i].checked else false
    }
    notifyDataSetChanged()
  }

  fun getPath(position: Int): String? {
    return dataSet[position].file.absolutePath
  }

  override fun getCount(): Int {
    return dataSet.size
  }

  override fun getItem(position: Int): FileItem {
    return dataSet[position]
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
    var checkedText = convertView as CheckedTextView?
    if (checkedText == null) {
      checkedText = LayoutInflater.from(parent!!.context)
        .inflate(android.R.layout.simple_list_item_checked, parent, false) as CheckedTextView
      checkedText.textSize = 10f
    }
    checkedText.text = getItem(position).file.name
    checkedText.isChecked = getItem(position).checked
    return checkedText
  }

  fun remove(item: FileItem) {
    dataSet.remove(item)
    notifyDataSetChanged()
  }

  fun replaceAll(it: List<File>) {
    dataSet.clear()
    for (i in it.indices) {
      dataSet.add(FileItem(it[i], false))
    }
    notifyDataSetChanged()
  }

  fun add(item: FileItem) {
    dataSet.add(item)
    notifyDataSetChanged()
  }
}