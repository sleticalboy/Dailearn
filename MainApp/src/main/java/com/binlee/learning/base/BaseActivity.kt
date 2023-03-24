package com.binlee.learning.base

import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

/**
 * Created on 18-2-1.
 *
 * @author leebin
 * @version 1.0
 */
abstract class BaseActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    prepareWork(savedInstanceState)
    setContentView(layout())
    initView()
    initData()
  }

  override fun onStart() {
    super.onStart()
    if (VERSION.SDK_INT >= VERSION_CODES.M) {
      val perms = requiredPermissions()
      if (perms.isNotEmpty()) requestPermissions(perms, PERM_REQUEST_CODE)
    }
  }

  protected open fun requiredPermissions(): Array<String> {
    return arrayOf()
  }

  protected fun hasPermission(perm: String): Boolean {
    return if (VERSION.SDK_INT >= VERSION_CODES.M) {
      checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }

  protected abstract fun layout(): View

  protected abstract fun initView()

  protected open fun initData() {}

  protected open fun prepareWork(savedInstanceState: Bundle?) {}

  protected open fun logTag(): String = javaClass.simpleName

  companion object {
    @JvmStatic
    protected val PERM_REQUEST_CODE = 0x12
  }

}
