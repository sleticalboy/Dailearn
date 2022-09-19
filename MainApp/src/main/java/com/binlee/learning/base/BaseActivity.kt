package com.binlee.learning.base

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.binlee.dl.PluginManager

/**
 * Created on 18-2-1.
 *
 * @author leebin
 * @version 1.0
 */
abstract class BaseActivity : AppCompatActivity() {

  protected val requestCode = 0x12

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    prepareWork(savedInstanceState)
    setContentView(layout())
    initView()
    initData()
  }

  override fun onStart() {
    super.onStart()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      val perms = requiredPermissions()
      if (perms.isEmpty()) return
      requestPermissions(perms, this.requestCode)
    }
  }

  protected open fun requiredPermissions(): Array<String> {
    return arrayOf()
  }

  protected abstract fun layout(): View

  protected abstract fun initView()

  protected open fun initData() {}

  protected open fun prepareWork(savedInstanceState: Bundle?) {}

  protected open fun logTag(): String = "BaseActivity"

}
