package com.example.dyvd.db.converter;

import com.example.dyvd.DyState;
import com.example.dyvd.db.Converter;

/**
 * Created on 2022/11/2
 *
 * @author binlee
 */
public final class StateConverter implements Converter<DyState, Integer> {

  @Override public Integer encode(DyState input) {
    return input.ordinal();
  }

  @Override public DyState decode(Integer input) {
    return DyState.values()[input];
  }
}
