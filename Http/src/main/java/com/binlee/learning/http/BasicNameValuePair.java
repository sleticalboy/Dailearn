package com.binlee.learning.http;

import androidx.annotation.NonNull;

/**
 * Created on 18-9-19.
 *
 * @author leebin
 */
public final class BasicNameValuePair implements NameValuePair {

  private static final long serialVersionUID = 5830785193629439085L;

  private final String name;
  private final String value;

  public BasicNameValuePair(@NonNull final String name, final String value) {
    this.name = name;
    this.value = value == null ? "" : value;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getValue() {
    return value;
  }
}
