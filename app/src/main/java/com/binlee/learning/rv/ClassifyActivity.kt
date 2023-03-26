package com.binlee.learning.rv

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.binlee.learning.R
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.bean.AppInfo
import com.binlee.learning.databinding.ActivityClassifyBinding
import com.binlee.learning.weight.xrecycler.decoration.DividerGridItemDecoration
import java.util.ArrayList

/**
 * Created on 18-2-12.
 *
 * @author leebin
 * @version 1.0
 * @description
 */
class ClassifyActivity : BaseActivity() {

  private val mAppList = ArrayList<AppInfo>()
  private var mAdapter: AppInfoAdapter? = null

  override fun initData() {
    val appInfoList = ArrayList<AppInfo>()
    appInfoList.add(AppInfo("", "common", -1, true))
    appInfoList.add(AppInfo("找同事", "", mId, false))
    appInfoList.add(AppInfo("敏行介绍", "", mId, false))
    appInfoList.add(AppInfo("热度帮", "", mId, false))
    appInfoList.add(AppInfo("敏邮", "", mId, false))
    appInfoList.add(AppInfo("抽奖", "", mId, false))
    appInfoList.add(AppInfo("后勤服务", "", mId, false))
    appInfoList.add(AppInfo("签到", "", mId, false))
    appInfoList.add(AppInfo("审批", "", mId, false))
    appInfoList.add(AppInfo("找同事", "", mId, false))
    appInfoList.add(AppInfo("敏行介绍", "", mId, false))
    appInfoList.add(AppInfo("热度帮", "", mId, false))
    appInfoList.add(AppInfo("敏邮", "", mId, false))
    appInfoList.add(AppInfo("抽奖", "", mId, false))
    appInfoList.add(AppInfo("后勤服务", "", mId, false))
    appInfoList.add(AppInfo("签到", "", mId, false))
    appInfoList.add(AppInfo("审批", "", mId, false))

    appInfoList.add(AppInfo("", "work", -1, true))
    appInfoList.add(AppInfo("公司公告", "", mId, false))
    appInfoList.add(AppInfo("待办审批", "", mId, false))
    appInfoList.add(AppInfo("OA演示", "", mId, false))
    appInfoList.add(AppInfo("签到", "", mId, false))
    appInfoList.add(AppInfo("IP电话", "", mId, false))
    appInfoList.add(AppInfo("公司公告", "", mId, false))
    appInfoList.add(AppInfo("待办审批", "", mId, false))
    appInfoList.add(AppInfo("OA演示", "", mId, false))
    appInfoList.add(AppInfo("签到", "", mId, false))
    appInfoList.add(AppInfo("IP电话", "", mId, false))
    appInfoList.add(AppInfo("敏邮", "", mId, false))
    appInfoList.add(AppInfo("抽奖", "", mId, false))
    appInfoList.add(AppInfo("后勤服务", "", mId, false))
    appInfoList.add(AppInfo("签到", "", mId, false))
    appInfoList.add(AppInfo("审批", "", mId, false))
    appInfoList.add(AppInfo("找同事", "", mId, false))
    appInfoList.add(AppInfo("敏行介绍", "", mId, false))
    appInfoList.add(AppInfo("热度帮", "", mId, false))
    appInfoList.add(AppInfo("敏邮", "", mId, false))

    appInfoList.add(AppInfo("", "test", -1, true))
    appInfoList.add(AppInfo("js api 兼容性", "", mId, false))
    appInfoList.add(AppInfo("Web RTC 视频", "", mId, false))
    appInfoList.add(AppInfo("H5 API Demo", "", mId, false))
    appInfoList.add(AppInfo("js api 兼容性", "", mId, false))
    appInfoList.add(AppInfo("Web RTC 视频", "", mId, false))
    appInfoList.add(AppInfo("H5 API Demo", "", mId, false))
    appInfoList.add(AppInfo("红包帐号应用", "", mId, false))
    appInfoList.add(AppInfo("通知", "", mId, false))
    appInfoList.add(AppInfo("百度", "", mId, false))
    appInfoList.add(AppInfo("iboss", "", mId, false))
    appInfoList.add(AppInfo("调试开关", "", mId, false))
    appInfoList.add(AppInfo("话题", "", mId, false))
    appInfoList.add(AppInfo("ADT", "", mId, false))
    appInfoList.add(AppInfo("365 体验版", "", mId, false))

    appInfoList.add(AppInfo("", "others", -1, true))
    appInfoList.add(AppInfo("红包帐号应用", "", mId, false))
    appInfoList.add(AppInfo("通知", "", mId, false))
    appInfoList.add(AppInfo("百度", "", mId, false))
    appInfoList.add(AppInfo("iboss", "", mId, false))
    appInfoList.add(AppInfo("调试开关", "", mId, false))
    appInfoList.add(AppInfo("话题", "", mId, false))
    appInfoList.add(AppInfo("ADT", "", mId, false))
    appInfoList.add(AppInfo("365 体验版", "", mId, false))
    appInfoList.add(AppInfo("js api 兼容性", "", mId, false))
    appInfoList.add(AppInfo("Web RTC 视频", "", mId, false))
    appInfoList.add(AppInfo("H5 API Demo", "", mId, false))
    mAppList.addAll(appInfoList)
    mAdapter!!.notifyDataSetChanged()
  }

  override fun initView() {

    val layoutManager = GridLayoutManager(this, 3)
    layoutManager.orientation = LinearLayoutManager.VERTICAL
    layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        // title 占 3 格，普通 item 占 1 格
        return if (mAdapter!!.getItemViewType(position) == TYPE_TITLE) layoutManager.spanCount else 1
      }
    }
    mBind!!.recyclerView.layoutManager = layoutManager
    mBind!!.recyclerView.addItemDecoration(DividerGridItemDecoration(this, 8))

    mAdapter = AppInfoAdapter(this, mAppList)
    mBind!!.recyclerView.adapter = mAdapter
  }

  private var mBind: ActivityClassifyBinding? = null

  override fun layout(): View {
    // return R.layout.activity_classify
    mBind = ActivityClassifyBinding.inflate(layoutInflater)
    return mBind!!.root
  }

  internal class AppInfoAdapter(private var mContext: Context, private var mList: List<AppInfo>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mInflater: LayoutInflater = LayoutInflater.from(mContext)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
      return when (viewType) {
        TYPE_TITLE -> ClassifyHolder(
          mInflater.inflate(
            R.layout.item_classify_title,
            parent,
            false
          )
        ) /*(viewType == TYPE_CHILD)*/
        else -> AppInfoHolder(mInflater.inflate(R.layout.item_app_layout, parent, false))
      }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
      val appInfo = mList[position]
      when (holder.itemViewType) {
        TYPE_TITLE -> {
          val classifyHolder = holder as ClassifyHolder
          classifyHolder.tv_classify_title.text = appInfo.category
          classifyHolder.itemView.setOnClickListener {
            Toast.makeText(mContext, appInfo.category, Toast.LENGTH_SHORT).show()
          }
        }
        TYPE_CHILD -> {
          val appInfoHolder = holder as AppInfoHolder
          appInfoHolder.tv_app_name.text = appInfo.name
          appInfoHolder.iv_app_icon.setImageResource(appInfo.imgId)
          appInfoHolder.itemView.setOnClickListener {
            Toast.makeText(mContext, appInfo.name, Toast.LENGTH_SHORT).show()
          }
        }
      }
    }

    override fun getItemCount(): Int {
      return mList.size
    }

    override fun getItemViewType(position: Int): Int {
      val isGroup = mList[position].isGroup
      return if (!isGroup) {
        TYPE_CHILD
      } else {
        TYPE_TITLE
      }
    }
  }

  class AppInfoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val iv_app_icon: ImageView = itemView.findViewById(R.id.iv_app_icon)
    val tv_app_name: TextView = itemView.findViewById(R.id.tv_app_name)
  }

  internal class ClassifyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tv_classify_title: TextView = itemView.findViewById(R.id.tv_classify_title)
  }

  companion object {
    val TYPE_TITLE = 0x001
    val TYPE_CHILD = 0x002
    private val mId = R.mipmap.ic_launcher
  }
}
