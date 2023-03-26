package com.binlee.learning.bean

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

data class ModuleItem @JvmOverloads constructor(
  var title: String = "",
  var clazz: Class<*>? = Any::class.java,
  var cls: String? = clazz?.name
) {

  private val factory = FragmentFactory()

  constructor(title: String, cls: String) : this(title, Any::class.java, cls)

  private fun isValidClass(): Boolean =
    clazz != Any::class.java && clazz?.isAssignableFrom(Fragment::class.java) == true

  fun createFragment(context: Context): Fragment? {
    if (isValidClass()) {
      return cls?.let { factory.instantiate(context.classLoader, it) }
    }
    return null
  }
}