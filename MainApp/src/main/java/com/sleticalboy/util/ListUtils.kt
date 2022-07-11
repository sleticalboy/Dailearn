package com.sleticalboy.util

/**
 * Created on 18-2-7.
 *
 * @author leebin
 * @version 1.0
 * @description
 */
object ListUtils {

  fun <E> exchangeItem(source: MutableList<E>, from: Int, to: Int) {
    if (isEmpty(source)) {
      return
    }
    val fromItem = source[from]
    val toItem = source[to]
    source[from] = toItem
    source[to] = fromItem
  }

  fun <E> equals(actual: List<E>?, excepted: List<E>?): Boolean {
    if (actual == null) {
      return excepted == null
    }
    if (excepted == null) {
      return false
    }
    if (actual.size != excepted.size) {
      return false
    }
    for (i in actual.indices) {
      if (!ObjectUtils.equals(actual[i], excepted[i])) {
        return false
      }
    }
    return true
  }

  fun <E> getSize(source: List<E>): Int {
    return if (isEmpty(source)) 0 else source.size
  }

  fun <E> isEmpty(source: List<E>?): Boolean {
    return source == null || source.isEmpty()
  }

  fun <E> relocation(source: MutableList<E>, from: Int, to: Int) {
    if (isEmpty(source)) {
      return
    }
    val fromItem = source.removeAt(from)
    source.add(to, fromItem)
  }
}
