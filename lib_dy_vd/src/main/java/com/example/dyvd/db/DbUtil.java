package com.example.dyvd.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.dyvd.VideoItem;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
final class DbUtil {

  private static final String TAG = "DbUtil";

  private DbUtil() {
    //no instance
  }

  public static void createTables(SQLiteDatabase db, Class<?>[] classes) {
    for (Class<?> clazz : classes) {
      final String table = getTable(clazz);
      StringBuilder sql = new StringBuilder("create table if not exists " + table + "(");
      for (Field field : clazz.getDeclaredFields()) {
        final Db.Column column = field.getAnnotation(Db.Column.class);
        if (column == null) continue;
        sql.append(column.name());
        final Class<?> type = column.type();
        if (type == int.class) {
          sql.append(" integer");
        } else if (type == long.class) {
          sql.append(" long");
        } else if (type == String.class) {
          sql.append(" varchar");
        }
        // 主键
        if (column.unique()) {
          sql.append(" primary key");
        }

        sql.append(", ");
      }
      sql.delete(sql.length() - 2, sql.length()).append(")");
      Log.d(TAG, "createTables() " + sql);
      db.execSQL(sql.toString());
    }
  }

  public static void remove(SQLiteDatabase db, VideoItem item) {
    db.delete(getTable(item.getClass()), "_share_key = ?", new String[] { item.shareUrl });
  }

  public static <T> void insertOrReplace(SQLiteDatabase db, T item) {
    final ContentValues values;
    try {
      values = toValues(item);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    db.replace(getTable(item.getClass()), null, values);
  }

  public static <T> List<T> queryAll(SQLiteDatabase db, Class<T> clazz) {
    final String sql = "select * from " + getTable(clazz);
    final List<T> list = new ArrayList<>();
    try (Cursor cursor = db.rawQuery(sql, null)) {
      while (cursor.moveToNext()) {
        try {
          list.add(toBean(cursor, clazz));
        } catch (IllegalAccessException | InstantiationException e) {
          e.printStackTrace();
        }
      }
    }
    return list;
  }

  private static String getTable(Class<?> clazz) {
    final Db.Table table = clazz.getAnnotation(Db.Table.class);
    if (table == null) {
      throw new IllegalArgumentException(clazz + " must be annotated by Db.Table!");
    }
    return table.name();
  }

  private static <T> ContentValues toValues(T bean) throws IllegalAccessException {
    final ContentValues values = new ContentValues();
    for (Field field : bean.getClass().getDeclaredFields()) {
      final Db.Column column = field.getAnnotation(Db.Column.class);
      if (column == null) continue;

      final Class<?> type = column.type();
      final Object value = ConverterWrapper.wrap(column.converter()).encode(field.get(bean));

      if (type == String.class) {
        values.put(column.name(), (String) value);
      } else if (type == int.class) {
        values.put(column.name(), (Integer) value);
      } else if (type == long.class) {
        values.put(column.name(), (Long) value);
      }
    }
    return values;
  }

  private static <T> T toBean(Cursor cursor, Class<T> clazz) throws IllegalAccessException, InstantiationException {
    final T bean = clazz.newInstance();

    for (Field field : clazz.getDeclaredFields()) {
      final Db.Column column = field.getAnnotation(Db.Column.class);
      if (column == null) continue;

      final int index = cursor.getColumnIndex(column.name());
      final Class<?> type = column.type();

      Object obj;
      if (type == String.class) {
        obj = cursor.getString(index);
      } else if (type == int.class) {
        obj = cursor.getInt(index);
      } else if (type == long.class) {
        obj = cursor.getLong(index);
      } else {
        continue;
      }
      field.set(bean, ConverterWrapper.wrap(column.converter()).decode(obj));
    }
    return bean;
  }

  private static class ConverterWrapper implements Converter<Object, Object> {

    private static final Map<Class<?>, Converter<?, ?>> sConverters = new HashMap<>();

    private final Converter<Object, Object> mConverter;

    static {
      sConverters.put(Converter.class, DO_NOT_CONVERT);
    }

    @SuppressWarnings("unchecked")
    public static Converter<Object, Object> wrap(Class<?> clazz) {
      Converter<Object, Object> converter = (Converter<Object, Object>) sConverters.get(clazz);
      if (converter == null) {
        sConverters.put(clazz, converter = new ConverterWrapper(clazz));
      }
      return converter;
    }

    @SuppressWarnings("unchecked")
    private ConverterWrapper(Class<?> clazz) {
      try {
        mConverter = (Converter<Object, Object>) clazz.newInstance();
      } catch (IllegalAccessException | InstantiationException e) {
        throw new RuntimeException(e);
      }
    }

    @Override public Object encode(Object input) {
      return mConverter.encode(input);
    }

    @Override public Object decode(Object input) {
      return mConverter.decode(input);
    }
  }

  private static class TableInfo {}
}
