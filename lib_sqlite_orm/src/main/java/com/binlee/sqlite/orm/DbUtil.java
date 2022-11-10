package com.binlee.sqlite.orm;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import androidx.annotation.NonNull;
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

  private static final Map<Class<?>, Table> sTables = new HashMap<>();

  private DbUtil() {
    //no instance
  }

  public static void preload(Class<?>[] classes) {
    for (Class<?> clazz : classes) {
      sTables.put(clazz, parseTable(clazz));
    }
  }

  public static void createTables(SQLiteDatabase db, Class<?>[] classes) {
    for (Class<?> clazz : classes) {
      final Table table = sTables.get(clazz);
      if (table == null) continue;

      StringBuilder sql = new StringBuilder("create table if not exists " + table.mName + "(");

      for (Column column : table.mColumns) {
        sql.append(column.mName);
        if (column.mType == int.class) {
          sql.append(" integer");
        } else if (column.mType == long.class) {
          sql.append(" long");
        } else if (column.mType == String.class) {
          sql.append(" varchar");
        }
        // 主键
        if (column.mUnique) {
          sql.append(" primary key");
        }
        sql.append(", ");
      }
      sql.delete(sql.length() - 2, sql.length()).append(")");
      Log.d(TAG, "createTables() " + sql);
      db.execSQL(sql.toString());
    }
  }

  public static void upgradeTables(SQLiteDatabase db, Class<?>[] tables) {
    // 是否需要保留原始数据
    // 如果保留原始数据，将原始表重命名为临时表，创建新表，把临时表数据导入新表，删除原始表
  }

  public static void remove(SQLiteDatabase db, Object bean) {
    final Table table = getTableOrThrow(bean.getClass());
    try {
      final String[] whereArgs = { "" + table.mPrimary.getField(bean) };
      db.delete(table.mName, table.mPrimary.mName + " = ?", whereArgs);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  public static <T> void insertOrReplace(SQLiteDatabase db, T item) {
    final Table table = getTableOrThrow(item.getClass());
    try {
      db.replace(table.mName, null, toValues(item, table));
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> List<T> queryAll(SQLiteDatabase db, Class<T> clazz) {
    final Table table = getTableOrThrow(clazz);
    final List<T> list = new ArrayList<>();
    try (Cursor cursor = db.rawQuery("select * from " + table.mName, null)) {
      while (cursor.moveToNext()) {
        try {
          list.add(toBean(cursor, clazz, table));
        } catch (IllegalAccessException | InstantiationException e) {
          e.printStackTrace();
        }
      }
    }
    return list;
  }

  static <T> Object getPrimaryKey(T bean) {
    final Table table = getTableOrThrow(bean.getClass());
    try {
      return table.mPrimary.getField(bean);
    } catch (IllegalAccessException e) {
      return null;
    }
  }

  private static Table parseTable(Class<?> clazz) {
    final Db.Table tableAnnotation = clazz.getAnnotation(Db.Table.class);
    if (tableAnnotation == null) {
      throw new IllegalArgumentException(clazz + " must be annotated by Db.Table!");
    }
    final Table table = new Table(tableAnnotation.value());

    for (Field field : clazz.getDeclaredFields()) {
      final Db.Column columnAnnotation = field.getAnnotation(Db.Column.class);
      if (columnAnnotation == null) continue;

      final Column column = new Column(field, columnAnnotation);
      if (column.mUnique) {
        table.mPrimary = column;
      }
      table.mColumns.add(column);
    }

    if (table.mPrimary == null) throw new IllegalArgumentException("No primary key in table: " + table.mName);

    Log.d(TAG, "parseTable() " + clazz + ", " + table);
    return table;
  }

  private static <T> ContentValues toValues(T bean, Table table) throws IllegalAccessException {
    final ContentValues values = new ContentValues(table.mColumns.size());
    for (Column column : table.mColumns) {
      final Object value = column.getField(bean);
      if (column.mType == String.class) {
        values.put(column.mName, (String) value);
      } else if (column.mType == int.class) {
        values.put(column.mName, (Integer) value);
      } else if (column.mType == long.class) {
        values.put(column.mName, (Long) value);
      } else if (column.mType == byte[].class) {
        values.put(column.mName, (byte[]) value);
      }
    }
    return values;
  }

  private static <T> T toBean(Cursor cursor, Class<T> clazz, Table table) throws IllegalAccessException, InstantiationException {
    final T bean = clazz.newInstance();
    for (Column column : table.mColumns) {
      Object obj;
      final int index = cursor.getColumnIndex(column.mName);
      if (column.mType == String.class) {
        obj = cursor.getString(index);
      } else if (column.mType == int.class) {
        obj = cursor.getInt(index);
      } else if (column.mType == long.class) {
        obj = cursor.getLong(index);
      } else if (column.mType == byte[].class) {
        obj = cursor.getBlob(index);
      } else {
        continue;
      }
      column.setField(bean, obj);
    }
    return bean;
  }

  private static Table getTableOrThrow(Class<?> clazz) {
    final Table table = sTables.get(clazz);
    if (table == null) {
      throw new IllegalArgumentException("Invalid java bean: " + clazz);
    }
    return table;
  }

  private static class Table {
    // 表名、表列、主键
    public final String mName;
    public List<Column> mColumns = new ArrayList<>();
    public Column mPrimary;

    public Table(String name) {
      mName = name;
    }

    @NonNull @Override public String toString() {
      return "Table{" + mName + ": " + mColumns + ", primary key: " + mPrimary.mName + '}';
    }
  }

  private static class Column {
    // 列名、列类型、主键、转换器、对应的转换器

    public final Field mField;
    public final String mName;
    public final Class<?> mType;
    public final Class<?> mConverter;

    public final boolean mUnique;

    public Column(Field field, Db.Column column) {
      mField = field;
      mName = column.value().equals("") ? mField.getName() : column.value();
      mType = column.type() == Void.class ? mField.getType() : column.type();
      mUnique = column.unique();
      mConverter = column.converter();
    }

    public <T> Object getField(T bean) throws IllegalAccessException {
      return ConverterWrapper.wrap(mConverter).encode(mField.get(bean));
    }

    public <T> void setField(T bean, Object obj) throws IllegalAccessException {
      mField.set(bean, ConverterWrapper.wrap(mConverter).decode(obj));
    }

    @NonNull @Override public String toString() {
      return "Column{" + mName + ": " + mType + ", converter: " + mConverter + ", unique: " + mUnique + '}';
    }
  }

  private static class ConverterWrapper extends Converter<Object, Object> {

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
}
