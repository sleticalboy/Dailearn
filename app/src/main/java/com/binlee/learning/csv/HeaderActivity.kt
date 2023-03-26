package com.binlee.learning.csv

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.binlee.learning.R
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.csv.HeaderActivity.RecyclerAdapter.MyViewHolder
import com.binlee.learning.databinding.ActivityHeaderBinding
import java.util.ArrayList
import java.util.Random

/**
 * Created on 18-6-6.
 *
 * @author leebin
 * @description
 */
class HeaderActivity : BaseActivity() {

  override fun layout(): View {
    // R.layout.activity_header
    return ActivityHeaderBinding.inflate(layoutInflater).root
  }

  override fun initView() {
    val appBarLayout = findViewById<AppBarLayout>(R.id.app_bar_layout)
    appBarLayout.addOnOffsetChangedListener(OnOffsetChangedListener { _: AppBarLayout?, verticalOffset: Int ->
      Log.d("HeaderActivity", "verticalOffset:$verticalOffset")
    })
    val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
    val lm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    recyclerView.layoutManager = lm
    recyclerView.adapter = RecyclerAdapter()
  }

  override fun initData() {
    for (i in 0..49) {
      M_DATA_SET.add(Random().nextInt(Int.MAX_VALUE).toString())
    }
  }

  internal class RecyclerAdapter : RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
      return MyViewHolder(
        LayoutInflater.from(parent.context)
          .inflate(R.layout.item_rv_text, parent, false)
      )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
      holder.tvRecyclerItem.text = M_DATA_SET[position]
    }

    override fun getItemCount(): Int {
      return M_DATA_SET.size
    }

    internal class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
      var tvRecyclerItem: TextView = itemView.findViewById(R.id.tv_recycler_item)
    }
  }

  companion object {
    private val M_DATA_SET: MutableList<String> = ArrayList()
  }
}