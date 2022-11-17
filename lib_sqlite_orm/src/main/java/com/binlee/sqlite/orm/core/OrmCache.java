package com.binlee.sqlite.orm.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2022/11/14
 *
 * @author binlee
 */
public abstract class OrmCache {

  static final OrmCache NO_CACHE = new OrmCache() {
    @Override public <T> void put(T item) {
    }

    @Override public <T> void remove(T item) {
    }

    @Override public <T> void putAll(List<T> items) {
    }

    @Override public <T> boolean has(Class<T> clazz, Object key) {
      return false;
    }
  };

  private final Map<Class<?>, Map<Object, Object>> mCache;

  public OrmCache() {
    mCache = new HashMap<>();
  }

  public <T> void put(T item) {
    Map<Object, Object> map = mCache.get(item.getClass());
    if (map == null) {
      mCache.put(item.getClass(), map = new HashMap<>());
    }
    map.put(getPrimaryKey(item), item);
  }

  public <T> void remove(T item) {
    Map<Object, Object> map = mCache.get(item.getClass());
    if (map == null) {
      mCache.put(item.getClass(), map = new HashMap<>());
    }
    map.remove(getPrimaryKey(item));
  }

  public <T> void putAll(List<T> items) {
    if (items == null || items.size() == 0) return;

    final Class<?> clazz = items.get(0).getClass();

    Map<Object, Object> map = mCache.get(clazz);
    if (map == null) {
      mCache.put(clazz, map = new HashMap<>());
    }
    map.clear();
    for (T item : items) {
      map.put(getPrimaryKey(item), item);
    }
  }

  public <T> boolean has(Class<T> clazz, Object key) {
    final Map<Object, Object> map = mCache.get(clazz);
    return map != null && map.containsKey(key);
  }

  private static <T> Object getPrimaryKey(T bean) {
    final Table table = DbUtil.getTableOrThrow(bean.getClass());
    try {
      return table.mPrimary.getField(bean);
    } catch (IllegalAccessException e) {
      return null;
    }
  }
}
