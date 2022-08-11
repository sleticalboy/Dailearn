package com.binlee.emoji.generic;

import androidx.annotation.NonNull;

/**
 * Created on 20-9-17.
 *
 * @author Ben binli@grandstream.cn
 */
public class SimpleGeneric implements IGeneric<String, Integer> {

  public final String mKey;
  public final int mValue;

  public SimpleGeneric(String key, int value) {
    mKey = key;
    mValue = value;
  }

  @NonNull
  @Override
  public String toString() {
    return "Generic{" +
      "mKey=" + getKey() +
      ", mValue=" + getValue() +
      '}';
  }

  @Override
  public String getKey() {
    return mKey;
  }

  @Override
  public Integer getValue() {
    return mValue;
  }
}
