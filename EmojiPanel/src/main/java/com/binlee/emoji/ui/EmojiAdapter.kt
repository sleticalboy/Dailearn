package com.binlee.emoji.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.binlee.emoji.ImageAdapter
import com.binlee.emoji.R
import com.binlee.emoji.helper.UrlHelper
import com.binlee.emoji.model.Emoji

/**
 * Created on 19-7-21.
 *
 * @author leebin
 */
internal class EmojiAdapter(emojis: Array<Emoji?>, spanCount: Int) : BaseAdapter() {

  private val mEmojis: MutableList<Emoji?> = emojis.toMutableList()
  private val mSpanCount: Int = spanCount
  private var mDeleteIndex = -1
  private var mDeleteAlpha = 0f

  override fun getCount(): Int {
    return mEmojis.size
  }

  override fun getItem(position: Int): Emoji? {
    return mEmojis[position]
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val itemView: View
    val emoji = getItem(position)
    val holder: EmojiHolder
    if (convertView == null) {
      itemView = LayoutInflater.from(parent.context)
        .inflate(R.layout.emoji_item_layout, parent, false)
      holder = EmojiHolder(itemView)
    } else {
      itemView = convertView
      holder = convertView.tag as EmojiHolder
    }
    if (emoji?.resId != -1) {
      holder.emojiIcon.setImageResource(emoji!!.resId)
    } else {
      val model = UrlHelper.inspectUrl(emoji.thumbnail)
      ImageAdapter.engine().show(model, holder.emojiIcon)
    }
    if (emoji.isSmall || emoji.description == null) {
      holder.emojiName.visibility = View.GONE
    } else {
      holder.emojiName.visibility = View.VISIBLE
      holder.emojiName.text = emoji.description!!.cn
    }
    if (mDeleteIndex == position) {
      holder.delIcon.visibility = View.VISIBLE
      holder.delIcon.alpha = mDeleteAlpha
      holder.emojiIcon.alpha = 1 - mDeleteAlpha
    } else {
      if (emoji.isDelete) {
        holder.delIcon.visibility = View.VISIBLE
        holder.delIcon.alpha = 1f
        holder.emojiIcon.visibility = View.GONE
      } else {
        holder.delIcon.visibility = View.GONE
        holder.emojiIcon.visibility = View.VISIBLE
        holder.emojiIcon.alpha = 1f
      }
    }
    val size: Int
    val width: Int
    if (mSpanCount == 4) {
      width = parent.resources.displayMetrics.widthPixels / 4
      size = (width * 0.67).toInt()
      val id = if (emoji.isAdd) R.dimen.mx_dp_20 else R.dimen.mx_dp_8
      if (emoji.isAdd) {
        // 去掉按压效果(每页只有8条数据，可以不考虑重用的问题)
        holder.emojiIcon.setBackgroundResource(R.drawable.emoji_add_btn_cover)
        holder.emojiIcon.setColorFilter(Color.parseColor("#6C6C6C"))
      }
      val padding = parent.resources.getDimensionPixelSize(id)
      holder.emojiIcon.setPadding(padding, padding, padding, padding)
    } else {
      width = parent.resources.displayMetrics.widthPixels / 7
      size = (width * 0.75f).toInt()
    }
    val lp = itemView.layoutParams
    lp.width = width
    itemView.layoutParams = lp
    updateImageSize(holder.emojiIcon, size)
    updateImageSize(holder.delIcon, size)
    return itemView
  }

  private fun updateImageSize(imageView: ImageView, size: Int) {
    val params = imageView.layoutParams as ConstraintLayout.LayoutParams
    params.width = size
    params.height = params.width
    imageView.layoutParams = params
  }

  fun attachToViewPager(viewPager: ViewPager?, index: Int) {
    if (viewPager == null || viewPager.adapter == null) {
      mDeleteIndex = -1
      mDeleteAlpha = 0f
      return
    }
    mDeleteIndex = index
    viewPager.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
      override fun onPageScrolled(p: Int, offset: Float, offsetPixels: Int) {
        if (p != 0) {
          return
        }
        // offset: [0, 1)
        // offset 增大：逐渐隐藏删除按钮
        // offset 减小：逐渐显示删除按钮
        mDeleteAlpha = 1 - offset
        notifyDataSetChanged()
      }
    })
  }

  fun updateDataSet(values: Array<Emoji?>) {
    if (values.isNotEmpty()) {
      mEmojis.clear()
      mEmojis.addAll(values)
      notifyDataSetChanged()
    }
  }

  val dataSet: Array<Emoji?>
    get() {
      return mEmojis.toTypedArray()
    }

  private class EmojiHolder(convertView: View) {

    val emojiIcon: ImageView = convertView.findViewById(R.id.emojiIcon)
    val delIcon: ImageView = convertView.findViewById(R.id.delIcon)
    val emojiName: TextView = convertView.findViewById(R.id.emojiName)

    init {
      convertView.tag = this
    }
  }

}