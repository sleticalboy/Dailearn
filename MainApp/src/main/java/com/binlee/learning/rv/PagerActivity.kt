package com.binlee.learning.rv

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.binlee.learning.R
import com.binlee.weight.view.PagerView
import com.binlee.weight.xrecycler.adapter.XBaseHolder
import com.binlee.weight.xrecycler.adapter.XRecyclerAdapter

/**
 * Created on 18-2-11.
 *
 * @author leebin
 * @version 1.0
 * @description 测试 RecyclerView  分页效果
 */
class PagerActivity : AppCompatActivity() {

  private val mImagesId = arrayOf(
    R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher,
    R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher,
    R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher,
    R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher,
    R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher,
    R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher,
    R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    //        setView();
    setLayout()
  }

  private fun setLayout() {
    setContentView(R.layout.activity_pager)
    val pagerView = findViewById<PagerView>(R.id.pager_view)
    pagerView.setTitle("已添加的应用")
    pagerView.adapter = PagerAdapter(this, mImagesId)
    pagerView.setIndicatorDrawable(R.drawable.mx_page_indicator)
    findViewById<View>(R.id.btnShowPage).setOnClickListener {
      pagerView.visibility = if (++sCounter % 2 == 0) View.GONE else View.VISIBLE
    }
  }

  private fun setView() {
    val rows = 3
    val columns = 4
    val pagerView = PagerView(this, rows, columns)
    pagerView.setIndicatorSize(4)
    pagerView.setIndicatorDrawable(R.drawable.mx_page_indicator)
    pagerView.adapter = PagerAdapter(this, mImagesId)
    setContentView(pagerView)
  }

  internal class PagerAdapter(context: Context, private var mIntegers: Array<Int>) :
    XRecyclerAdapter<Int>(context) {

    override fun onCreateItemHolder(parent: ViewGroup, viewType: Int): XBaseHolder<Int> {
      return ItemHolder(parent, R.layout.item_common_layout)
    }

    override fun getItemData(position: Int): Int {
      //            position %= mIntegers.length;
      return mIntegers[position]
    }

    override fun getCount(): Int {
      return mIntegers.size
    }

    internal class ItemHolder(parent: ViewGroup, res: Int) : XBaseHolder<Int>(parent, res) {

      private var mImageView: ImageView? = getView(R.id.image_view)

      override fun bindData(data: Int) {
        mImageView!!.setImageResource(data)
      }

      init {
        mImageView!!.adjustViewBounds = true
      }
    }
  }

  companion object {
    private const val TAG = "PagerActivity"
    private var sCounter = 0
  }
}