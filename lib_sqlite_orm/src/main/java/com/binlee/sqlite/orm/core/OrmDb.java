package com.binlee.sqlite.orm.core;

import android.content.Context;
import com.binlee.sqlite.orm.OrmConfig;
import java.util.List;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
public final class OrmDb {

  private final OrmDbHelper mHelper;
  private final OrmCache mCache;

  public OrmDb(Context context) {
    this(context, new OrmConfig());
  }

  public OrmDb(Context context, OrmConfig config) {
    mHelper = new OrmDbHelper(context);
    mCache = config.useCache ? new OrmCache() {
      @Override public int hashCode() {
        return super.hashCode();
      }
    } : OrmCache.NO_CACHE;
  }

  public <T> void insert(T item) {
    DbUtil.insertOrReplace(mHelper.getWritableDatabase(), item);
    mCache.put(item);
  }

  public <T> void remove(T item) {
    DbUtil.remove(mHelper.getWritableDatabase(), item);
    mCache.remove(item);
  }

  public <T> List<T> getAll(Class<T> clazz) {
    final List<T> items = DbUtil.queryAll(mHelper.getReadableDatabase(), clazz);

    mCache.putAll(items);

    return items;
  }

  public <T> boolean contains(Class<T> clazz, Object key) {
    return mCache.has(clazz, key);
  }

  public void close() {
    mHelper.close();
  }
}
