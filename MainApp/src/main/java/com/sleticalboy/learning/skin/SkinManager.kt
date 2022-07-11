package com.sleticalboy.learning.skin

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.util.Log
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

/**
 * Created on 20-9-10.
 *
 * @author binlee sleticalboy@gmail.com
 */
class SkinManager {

  private lateinit var mContext: Context
  private var mResource: Resources? = null
  private var mPackageName: String? = null

  fun init(context: Context) {
    get().mContext = context
  }

  @Throws(Exception::class)
  fun loadSkin(url: String) {
    val assets = AssetManager::class.java.newInstance()
    val m = AssetManager::class.java.getDeclaredMethod("addAssetPath", String::class.java)
    m.isAccessible = true
    val ret = m.invoke(assets, url)
    Log.d(TAG, "invoke addAssetPath() ret: $ret")

    mResource =
      Resources(assets, mContext.resources.displayMetrics, mContext.resources.configuration)

    mPackageName = mContext.packageManager.getPackageArchiveInfo(
      url,
      PackageManager.GET_ACTIVITIES
    )!!.packageName
    Log.d(TAG, "get package name by PackageManager: $mPackageName")
  }

  fun getColor(@ColorRes oldRes: Int): Int {
    if (mResource == null) return ContextCompat.getColor(mContext, oldRes)
    val name = mContext.resources.getResourceEntryName(oldRes)
    val defType = mContext.resources.getResourceTypeName(oldRes)
    val id = mResource!!.getIdentifier(name, defType, mPackageName)
    if (id == 0) return ContextCompat.getColor(mContext, oldRes)
    return mResource!!.getColor(id)
  }

  companion object {

    const val TAG = "SkinManager"

    fun get(): SkinManager = SkinManager()
  }
}