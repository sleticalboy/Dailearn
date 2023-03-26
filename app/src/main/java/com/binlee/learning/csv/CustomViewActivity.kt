package com.binlee.learning.csv

import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView

/**
 * Created on 18-6-10.
 *
 * @author leebin
 * @description 自定义 View 相关知识
 */
class CustomViewActivity : ListActivity() {

  private val mDataList = arrayOf(
    ViewHolder(ScrollerActivity::class.java, "Scroller 使用"),
    ViewHolder(PathActivity::class.java, "Path 类使用"),
    ViewHolder(GridViewActivity::class.java, "GridView 使用")
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mDataList)
  }

  @Deprecated("Deprecated in Java")
  override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
    val holder = mDataList[position]
    (v as TextView).text = holder.mName
    startActivity(Intent(this, holder.mClass))
  }

  class ViewHolder(internal var mClass: Class<*>, internal var mName: String) {

    override fun toString(): String {
      return mName
    }
  }
}
