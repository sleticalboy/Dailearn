package com.sleticalboy.dailywork

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.sleticalboy.dailywork.base.BaseActivity
import com.sleticalboy.dailywork.bean.ModuleItem
import com.sleticalboy.dailywork.data.DataEngine
import kotlinx.android.synthetic.main.activity_index.*

const val TAG = "IndexActivity"

class IndexActivity : BaseActivity() {

    val dataSet = arrayListOf<ModuleItem>()

    override fun layoutResId(): Int = R.layout.activity_index

    override fun initView() {
        val adapter = DataAdapter()
        DataEngine.get().indexModel().moduleSource.observe(this, Observer {
            Log.d(TAG, "items: $it")
            dataSet.clear()
            dataSet.addAll(it)
            adapter.notifyDataSetChanged()
        })
        recyclerView.adapter = adapter
    }

    inner class DataAdapter : RecyclerView.Adapter<ItemHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            return ItemHolder(TextView(parent.context))
        }

        override fun getItemCount(): Int = dataSet.size

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {
            val item = dataSet[position]
            holder.textView.text = item.title
            holder.textView.setOnClickListener {
                Log.d(TAG, "item click with: ${item.clazz}")
                startActivity(Intent(this@IndexActivity, item.clazz))
            }
            holder.textView.setPadding(32, 16, 32, 16)
            holder.textView.textSize = 24F
            holder.textView.setTextColor(Color.BLUE)
        }
    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView = itemView as TextView
    }
}