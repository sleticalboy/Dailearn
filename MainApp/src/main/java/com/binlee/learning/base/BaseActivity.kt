package com.binlee.learning.base

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

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

  protected fun hasPermission(perm: String): Boolean {
    return ActivityCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
  }

  protected fun askPermission(permissions: Array<String>) {
    ActivityCompat.requestPermissions(this, permissions, PERM_REQUEST_CODE)
  }

  protected open fun whenPermissionResult(permissions: Array<out String>, grantResults: BooleanArray) {
  }

  final override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
    grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == PERM_REQUEST_CODE) {
      val results = BooleanArray(grantResults.size)
      for (i in grantResults.indices) {
        results[i] = grantResults[i] == PackageManager.PERMISSION_GRANTED
      }
      whenPermissionResult(permissions, results)
    }
  }

  protected abstract fun layout(): View

  protected abstract fun initView()

  protected open fun initData() {}

  protected open fun prepareWork(savedInstanceState: Bundle?) {}

  protected open fun logTag(): String = javaClass.simpleName.replace(Regex("Activity|UI"), "")

  companion object {
    @JvmStatic
    protected val PERM_REQUEST_CODE = 0x12
  }

}
