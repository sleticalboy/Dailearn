package com.example.freevideo.db;

import com.example.freevideo.DyState;
import com.binlee.sqlite.orm.Converter;

/**
 * Created on 2022/11/2
 *
 * @author binlee
 */
public final class StateConverter extends Converter<DyState, Integer> {

  @Override public Integer encode(DyState input) {
    return input.ordinal();
  }

  @Override public DyState decode(Integer input) {
    return DyState.values()[input];
  }
}
