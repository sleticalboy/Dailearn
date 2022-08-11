package com.binlee.emoji.generic;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created on 20-9-17.
 *
 * @author Ben binli@grandstream.cn
 */
public final class ComplexGeneric implements IGeneric<String, Map<String, List<Set<IGeneric<String, Integer>>>>> {
  @Override
  public String getKey() {
    return null;
  }

  @Override
  public Map<String, List<Set<IGeneric<String, Integer>>>> getValue() {
    return null;
  }
}
