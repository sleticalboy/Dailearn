package com.binlee.emoji.generic;

/**
 * Created on 20-9-17.
 *
 * @author Ben binli@grandstream.cn
 */
public interface IGeneric<K, V> {

  K getKey();

  V getValue();
}
