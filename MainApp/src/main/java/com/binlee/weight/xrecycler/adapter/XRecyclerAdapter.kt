package com.binlee.weight.xrecycler.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

/**
 * Created on 18-2-7.
 *
 * @author leebin
 * @version 1.0
 * @description
 */
abstract class XRecyclerAdapter<M> : RecyclerView.Adapter<XBaseHolder<M>> {

  private val mLock = Any()
  private val mHeaders: MutableList<HeaderView> = ArrayList()
  private val mFooters: MutableList<FooterView> = ArrayList()
  private var mOnItemClickListener: OnItemClickListener? = null
  private var mOnItemLongClickListener: OnItemLongClickListener? = null
  private var mDataList: MutableList<M>? = null
  private val mRecyclerView: RecyclerView? = null
  private var mNotifyOnChange = false
  private val count: Int? = 0

  var context: Context? = null
    private set

  constructor(context: Context) {
    init(context, ArrayList())
  }

  private fun init(context: Context, dataList: List<M>) {
    this.context = context
    mDataList = ArrayList(dataList)
  }

  constructor(context: Context, dataArray: Array<M>) {
    init(context, listOf(*dataArray))
  }

  constructor(context: Context, dataList: List<M>) {
    init(context, dataList)
  }

  final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): XBaseHolder<M> {
    val view = createViewByViewType(parent, viewType)
    if (view != null) {
      return PlaceHolder(view)
    }
    val holder = onCreateItemHolder(parent, viewType)
    // final int position = holder.getAdapterPosition() - mHeaders.size();
    val position = holder.adapterPosition
    if (mOnItemClickListener != null) {
      holder.itemView.setOnClickListener { mOnItemClickListener!!.onItemClick(position) }
    }
    if (mOnItemLongClickListener != null) {
      holder.itemView.setOnLongClickListener { mOnItemLongClickListener!!.onItemLongClick(position) }
    }
    return holder
  }

  private fun createViewByViewType(parent: ViewGroup, viewType: Int): View? {
    for (headerView in mHeaders) {
      if (headerView.hashCode() == viewType) {
        return getView(parent, headerView)
      }
    }
    for (footerView in mFooters) {
      if (footerView.hashCode() == viewType) {
        return getView(parent, footerView)
      }
    }
    return null
  }

  protected abstract fun onCreateItemHolder(parent: ViewGroup, viewType: Int): XBaseHolder<M>

  private fun getView(parent: ViewGroup, itemView: ItemView): View {
    val view = itemView.onCreateView(parent)
    val params: StaggeredGridLayoutManager.LayoutParams
    params =
      if (view.layoutParams != null) StaggeredGridLayoutManager.LayoutParams(view.layoutParams) else StaggeredGridLayoutManager.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
      )
    params.isFullSpan = true
    view.layoutParams = params
    return view
  }

  override fun onBindViewHolder(holder: XBaseHolder<M>, position: Int) {
    holder.itemView.id = position
    if (mHeaders.size != 0 && position < mHeaders.size) {
      mHeaders[position].onBindView(holder.itemView)
      return
    }
    if (mFooters.size != 0 && position >= mHeaders.size + mDataList!!.size) {
      mFooters[position - mHeaders.size - mDataList!!.size].onBindView(holder.itemView)
    }
    holder.bindData(getItemData(position))
  }

  /**
   * 获取 item 数据
   *
   * @param position
   * @return
   */
  open fun getItemData(position: Int): M {
    return mDataList!![position]
  }

  override fun getItemViewType(position: Int): Int {
    if (mHeaders.size != 0) {
      if (position < mHeaders.size) {
        return mHeaders[position].hashCode()
      }
    }
    if (mFooters.size != 0) {
      if (position >= mHeaders.size + mDataList!!.size) {
        return mFooters[position - mHeaders.size - mDataList!!.size].hashCode()
      }
    }
    return getViewType(position - mHeaders.size)
  }

  private fun getViewType(position: Int): Int {
    return 0
  }

  override fun getItemCount(): Int {
    return mHeaders.size + getCount() + mFooters.size
  }

  fun setData(dataList: MutableList<M>?) {
    mDataList = dataList
  }

  fun setData(dataArray: Array<M>) {
    mDataList = dataArray.toMutableList()
  }

  val allData: List<M>?
    get() = mDataList

  fun getPosition(item: M): Int {
    return mDataList!!.indexOf(item)
  }

  fun add(`object`: M) {
    add(getCount(), `object`)
  }

  // --------------------对数据的一些操作-------------------
  fun add(index: Int, `object`: M?) {
    if (index > getCount() || index < 0) {
      throw IndexOutOfBoundsException("Index: $index, Size: $count")
    }
    if (`object` != null) {
      synchronized(mLock) { mDataList!!.add(index, `object`) }
    }
    if (mNotifyOnChange) {
      notifyItemInserted(index)
    }
  }

  fun addAll(collection: Collection<M>?) {
    addAll(getCount(), collection)
  }

  fun addAll(index: Int, collection: Collection<M>?) {
    if (index > getCount() || index < 0) {
      throw IndexOutOfBoundsException("Index: $index, Size: $count")
    }
    if (collection != null && collection.isNotEmpty()) {
      synchronized(mLock) { mDataList!!.addAll(index, collection) }
    }
    val dataCount = collection?.size ?: 0
    if (mNotifyOnChange) {
      notifyItemRangeInserted(index, dataCount)
    }
  }

  fun update(position: Int, `object`: M) {
    synchronized(mLock) { mDataList!!.set(position, `object`) }
    if (mNotifyOnChange) {
      notifyItemChanged(position)
    }
  }

  fun remove(`object`: M) {
    val position = mDataList!!.indexOf(`object`)
    synchronized(mLock) {
      if (mDataList!!.remove(`object`)) {
        if (mNotifyOnChange) {
          notifyItemRemoved(position)
        }
      }
    }
  }

  fun remove(position: Int) {
    synchronized(mLock) { mDataList!!.removeAt(position) }
    if (mNotifyOnChange) {
      notifyItemRemoved(position)
    }
  }

  fun removeAll() {
    val count = mDataList!!.size
    synchronized(mLock) { mDataList!!.clear() }
    if (mNotifyOnChange) {
      notifyItemRangeChanged(0, count)
    }
  }

  fun clear() {
    val count = mDataList!!.size
    synchronized(mLock) { mDataList!!.clear() }
    if (mNotifyOnChange) {
      notifyDataSetChanged()
    }
  }

  fun sort(comparator: Comparator<in M>?) {
    synchronized(mLock) { Collections.sort(mDataList, comparator) }
    if (mNotifyOnChange) {
      notifyDataSetChanged()
    }
  }

  fun addHeader(headerView: HeaderView?) {
    if (headerView == null) {
      throw NullPointerException("view can not be null")
    }
    mHeaders.add(headerView)
    notifyItemInserted(mHeaders.size - 1)
  }

  // --------------------对数据的一些操作-------------------
  // -------------对 header 和 footer 的一些操作------------
  fun getHeader(position: Int): HeaderView {
    return mHeaders[position]
  }

  fun removeHeader(headerView: HeaderView?) {
    val position = mHeaders.indexOf(headerView)
    mHeaders.remove(headerView)
    notifyItemRemoved(position)
  }

  fun removeAllHeaders() {
    val count = mHeaders.size
    mHeaders.clear()
    notifyItemRangeRemoved(0, count)
  }

  fun addFooter(footerView: FooterView?) {
    if (footerView == null) {
      throw NullPointerException("view can not be null")
    }
    mFooters.add(footerView)
    notifyItemInserted(mFooters.size - 1)
  }

  fun getFooter(position: Int): FooterView {
    return mFooters[position]
  }

  fun removeFooter(footerView: FooterView?) {
    val position = mFooters.indexOf(footerView)
    mFooters.remove(footerView)
    notifyItemRemoved(position)
  }

  fun removeAllFooters() {
    val count = mFooters.size
    mFooters.clear()
    notifyItemRangeRemoved(0, count)
  }

  val headersCount: Int get() = mHeaders.size

  val footersCount: Int get() = mFooters.size

  fun setNotifyOnChange(notifyOnChange: Boolean) {
    mNotifyOnChange = notifyOnChange
  }

  // -------------对 header 和 footer 的一些操作------------
  fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
    mOnItemClickListener = onItemClickListener
  }

  fun setOnItemLongClickListener(onItemLongClickListener: OnItemLongClickListener?) {
    mOnItemLongClickListener = onItemLongClickListener
  }

  /**
   * Header or Footer
   */
  interface ItemView {
    /**
     * Call when the item was created
     *
     * @return the View itself
     */
    fun onCreateView(parent: ViewGroup?): View

    /**
     * Called when  binding view
     *
     * @param itemView item view
     */
    fun onBindView(itemView: View?)
  }

  interface HeaderView : ItemView
  interface FooterView : ItemView
  interface OnItemClickListener {
    fun onItemClick(position: Int)
  }

  interface OnItemLongClickListener {
    fun onItemLongClick(position: Int): Boolean
  }

  // placeholder
  private inner class PlaceHolder(itemView: View?) : XBaseHolder<M>(itemView) {

    override fun bindData(data: M) {
    }
  }

  protected open fun getCount(): Int {
    return mDataList!!.size
  }
}