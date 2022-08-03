package com.binlee.learning.util

object UnicodeUtils {

  /**
   * 字符串转换 unicode
   */
  fun string2Unicode(string: String): String {
    val unicode = StringBuilder()
    for (i in 0 until string.length) {
      // 取出每一个字符
      val c = string[i]
      // 转换为unicode
      unicode.append("\\u").append(Integer.toHexString(c.code))
    }
    return unicode.toString()
  }

  /**
   * unicode 转字符串
   */
  fun unicode2String(unicode: String): String {
    val string = StringBuilder()
    val hex = unicode.split("\\\\u".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    for (i in 1 until hex.size) {
      // 转换出每一个代码点
      val data = Integer.parseInt(hex[i], 16)
      // 追加成string
      string.append(data.toChar())
    }
    return string.toString()
  }

  /**
   * string 转 char
   */
  fun string2char(string: String): Char {
    var integer = 0
    for (i in 0 until string.length) {
      // 取出每一个字符
      val c = string[i]
      integer += c.code
    }
    integer = integer % 95 + 32
    return integer.toChar()
  }
}
