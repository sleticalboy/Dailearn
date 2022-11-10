package com.binlee.sqlite.orm;

import android.content.Context;
import android.os.Build;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
public final class VideosDb {

  private final VideosDbHelper mHelper;
  private final Map<Class<?>, Map<Object, Object>> mCache;

  public static void registerTables(Class<?>... tables) {
    VideosDbHelper.registerTables(tables);
  }

  public VideosDb(Context context) {
    mHelper = new VideosDbHelper(context);
    mCache = new HashMap<>();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      context.deleteSharedPreferences("all_dy_videos");
    }
  }

  public <T> void insert(T item) {
    DbUtil.insertOrReplace(mHelper.getWritableDatabase(), item);
    Map<Object, Object> map = mCache.get(item.getClass());
    if (map == null) {
      mCache.put(item.getClass(), map = new HashMap<>());
    }
    map.put(DbUtil.getPrimaryKey(item), item);
  }

  public <T> void remove(T item) {
    DbUtil.remove(mHelper.getWritableDatabase(), item);

    Map<Object, Object> map = mCache.get(item.getClass());
    if (map == null) {
      mCache.put(item.getClass(), map = new HashMap<>());
    }
    map.remove(DbUtil.getPrimaryKey(item));
  }

  public <T> List<T> getAll(Class<T> clazz) {
    final List<T> items = DbUtil.queryAll(mHelper.getReadableDatabase(), clazz);
    Map<Object, Object> map = mCache.get(clazz);
    if (map == null) {
      mCache.put(clazz, map = new HashMap<>());
    }
    map.clear();
    for (T item : items) {
      map.put(DbUtil.getPrimaryKey(item), item);
    }
    return items;
  }

  public <T> boolean contains(Class<T> clazz, Object key) {
    final Map<Object, Object> map = mCache.get(clazz);
    return map != null && map.containsKey(key);
  }

  public void close() {
    mHelper.close();
  }
}
