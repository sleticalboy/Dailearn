package com.sleticalboy.util

object AsciiUtils {

  /*****char&ascii */
  fun ascii2Char(ASCII: Int): Char {
    return ASCII.toChar()
  }

  fun char2ASCII(c: Char): Int {
    return c.code
  }

  /*****string&ascii (Int型) */
  fun ascii2String(ASCIIs: IntArray): String {
    val sb = StringBuffer()
    for (i in ASCIIs.indices) {
      sb.append(ascii2Char(ASCIIs[i]))
    }
    return sb.toString()
  }

  /*****string&ascii */
  fun ascii2String(ASCIIs: String): String {
    val ASCIIss = ASCIIs.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val sb = StringBuffer()
    for (i in ASCIIss.indices) {
      sb.append(ascii2Char(Integer.parseInt(ASCIIss[i])))
    }
    return sb.toString()
  }

  fun string2ASCII(s: String?): IntArray? { // 字符串转换为ASCII码
    if (s == null || "" == s) {
      return null
    }

    val chars = s.toCharArray()
    val asciiArray = IntArray(chars.size)

    for (i in chars.indices) {
      asciiArray[i] = char2ASCII(chars[i])
    }
    return asciiArray
  }
}
