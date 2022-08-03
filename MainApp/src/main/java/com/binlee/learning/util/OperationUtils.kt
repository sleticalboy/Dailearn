package com.binlee.learning.util

import java.util.regex.Pattern

object OperationUtils {

  fun isNumeric(str: String?): Boolean {
    val pattern = Pattern.compile("[0-9]*")
    val isNum = pattern.matcher(str)
    return isNum.matches()
  }

  fun secretKey(): String {
    var resultStr = ""
    for (i in 0..15) {
      val x = (Math.random() * 95).toInt() + 32
      val c = x.toChar()
      resultStr += c
    }
    return resultStr
  }

  fun convertKey(inStr: String?, c: Char): String {
    val a = inStr!!.toCharArray()
    for (i in inStr.toCharArray().indices) {
      a[i] = (a[i].code xor c.code).toChar()
    }
    return String(a)
  }
}