package com.binlee.dl.util;

import java.lang.reflect.Array;

/**
 * Created on 19-4-5.
 *
 * @author leebin
 */
public final class ArrayUtils {

  public static Object merge(Object left, Object right) {
    if (left == null && right == null) return null;
    if (left == null) return right;
    if (right == null) return left;
    // 获取返回值类型
    final Class<?> retType = left.getClass().getComponentType();
    if (retType == null) throw new RuntimeException(left + " is not an array type");

    // 修复包的数组长度
    final int len = Array.getLength(left);
    // 系统原有的数组长度
    final int lenOld = Array.getLength(right);
    // 新数组
    final Object ret = Array.newInstance(retType, len + lenOld);
    for (int i = 0; i < len + lenOld; i++) {
      if (i < len) {
        // 首先从修复包的数组开始遍历添加到新数组
        Array.set(ret, i, Array.get(left, i));
      } else {
        // 接着从系统原有的数组开始遍历添加到新数组
        Array.set(ret, i, Array.get(right, i - len));
      }
    }
    return ret;
  }
}
