package com.binlee.emoji

import com.binlee.emoji.compat.ImageEngine
import com.binlee.emoji.third.GlideImageEngine

class ImageAdapter private constructor() {

  companion object {

    private var sEngine: ImageEngine? = null

    fun setImageEngine(engine: ImageEngine?) {
      if (engine == null) {
        throw NullPointerException("setImageEngine(): engine is null")
      }
      sEngine = engine
    }

    fun engine(): ImageEngine {
      if (sEngine == null) {
        sEngine = GlideImageEngine()
      }
      return sEngine!!
    }
  }

  init {
    throw AssertionError("no instance.")
  }
}