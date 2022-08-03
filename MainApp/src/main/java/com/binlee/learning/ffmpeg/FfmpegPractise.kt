package com.binlee.learning.ffmpeg

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.binlee.learning.R
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.bean.ModuleItem
import com.binlee.learning.databinding.ActivityIndexBinding
import com.example.ffmpeg.FfmpegHelper

/**
 * Created on 2022/8/3
 *
 * @author binlee
 */
class FfmpegPractise : BaseActivity() {

  // java 中的构造代码块
  init {
    //
  }

  private var mBind: ActivityIndexBinding? = null
  private val dataSet = arrayListOf<ModuleItem>(
    // ModuleItem(""),
  )

  override fun layout(): View {
    mBind = ActivityIndexBinding.inflate(layoutInflater)
    return mBind!!.root
  }

  override fun initView() {
    mBind?.recyclerView?.adapter = DataAdapter(dataSet)
    Toast.makeText(this, ffmpegConfiguration, Toast.LENGTH_LONG).show()
  }

  private class DataAdapter(private val dataSet: ArrayList<ModuleItem>) :
    RecyclerView.Adapter<ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
      val itemView = TextView(parent.context)
      itemView.setBackgroundResource(R.drawable.module_item_bg)
      return ItemHolder(itemView)
    }

    override fun getItemCount(): Int {
      return dataSet.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
      val item = dataSet[position]
      holder.textView.text = item.title
      holder.textView.setOnClickListener {
        Log.d(TAG, "item click with: ${item.clazz}")
        holder.itemView.context.startActivity(Intent(holder.itemView.context, item.clazz))
      }
    }
  }

  private class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val textView = itemView as TextView

    init {
      textView.gravity = Gravity.CENTER
      textView.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
      )
      textView.setPadding(32, 16, 32, 16)
      textView.textSize = 24F
      textView.setTextColor(Color.BLUE)
    }
  }

  companion object {
    private const val TAG = "FfmpegPractise"

    private val ffmpegConfiguration: String

    // java 中的静态代码块
    init {
      ffmpegConfiguration = FfmpegHelper.getConfiguration()
    }
  }
}