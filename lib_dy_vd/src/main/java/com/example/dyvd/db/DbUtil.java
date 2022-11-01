package com.example.dyvd.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.dyvd.VideoItem;
import com.example.dyvd.VideoParser;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
final class DbUtil {

  private DbUtil() {
    //no instance
  }

  @SuppressWarnings("unchecked")
  public static <T> List<T> queryAll(SQLiteDatabase db, Class<T> clazz) {
    List<T> list = new ArrayList<>();
    try (Cursor cursor = db.rawQuery("select * from " + getTable(clazz), null)) {
      while (cursor.moveToNext()) {
        if (clazz == VideoItem.class) {
          list.add((T) VideoParser.fromCursor(cursor));
        }
      }
    }
    return list;
  }

  public static <T> void insertOrReplace(SQLiteDatabase db, T item) {
    db.replace(getTable(item.getClass()), null, toValues(item));
  }

  private static String getTable(Class<?> clazz) {
    final Db.Table table = clazz.getAnnotation(Db.Table.class);
    if (table == null) {
      throw new IllegalArgumentException("bean must be annotated by Db.Table!");
    }
    return table.name();
  }

  private static ContentValues toValues(Object item) {
    final ContentValues values = new ContentValues();
    for (Field field : item.getClass().getDeclaredFields()) {
      final Db.Column column = field.getAnnotation(Db.Column.class);
      if (column == null) continue;
      final Class<?> type = column.type();
      try {
        if (type == String.class) {
          values.put(column.name(), ((String) field.get(item)));
        } else if (type == int.class) {
          values.put(column.name(), field.getInt(item));
        }
        else if (type == long.class) {
          values.put(column.name(), field.getLong(item));
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    return values;
  }
}
