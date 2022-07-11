package com.sleticalboy.learning.data

/**
 * Created on 20-3-27.
 *
 * @author binlee sleticalboy@gmail.com
 */
class DataEngine private constructor() {

  private var mIndexModel: IndexModel? = null

  fun indexModel(): IndexModel {
    if (mIndexModel == null) {
      mIndexModel = IndexModel()
    }
    return mIndexModel!!
  }

  companion object {

    private val sDataEngine = DataEngine()

    fun get(): DataEngine = sDataEngine
  }
}