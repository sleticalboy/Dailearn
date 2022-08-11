package com.binlee.emoji.generic;

import androidx.annotation.NonNull;

/**
 * Created on 20-9-17.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class UpperHolder {

  private BaseGeneric mValue;

  public <T extends BaseGeneric> void setValue(T value) {
    mValue = value;
  }

  public BaseGeneric getValue() {
    return mValue;
  }

  @NonNull
  @Override
  public String toString() {
    return "UpperHolder{" +
      "key=" + mValue.getKey() +
      "value=" + mValue.getValue() +
      '}';
  }
}
