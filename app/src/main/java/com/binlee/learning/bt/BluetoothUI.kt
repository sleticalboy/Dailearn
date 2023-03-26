package com.binlee.learning.bt

import android.Manifest
import android.util.Log
import android.view.View
import com.binlee.learning.R
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.databinding.ActivityBluetoothMainBinding

class BluetoothUI : BaseActivity() {

  private var mBind: ActivityBluetoothMainBinding? = null

  override fun layout(): View {
    // R.layout.activity_bluetooth_main
    mBind = ActivityBluetoothMainBinding.inflate(layoutInflater)
    return mBind!!.root
  }

  override fun onStart() {
    super.onStart()
    askPermission(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
  }

  override fun whenPermissionResult(permissions: Array<out String>, grantResults: BooleanArray) {
    if (grantResults[0]) {
      Log.d(TAG, "onRequestPermissionsResult() permission granted.")
    }
  }

  override fun initView() {
    mBind!!.btCommon.setOnClickListener {
      // open common fragment
      showFragment(CommonBtFragment::class.java.name)
    }
    mBind!!.ble.setOnClickListener {
      // open ble fragment
      showFragment(BleFragment::class.java.name)
    }
  }

  private fun showFragment(tag: String) {
    val transaction = supportFragmentManager.beginTransaction()
    var f = supportFragmentManager.findFragmentByTag(tag)
    if (f == null) {
      f = supportFragmentManager.fragmentFactory.instantiate(classLoader, tag)
      transaction.replace(R.id.fragmentRoot, f, tag)
    } else {
      if (f.isAdded) transaction.show(f)
    }
    transaction.commitAllowingStateLoss()
    supportFragmentManager.executePendingTransactions()
  }
}