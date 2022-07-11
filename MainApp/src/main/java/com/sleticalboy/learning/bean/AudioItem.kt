package com.sleticalboy.learning.bean

/**
 * Created on 20-9-3.
 *
 * @author Ben binli@grandstream.cn
 */
class AudioItem {
  var mTitle: String? = null
  var mSummary: String? = null
  var mColor = 0
  var mColor2 = 0
  override fun toString(): String {
    return "AudioItem{" +
        "mTitle='" + mTitle + '\'' +
        ", mSummary='" + mSummary + '\'' +
        ", mColor=" + mColor +
        ", mColor2=" + mColor2 +
        '}'
  }
}