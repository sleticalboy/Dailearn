package com.example.freevideo.db;

import com.binlee.sqlite.orm.Converter;
import com.example.freevideo.DySource;

/**
 * Created on 2022/11/05
 *
 * @author binlee
 */
public final class SourceConverter extends Converter<DySource, Integer> {
  @Override
  public Integer encode(final DySource input) {
    return input.ordinal();
  }

  @Override
  public DySource decode(final Integer input) {
    return DySource.values()[input];
  }
}
