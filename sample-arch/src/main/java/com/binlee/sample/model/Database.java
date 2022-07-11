package com.binlee.sample.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.ArrayMap;
import android.util.Pair;
import androidx.annotation.Nullable;
import com.binlee.sample.util.Glog;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created on 21-2-26.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class Database extends SQLiteOpenHelper implements IDataSource {

  private static final String TAG = Glog.wrapTag("Database");

  private static final int DB_VERSION = 1;
  private static final String DB_NAME = "arch-settings.db";
  private static final Class<?>[] TABLED_CLASSES = { CacheEntry.class };
  private static final Map<Class<?>, TableInfo> TABLES = new ArrayMap<>();

  static {
    parseTables();
  }

  public Database(@Nullable Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public <T> T query(Class<T> clazz, String selection, String[] args) {
    TableInfo info = TABLES.get(clazz);
    if (info == null) throw new IllegalStateException("tables are null");
    try (Cursor cursor = getReadableDatabase().query(info.name, null, selection, args,
      null, null, null)) {
      return cursorToObj(cursor, clazz);
    }
  }

  @Override
  public <T> List<T> queryAll(Class<T> clazz) {
    return queryAll(getReadableDatabase(), clazz);
  }

  @Override
  public <T> void update(T obj) {
    if (obj == null) return;
    updateBatch(Collections.singletonList(obj));
  }

  @Override
  public <T> void updateBatch(List<T> list) {
    if (list == null || list.size() == 0) return;
    Class<?> clazz = list.get(0).getClass();
    TableInfo info = TABLES.get(clazz);
    if (info == null) {
      Glog.w(TAG, "updateBatch() " + clazz + " is not a @tabled class");
      return;
    }
    SQLiteDatabase db = getWritableDatabase();
    db.beginTransaction();
    try {
      long id;
      for (final T obj : list) {
        id = db.insert(info.name, null, objToValues(obj, info.columns));
        Glog.v(TAG, "updateBatch() id: " + id);
      }
    } finally {
      db.endTransaction();
    }
  }

  @Override
  public <T> void delete(T obj) {
    if (obj == null) return;
    deleteBatch(Collections.singletonList(obj));
  }

  @Override
  public <T> void deleteBatch(List<T> list) {
    if (list == null || list.size() == 0) return;
    Class<?> clazz = list.get(0).getClass();
    TableInfo info = TABLES.get(clazz);
    if (info == null) {
      Glog.w(TAG, "deleteBatch() " + clazz + " is not a @tabled class");
      return;
    }
    Pair<String, String[]> where;
    try {
      where = joinWhere(info, list);
    } catch (IllegalAccessException e) {
      throw new DbException("joinWhere() error", e);
    }
    if (where == null || where.second.length != list.size()) return;

    SQLiteDatabase db = getWritableDatabase();
    db.beginTransaction();
    try {
      long id = db.delete(info.name, where.first, where.second);
      Glog.v(TAG, "deleteBatch() id: " + id);
    } finally {
      db.endTransaction();
    }
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    createTables(db, "onCreate()");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Map<Class<?>, List<?>> data = dropTables(db, "onUpgrade()");
    createTables(db, "onUpgrade()");
    restore(data);
  }

  @Override
  public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Map<Class<?>, List<?>> data = dropTables(db, "onDowngrade()");
    createTables(db, "onDowngrade()");
    restore(data);
  }

  private void restore(Map<Class<?>, List<?>> data) {
    if (data == null || data.size() == 0) return;
    for (final Class<?> clazz : data.keySet()) {
      updateBatch(data.get(clazz));
    }
  }

  private static <T> List<T> queryAll(SQLiteDatabase db, Class<T> clazz) {
    TableInfo info = TABLES.get(clazz);
    if (info == null) throw new IllegalStateException("tables are null");
    try (Cursor cursor = db.rawQuery("SELECT * FROM " + info.name, null)) {
      List<T> list = new ArrayList<>();
      while (cursor != null && cursor.moveToNext()) {
        T obj = cursorToObj(cursor, clazz);
        if (obj == null) continue;
        list.add(obj);
      }
      Glog.v(TAG, "queryAll() list: " + list);
      return list;
    }
  }

  private static <T> Pair<String, String[]> joinWhere(TableInfo info, List<T> list)
    throws IllegalAccessException {
    Pair<Field, Table.Column> column = null;
    for (final Pair<Field, Table.Column> pair : info.columns) {
      if (pair.second.unique()) {
        column = pair;
        break;
      }
    }
    if (column == null) return null;
    String[] args = new String[list.size()];
    // column in (?,?,?,?)
    StringBuilder cause = new StringBuilder(column.second.name()).append(" in (");
    for (int i = 0, size = list.size() - 1; i <= size; i++) {
      cause.append(i == size ? "?)" : "?,");
      args[i] = String.valueOf(column.first.get(list.get(i)));
    }
    // cause.append(")");
    return Pair.create(cause.toString(), args);
  }

  private static void createTables(SQLiteDatabase db, String reason) {
    StringBuilder sql = new StringBuilder();
    StringBuilder unique = new StringBuilder();
    for (final TableInfo info : TABLES.values()) {
      // table start
      sql.append("CREATE TABLE IF NOT EXISTS ").append(info.name).append(" (");
      sql.append("_id INTEGER PRIMARY KEY AUTOINCREMENT");
      // table columns
      for (final Pair<Field, Table.Column> p : info.columns) {
        sql.append(",").append(p.second.name()).append(' ').append(p.second.type());
        if (p.second.defVal().length() != 0) {
          sql.append(" DEFAULT ").append(p.second.defVal());
        }
        // record unique
        if (p.second.unique()) {
          if (unique.length() != 0) unique.append(",");
          unique.append(p.second.name());
        }
      }
      // unique
      if (unique.length() > 0) {
        sql.append(",UNIQUE(").append(unique).append(")");
        // 重置 StringBuilder
        unique.setLength(0);
      }
      // table end
      sql.append(");");

      // create tables
      logAndExec(db, sql.toString(), reason);
      // 重置 StringBuilder
      sql.setLength(0);

      // create indexes
      for (final Pair<Field, Table.Column> pair : info.columns) {
        if (!pair.second.indexed()) continue;
        sql.append("create index ");
        // index name: {table}_index_{column}
        sql.append(info.name).append("_index_").append(pair.second.name());
        sql.append(" ON ").append(info.name);
        sql.append("(").append(pair.second.name()).append(");");
        logAndExec(db, sql.toString(), reason);
        // 重置 StringBuilder
        sql.setLength(0);
      }
    }
  }

  private static Map<Class<?>, List<?>> dropTables(SQLiteDatabase db, String reason) {
    // 保留原始数据
    Map<Class<?>, List<?>> backup = new ArrayMap<>();
    for (final Class<?> clazz : TABLES.keySet()) {
      List<?> list = (List<?>) queryAll(db, clazz);
      if (list.size() != 0) backup.put((Class<?>) clazz, list);
    }
    for (final TableInfo info : TABLES.values()) {
      logAndExec(db, "DROP TABLE IF EXISTS " + info.name + ";", reason);
    }
    return backup;
  }

  private static void logAndExec(SQLiteDatabase db, String sql, String reason) {
    Glog.v(TAG, "logAndExec() " + reason + ", " + sql);
    try {
      if (db != null && sql != null) db.execSQL(sql);
    } catch (SQLException e) {
      Glog.e(TAG, "logAndExec() " + reason + ", " + sql);
    }
  }

  private static <T> T cursorToObj(Cursor c, Class<T> clazz) throws DbException {
    TableInfo info = TABLES.get(clazz);
    if (info == null) return null;
    T obj = null;
    try {
      obj = clazz.getConstructor().newInstance();
    } catch (IllegalAccessException | InstantiationException | InvocationTargetException
      | NoSuchMethodException e) {
      throw new DbException("cursorToObj() error", e);
    }
    for (final Pair<Field, Table.Column> p : info.columns) {
      int index = c.getColumnIndex(p.second.name());
      int type = c.getType(index);
      Object value = null;
      if (type == Cursor.FIELD_TYPE_INTEGER) {
        value = c.getInt(index);
      } else if (type == Cursor.FIELD_TYPE_BLOB) {
        value = c.getBlob(index);
      } else if (type == Cursor.FIELD_TYPE_FLOAT) {
        value = c.getFloat(index);
      } else if (type == Cursor.FIELD_TYPE_STRING) {
        value = c.getString(index);
      }
      Glog.v(TAG, "cursorToObj() " + p.second.name() + " = " + value);
      if (value != null) {
        try {
          p.first.set(obj, value);
        } catch (IllegalAccessException e) {
          throw new DbException("cursorToObj() error", e);
        }
      }
    }
    return obj;
  }

  private static ContentValues objToValues(Object obj, List<Pair<Field, Table.Column>> columns)
    throws DbException {
    ContentValues cv = new ContentValues();
    for (final Pair<Field, Table.Column> p : columns) {
      String key = p.second.name();
      Object name = null;
      try {
        name = p.first.get(obj);
      } catch (IllegalAccessException e) {
        throw new DbException("objToValues() error", e);
      }
      Glog.v(TAG, "objToValues() " + key + " = " + name);
      if (name instanceof Integer) {
        cv.put(key, ((Integer) name));
      } else if (name instanceof String) {
        cv.put(key, ((String) name));
      } else if (name instanceof Byte) {
        cv.put(key, ((Byte) name));
      } else if (name instanceof Short) {
        cv.put(key, ((Short) name));
      } else if (name instanceof Float) {
        cv.put(key, ((Float) name));
      } else if (name instanceof Long) {
        cv.put(key, ((Long) name));
      } else if (name instanceof Double) {
        cv.put(key, ((Double) name));
      } else if (name instanceof Boolean) {
        cv.put(key, ((Boolean) name));
      } else if (name != null && name.getClass() == byte[].class) {
        cv.put(key, ((byte[]) name));
      } else {
        Glog.w(TAG, "objToValues() unexpected name: " + name);
      }
    }
    return cv;
  }

  private static void parseTables() {
    TABLES.clear();
    List<Pair<Field, Table.Column>> columns;
    for (final Class<?> clazz : TABLED_CLASSES) {
      Table table = clazz.getAnnotation(Table.class);
      if (table == null) continue;
      columns = new ArrayList<>();
      for (final Field field : clazz.getDeclaredFields()) {
        Table.Column column = field.getAnnotation(Table.Column.class);
        if (column == null) continue;
        field.setAccessible(true);
        columns.add(Pair.create(field, column));
      }
      TABLES.put(clazz, new TableInfo(table.name(), columns));
    }
    Glog.v(TAG, "parseTables() " + TABLES);
  }

  private static final class TableInfo {

    final String name;
    final List<Pair<Field, Table.Column>> columns;

    public TableInfo(String name, List<Pair<Field, Table.Column>> columns) {
      this.name = name;
      this.columns = columns;
    }

    @Override
    public String toString() {
      StringBuilder buffer = new StringBuilder("TableInfo{");
      buffer.append("name='").append(name).append('\'');
      buffer.append(", columns=");
      if (columns == null || columns.size() == 0) {
        buffer.append("no columns");
      } else {
        buffer.append('[');
        for (final Pair<Field, Table.Column> p : columns) {
          buffer.append(p.first.getName()).append('=').append(p.second).append(',');
        }
        buffer.append(']');
      }
      buffer.append("}");
      return buffer.toString();
    }
  }

  public static final class DbException extends RuntimeException {

    public DbException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
