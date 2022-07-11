package com.sleticalboy.learning.bean

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

data class ModuleItem @JvmOverloads constructor(
  var title: String = "",
  var clazz: Class<*> = Any::class.java,
  var cls: String = clazz.name
) {

  private val factory = FragmentFactory()

  private fun isValidClass(): Boolean =
    clazz != Any::class.java && clazz.isAssignableFrom(Fragment::class.java)

  fun createFragment(context: Context): Fragment? {
    if (isValidClass()) {
      return factory.instantiate(context.classLoader, cls)
    }
    return null
  }
}