package com.sleticalboy.learning.rv

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sleticalboy.learning.base.BaseActivity
import com.sleticalboy.learning.databinding.ActivityRvNestedBinding
import com.sleticalboy.learning.databinding.ItemNestedRvBinding
import com.sleticalboy.learning.databinding.ItemRvTextBinding

/**
 * Created on 2021/8/17
 *
 * @author binli@faceunity.com
 */
class NestedRvActivity : BaseActivity() {

  private var mBind: ActivityRvNestedBinding? = null
  private val mData = arrayOf(
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
    arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k"),
  )

  override fun layout(): View {
    mBind = ActivityRvNestedBinding.inflate(layoutInflater)
    return mBind!!.root
  }

  override fun initView() {
    mBind!!.root.adapter = MyAdapter(data = mData)
  }

  override fun onDestroy() {
    super.onDestroy()
    mBind = null
  }

  class MyAdapter(private val data: Array<Array<String>>) : RecyclerView.Adapter<HViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HViewHolder {
      return HViewHolder(
        ItemNestedRvBinding.inflate(
          LayoutInflater.from(parent.context),
          parent, false
        ).root
      )
    }

    override fun onBindViewHolder(holder: HViewHolder, position: Int) {
      if (holder.rv.adapter == null) {
        val adapter = ChildAdapter()
        adapter.setData(data = data[position])
        holder.rv.adapter = adapter
      } else {
        // rv 设置过数据了，滚动到指定位置？
        val tag = holder.rv.tag
        Log.d(TAG, "onBindViewHolder() tag: $tag")
        if (tag is Position) {
          holder.rv.layoutManager!!.scrollToPosition(tag.position)
        }
      }
    }

    override fun getItemCount(): Int = data.size

    // 能解决布局错乱问题，但是会不停地创建 ViewHolder，复用的优势就没了
    // override fun getItemViewType(position: Int): Int = position
  }

  class HViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    // 横向 RecyclerView
    val rv = itemView as RecyclerView

    init {
      rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
          // 获取第一个可见的 View
          val topView = rv.getChildAt(0)
          // 获取该 View 与左边缘的偏移量
          val lastOffset = topView.left
          // 获取该 View 在数据集中的位置
          val lastPosition = rv.layoutManager!!.getPosition(topView)
          rv.tag = Position(lastOffset, lastPosition)
        }
      })
    }
  }

  data class Position(val offset: Int, val position: Int)

  class ChildAdapter : RecyclerView.Adapter<ChildViewHolder>() {

    private lateinit var data: Array<String>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
      return ChildViewHolder(
        ItemRvTextBinding.inflate(
          LayoutInflater.from(parent.context),
          parent, false
        ).root
      )
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
      holder.textView.text = data[position]
    }

    override fun getItemCount(): Int = data.size

    fun setData(data: Array<String>) {
      this.data = data
    }
  }

  class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val textView = itemView as TextView

    init {
      val lp = textView.layoutParams
      lp.width = textView.resources.displayMetrics.widthPixels / 4
      textView.layoutParams = lp
    }
  }

  companion object {
    private const val TAG = "NestedRvActivity"
  }
}