package com.binlee.emoji.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Spannable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.binlee.emoji.ImageAdapter
import com.binlee.emoji.R
import com.binlee.emoji.helper.EmojiHelper
import com.binlee.emoji.helper.LogHelper
import com.binlee.emoji.helper.ThreadHelper
import com.binlee.emoji.helper.UiHelper
import com.binlee.emoji.helper.UrlHelper
import com.binlee.emoji.model.Emoji
import com.binlee.emoji.model.EmojiGroup
import com.binlee.emoji.model.EmojiRepo
import com.binlee.emoji.model.EmojiRepo.OnDataChangeCallback
import com.binlee.emoji.span.CustomAtSpan
import com.binlee.emoji.ui.EmojiAdapter
import com.binlee.emoji.ui.EmojiPreviewWindow
import com.binlee.emoji.widget.EmojiGridView.OnPressListener
import kotlin.math.max
import kotlin.math.min

/**
 * Created on 19-7-17.
 *
 * @author leebin
 */
class EmojiPanel @JvmOverloads constructor(
  context: Context?, attrs: AttributeSet? = null,
  defStyle: Int = 0
) : ConstraintLayout(context!!, attrs, defStyle) {

  private val mInflater: LayoutInflater = LayoutInflater.from(context)
  private var mCallback: OnActionCallback? = null
  private var mPreviewWin: EmojiPreviewWindow? = null

  // 区分是点击切换分组还是由子 ViewPager 从最后一页或者第一页滑动切换分组
  private var mScrollToChange = true

  // 退出表情面板时所在的组
  private var mLastGroup: EmojiGroup? = null
  private lateinit var mGroups: MutableList<EmojiGroup>
  private lateinit var mGroupItems: MutableList<View>
  private var mRecentEmojiAdapter: EmojiAdapter? = null

  /**
   * true: 只提供基础功能, 即只显示小表情
   * false: 提供全部功能，即除包含基础功能外的表情包、表情商店等入口
   */
  private var mBasicMode = true
  private var mAttachedEditText: EditText? = null
  private var mDataChangeCallback: OnDataChangeCallback? = null

  /**
   * 如果对输入框的操作没有特殊需求，可直接调用此方法，内部自动处理删除和插入操作
   *
   * @param editText 输入框
   */
  fun attachWithEditText(editText: EditText?) {
    initialize(editText, true, null)
  }

  /**
   * 如果对输入框的操作有特殊需求，比如处理`@`等特殊符号，直接调用此方法处理特殊操作
   *
   * @param editText 输入框
   * @param listener 回调，如调用者无特殊处理可传 null
   */
  fun initialize(
    editText: EditText?, basicMode: Boolean,
    listener: OnActionCallback?
  ) {
    if (mCallback != null) {
      // mCallback 字段不为空说明 attachWithEditText() 或 initialize() 之一已被调用
      return
    }
    mBasicMode = basicMode
    mAttachedEditText = editText
    mCallback = listener ?: OnActionCallback()
    // 首次 52 ms， 之后平均在 20 ms
    val start: Long = LogHelper.logTime()
    initContentView()
    LogHelper.debug("EmojiPanel", "Constructor setupClient:" + LogHelper.elapsedMillis(start))
  }

  private fun initContentView() {
    val emojiStore = findViewById<ImageButton>(R.id.emojiStore)
    emojiStore.setOnClickListener { Toast.makeText(context, "功能建设中", Toast.LENGTH_SHORT).show() }
    emojiStore.visibility = if (mBasicMode) GONE else VISIBLE
    val myEmoji = findViewById<ImageButton>(R.id.myEmoji)
    myEmoji.setOnClickListener { LogHelper.debug("EmojiPanel", "功能建设中") }
    myEmoji.visibility = if (mBasicMode) GONE else VISIBLE
    val parentPager: ViewPager = findViewById(R.id.parentPager)
    val parentIndicator = findViewById<LinearLayout>(R.id.parentIndicator)
    setupEmojiGroup(parentPager, parentIndicator)
    findViewById<View>(R.id.indicatorContainer).visibility = if (mBasicMode) GONE else VISIBLE
  }

  private fun setupEmojiGroup(parentPager: ViewPager, container: LinearLayout) {
    mGroups = EmojiRepo.emojiGroups
    if (mBasicMode) {
      val smallGroup = mGroups[0]
      mGroups.clear()
      mGroups.add(smallGroup)
    }
    mGroupItems = mutableListOf()
    var item: View
    for (i in mGroups.indices) {
      // 初始化组指示器
      val emojiGroup = mGroups[i]
      item = mInflater.inflate(R.layout.emoji_preview_layout, container, false)
      val lp = item.layoutParams as LinearLayout.LayoutParams
      lp.height = resources.getDimensionPixelSize(R.dimen.mx_dp_36)
      lp.width = resources.getDimensionPixelSize(R.dimen.mx_dp_48)
      lp.gravity = Gravity.CENTER
      item.layoutParams = lp
      item.setPadding(0, 0, 0, 0)
      item.setBackgroundResource(R.drawable.emoji_item_bg)
      item.findViewById<View>(R.id.emojiName).visibility = GONE
      val image = item.findViewById<ImageView>(R.id.emojiIcon)
      val p = image.layoutParams as LayoutParams
      p.width = resources.getDimensionPixelSize(R.dimen.mx_dp_28)
      p.height = p.width
      p.bottomToBottom = LayoutParams.PARENT_ID
      image.layoutParams = p
      ImageAdapter.engine().show(UrlHelper.inspectUrl(emojiGroup.thumbnail), image)
      item.setOnClickListener {
        mScrollToChange = false
        parentPager.currentItem = i
      }
      container.addView(item)
      // 初始化组 item
      item = mInflater.inflate(R.layout.emoji_group_item_layout, parentPager, false)
      initGroupItem(item, emojiGroup)
      mGroupItems.add(item)
    }
    parentPager.adapter = object : PagerAdapter() {
      override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // 根据分组不同渲染不同的 View
        val groupItem = mGroupItems.get(position)
        container.addView(groupItem)
        return groupItem
      }

      override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
      }

      override fun getCount(): Int {
        return mGroups.size
      }

      override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
      }
    }
    parentPager.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
      override fun onPageSelected(position: Int) {
        // 分组指示器与 ViewPager 联动
        var i = 0
        val count = container.childCount
        while (i < count) {
          mGroups[i].isLastGroup = i == position
          container.getChildAt(i).isSelected = i == position
          i++
        }
        // 当页面被选中的时候，取前一个组的 lastPage 字段
        // 如果 lastPage == 0 则设置当前组选中最后一页
        // 如果 lastPage == lastPager.mAdapter.mCount - 1 则选中第一页
        setChildPagerRealIndex(position)
      }
    })
    var index = -1
    var i = 0
    val size = mGroups.size
    while (i < size) {
      if (mGroups[i].isLastGroup) {
        index = i
        break
      }
      i++
    }
    index = max(index, 0)
    parentPager.currentItem = index
    container.getChildAt(index).isSelected = true
  }

  private fun setChildPagerRealIndex(position: Int) {
    if (mScrollToChange && mLastGroup != null) {
      val lastPager: ViewPager = mGroupItems[mGroups.indexOf(mLastGroup!!)]
        .findViewById(R.id.childPager)
      var realIndex = mLastGroup!!.lastChildIndex
      if (realIndex == 0) {
        realIndex = 99
      } else if (lastPager.adapter != null && realIndex == lastPager.adapter!!.count - 1) {
        realIndex = 0
      }
      (mGroupItems[position].findViewById<ViewPager>(R.id.childPager)).currentItem = realIndex
    }
    mLastGroup = mGroups[position]
    mScrollToChange = true
  }

  private fun initGroupItem(groupItem: View, group: EmojiGroup) {

    val data: Array<Array<Emoji?>>? = EmojiRepo.getGroupData(group)
    val spanCount = if (EmojiHelper.isSmallGroup(group)) 7 else 4
    // 最近使用表情 3 列, 至多 9 条数据
    val hasRecently = spanCount == 7 && data!![0].size <= 9 && data[0].isNotEmpty()
    val indicator: EmojiIndicator = groupItem.findViewById(R.id.childIndicator)
    val childPager: ViewPager = groupItem.findViewById(R.id.childPager)

    childPager.adapter = object : PagerAdapter() {

      override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val pageItem = mInflater.inflate(R.layout.emoji_page_item_layout, container, false)
        initPageData(childPager, pageItem, data?.get(position)!!, position, spanCount, hasRecently)
        container.addView(pageItem)
        return pageItem
      }

      override fun getCount(): Int {
        return data?.size ?: 0
      }

      override fun isViewFromObject(
        view: View,
        `object`: Any
      ): Boolean {
        return view === `object`
      }

      override fun destroyItem(
        container: ViewGroup, position: Int,
        `object`: Any
      ) {
        container.removeView(`object` as View)
      }

      override fun getPageWidth(position: Int): Float {
        return if (position == 0 && hasRecently) 4f / 7 else 1f
      }
    }
    childPager.currentItem = group.lastChildIndex
    childPager.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
      override fun onPageSelected(position: Int) {
        group.lastChildIndex = position
        mLastGroup = group
        if (position == 0) {
          updateRecentlyUsed()
        }
      }

      override fun onPageScrolled(pos: Int, offset: Float, offsetPixels: Int) {
        if ((pos == 0 || pos == data!!.size - 1) && offsetPixels == 0) {
          mLastGroup = group
        }
      }
    })
    indicator.attachViewPager(childPager, hasRecently)
  }

  private fun initPageData(
    pager: ViewPager, pageItem: View, data: Array<Emoji?>,
    index: Int, spanCount: Int, hasRecently: Boolean
  ) {
    val paddingTop: Int = UiHelper.dip2px(if (spanCount == 7) 44F else 32F)
    val gridView: EmojiGridView = pageItem.findViewById(R.id.gridView)
    val adapter = EmojiAdapter(data, spanCount)
    gridView.adapter = adapter
    val recently = pageItem.findViewById<TextView>(R.id.recentlyUsed)
    val divider = pageItem.findViewById<View>(R.id.recentlyDivider)
    gridView.setBackgroundColor(Color.TRANSPARENT)
    gridView.numColumns = if (hasRecently && index == 0) 3 else spanCount
    val columnWidth: Int = UiHelper.screenWidth() / spanCount
    gridView.columnWidth = columnWidth
    if (hasRecently && index == 0) {
      recently.visibility = VISIBLE
      divider.visibility = VISIBLE
      pageItem.setPadding(0, 0, 0, 0)
      gridView.setPadding(0, paddingTop, 0, 0)
      // 第一页的页宽是 ViewPager 页宽的 4/7
      // GridView 的宽度改为 ViewPager 页宽的 3/7
      val params = gridView.layoutParams
      params.width = columnWidth * 3
      gridView.layoutParams = params
      // 剩下的 1/7 页宽除以2,用作 divider 的 leftMargin
      val lp = divider.layoutParams as LayoutParams
      lp.leftMargin = columnWidth / 2
      divider.layoutParams = lp
      mRecentEmojiAdapter = adapter
    } else {
      recently.visibility = GONE
      divider.visibility = GONE
      pageItem.setPadding(0, paddingTop, 0, 0)
      gridView.setPadding(0, 0, 0, 0)
      val params = gridView.layoutParams
      params.width = columnWidth * spanCount
      gridView.layoutParams = params
    }
    if (hasRecently && index == 1) {
      // 第 2 页的第 17 个元素
      adapter.attachToViewPager(pager, 16)
    } else {
      adapter.attachToViewPager(null, -1)
    }
    gridView.selector = ColorDrawable(Color.TRANSPARENT)
    // 连续点击时
    gridView.onItemClickListener =
      AdapterView.OnItemClickListener { _: AdapterView<*>?, view: View, pos: Int, _: Long ->
        val src = adapter.dataSet[pos]
        // 深 copy，不改变原有对象字段
        val emoji = src!!.clone()
        // 拦截删除按钮
        if (hasRecently && index == 1 && pos == 16) {
          val del = view.findViewById<View>(R.id.delIcon)
          emoji.isDelete = del != null && del.alpha >= 0.85f
        } else {
          emoji.isDelete = src.isDelete
        }
        if (emoji.isSmall && !emoji.isDelete && !emoji.isAdd) {
          // 更新最后使用时间
          src.lastUseTime = System.currentTimeMillis()
          recordRecentlyUsed(src)
        }
        emoji.resId = src.resId
        handleClickEvent(emoji)
      }
    gridView.setOnPressListener(object : OnPressListener {

      override fun onLongPress(position: Int) {
        val emoji = adapter.dataSet[position]
        if (emoji!!.isAdd) {
          // 如果长按是添加自定义表情按钮，则走点击的逻辑
          // handleClickEvent(emoji);
        } else if (!emoji.isSmall) {
          handleLongTouchEvent(gridView, emoji, position)
        } else {
          //
        }
      }

      override fun onCancelPress() {
        dismissPopupWin()
      }
    })
  }

  private fun handleClickEvent(emoji: Emoji?) {
    if (emoji == null) {
      LogHelper.debug("EmojiPanel", "handleClickEvent() emoji is null")
      return
    }
    val inputView = mAttachedEditText
    // 细化: 删除、插入、自定义表情、表情包
    // 优先处理特殊的`表情`，比如删除、添加等
    if (emoji.isDelete) {
      // 如调用者处理了删除事件，则内部不再处理
      if (!mCallback!!.onDelete(emoji) && inputView != null) {
        // 删除的只能是小表情或者文字
        val end = inputView.selectionEnd
        if (end <= 0) {
          return
        }
        val editable = inputView.text
        // 如果光标前一个字符是 '\u2005'
        if (editable[end - 1] == '\u2005') {
          val spans = editable.getSpans(0, end, CustomAtSpan::class.java)
          if (spans != null && spans.isNotEmpty()) {
            // 取最后一个元素
            val atSpan = spans[spans.size - 1]
            val st = editable.getSpanStart(atSpan)
            editable.delete(st, end)
          }
        } else {
          var text = editable.toString()
          if (end < text.length) {
            text = text.substring(0, end + 1)
          }
          val index: Int = EmojiHelper.findDeleteIndex(text)
          editable.delete(if (index == 0) 0 else if (index > 0) index else end - 1, end)
        }
      }
    } else if (emoji.isAdd) {
      // 如果是自定义表情的`添加按钮`
    } else if (!mCallback!!.onInsert(emoji) && emoji.isSmall) {
      if (inputView != null) {
        val text: Spannable = EmojiHelper.toSpannable(context, emoji.key, inputView.textSize)
        inputView.text.insert(inputView.selectionEnd, text)
      }
    } else {
      mCallback!!.onSpecialEmoji(emoji)
    }
  }

  private fun handleLongTouchEvent(target: GridView, emoji: Emoji?, index: Int) {
    var child: View? = null
    if (emoji!!.isDelete || target.getChildAt(index).also { child = it } == null) {
      dismissPopupWin()
      return
    }
    val params = EmojiPreviewWindow.Params()
    params.position = index
    child!!.getLocationInWindow(params.loc)
    params.loc[0] -= (child!!.paddingLeft + child!!.paddingRight) / 2
    params.width = child!!.width
    params.height = child!!.height
    params.size = min(params.width, params.height)
    params.text = if (emoji.description == null) null else emoji.description!!.cn
    params.url = if (TextUtils.isEmpty(emoji.path)) emoji.thumbnail else emoji.path
    // LogHelper.debug("EmojiPanel", "handleLongTouchEvent() pos: " + index + ", emoji: "
    //         + emoji + ", position: {x: " + params.loc[0] + ", y: " + params.loc[1] + "}");
    if (mPreviewWin == null) {
      mPreviewWin = EmojiPreviewWindow(context)
    }
    mPreviewWin!!.show(target, params)
  }

  private fun dismissPopupWin() {
    if (mPreviewWin != null) {
      mPreviewWin!!.dismiss()
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    EmojiRepo.registerCallback(object : OnDataChangeCallback {
      override fun onGroupChanged(group: EmojiGroup, isAdd: Boolean) {
        val parentPager: ViewPager = findViewById(R.id.parentPager)
        if (isAdd) {
          val item = mInflater.inflate(R.layout.emoji_group_item_layout, parentPager, false)
          initGroupItem(item, group)
          mGroupItems.add(item)
          mGroups.add(group)
        } else {
          mGroupItems.removeAt(mGroups.indexOf(group))
          mGroups.remove(group)
        }
        if (parentPager.adapter != null) {
          parentPager.adapter!!.notifyDataSetChanged()
        }
      }

      override fun onCustomEmojiChanged() {
        if (!EmojiRepo.updateEmojiMap(mGroups[1], false)) {
          return
        }
        val parentPager: ViewPager = findViewById(R.id.parentPager)
        initGroupItem(mGroupItems[1], mGroups[1])
        if (parentPager.adapter != null) {
          parentPager.adapter!!.notifyDataSetChanged()
        }
      }
    }.also { mDataChangeCallback = it })
  }

  override fun onDetachedFromWindow() {
    // 退出页面时才会回调此方法，由当前页跳转到其他页面时不会回调
    super.onDetachedFromWindow()
    EmojiRepo.unregisterCallback(mDataChangeCallback)
  }

  override fun onVisibilityChanged(changedView: View, v: Int) {
    super.onVisibilityChanged(changedView, v)
    // 前台切后台时： invisible
    // 后台切前台时：visible
    // 退出时：先 invisible 再 onDetachedFromWindow()
    // 初始化时：先 onAttachedToWindow() 再 visible
    Log.d(
      "EmojiPanel", "onVisibilityChanged() we update cache and db. "
          + if (v == VISIBLE) "VISIBLE" else if (v == INVISIBLE) "INVISIBLE" else "GONE"
    )
    updateRecentlyUsed()
    if (v == VISIBLE) {
      return
    }
    // 更新 mLastGroup
    // 没有放在 onDetachedFromWindow() 中执行是因为杀进程时 onDetachedFromWindow() 执行不到
    for (group in mGroups) {
      if (mLastGroup != null) {
        group.isLastGroup = group == mLastGroup
      }
    }
    EmojiRepo.updateLastUsedGroup(mGroups)
  }

  // 更新缓存和数据库（最近使用表情）
  private fun updateRecentlyUsed() {
    val updatedValues = EmojiRepo.updateRecentEmojis() ?: return
    // 刷新 ui
    if (mRecentEmojiAdapter != null) {
      Log.d("EmojiPanel", "updateRecentlyUsed() ${updatedValues[0].contentToString()}")
      mRecentEmojiAdapter!!.updateDataSet(updatedValues[0])
    } else {
      // 如果是最开始的一次使用表情 mRecentEmojiAdapter 将会是 null
      // 此时需要重新初始化父 ViewPager 的第一个item
      ThreadHelper.runOnMain({
        val parentPager: ViewPager = findViewById(R.id.parentPager)
        initGroupItem(mGroupItems[0], mGroups[0])
        if (parentPager.adapter != null) {
          parentPager.adapter!!.notifyDataSetChanged()
        }
      }, 350L)
    }
  }

  private fun recordRecentlyUsed(emoji: Emoji?) {
    EmojiRepo.recordEmoji(emoji!!)
  }

  class OnActionCallback {
    /**
     * @param emoji 被点的表情
     * @return 返回 true 表示调用者自己处理删除事件，反之内部默认处理
     */
    fun onInsert(emoji: Emoji?): Boolean {
      Log.d(TAG, "onInsert() called with: emoji = $emoji")
      return false
    }

    /**
     * 当点击了表情删除按钮
     *
     * @param emoji 被点的表情
     * @return 返回 true 表示调用者自己处理删除事件，反之内部默认处理
     */
    fun onDelete(emoji: Emoji?): Boolean {
      Log.d(TAG, "onDelete() called with: emoji = $emoji")
      return false
    }

    /**
     * 当点击表情包表情中或自定义表情
     *
     * @param emoji 被点表情
     */
    fun onSpecialEmoji(emoji: Emoji?) {
      Log.d(TAG, "onSpecialEmoji() called with: emoji = $emoji")
    }
  }

  init {
    mInflater.inflate(R.layout.emoji_panel_layout, this, true)
    setBackgroundColor(Color.WHITE)
  }

  companion object {
    private const val TAG = "EmojiPanel"
  }
}