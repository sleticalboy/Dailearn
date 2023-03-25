package com.binlee.learning.csv

import android.util.Log
import android.view.View
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.bean.AudioItem
import com.binlee.learning.databinding.ActivityGridViewBinding

/**
 * Created on 20-9-1.
 *
 * @author binlee sleticalboy@gmail.com
 */
class GridViewActivity : BaseActivity() {

  private var mBind: ActivityGridViewBinding? = null
  private var mData: ArrayList<AudioItem>? = null
  private var mSelection: Int = -1

  override fun initData() {
    if (mSelection == -1) {
      mSelection = intent.getIntExtra("mock_data", 26)
    }
    if (mData == null) {
      mData = ArrayList()
      var item: AudioItem
      for (index in 0..42) {
        item = AudioItem()
        item.mTitle = "Item ${index + 1}"
        item.mSummary = "Item ${index + 1}"
        if (mSelection == index) {
          item.mColor = android.R.color.holo_red_light
          item.mColor2 = android.R.color.holo_blue_light
        } else {
          item.mColor = android.R.color.black
          item.mColor2 = android.R.color.black
        }
        mData!!.add(item)
      }
    }
    Log.d(TAG, "initData() selection: $mSelection, data size: ${mData!!.size}")
  }

  override fun layout(): View {
    // R.layout.activity_grid_view
    mBind = ActivityGridViewBinding.inflate(layoutInflater)
    return mBind!!.root
  }

  override fun initView() {
    mBind!!.btnShowDialog.setOnClickListener {
      showGridDialog()
    }
  }

  private fun showGridDialog() {
    val dialog = GridDialog()
    dialog.setSelection(mSelection)
    dialog.setData(mData!!)
    dialog.show(supportFragmentManager, dialog.javaClass.name)
  }
}