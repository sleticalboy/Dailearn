package com.binlee.sqlite.orm.core;

import androidx.annotation.NonNull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 2022/11/14
 *
 * @author binlee
 */
class Table {
  // 表名、表列、主键
  public final String mName;
  public List<Column> mColumns = new ArrayList<>();
  public Column mPrimary;

  Table(String name) {
    mName = name;
  }

  @NonNull @Override public String toString() {
    return "Table{" + mName + ": " + columns() + ", primary key: " + mPrimary.mName + '}';
  }

  private String columns() {
    final List<String> columns = new ArrayList<>(mColumns.size());
    for (Column column : mColumns) {
      columns.add(column.mName);
    }
    return columns.toString();
  }

  static class Column {
    // 列名、列类型、主键、转换器、对应的转换器

    public final Field mField;
    public final String mName;
    public final Class<?> mType;
    public final Converter<Object, Object> mConverter;

    public final boolean mUnique;

    Column(Field field, Db.Column column) {
      mField = field;
      mName = column.value().equals("") ? mField.getName() : column.value();
      mType = column.type() == Void.class ? mField.getType() : column.type();
      mUnique = column.unique();
      mConverter = WrappedConverters.get(column.converter());
    }

    <T> Object getField(T bean) throws IllegalAccessException {
      return mConverter.encode(mField.get(bean));
    }

    <T> void setField(T bean, Object obj) throws IllegalAccessException {
      mField.set(bean, mConverter.decode(obj));
    }

    @NonNull @Override public String toString() {
      return "Column{"
        + mName
        + ": "
        + mType.getSimpleName().toLowerCase()
        + ", converter: "
        + mConverter.getClass().getSimpleName()
        + ", unique: "
        + mUnique
        + '}';
    }
  }

  private static class WrappedConverters extends Converter<Object, Object> {

    private static final Map<Class<?>, Converter<?, ?>> sConverters = new HashMap<>();

    private final Converter<Object, Object> mConverter;

    static {
      sConverters.put(Converter.class, DO_NOT_CONVERT);
    }

    @SuppressWarnings("unchecked")
    static Converter<Object, Object> get(Class<?> clazz) {
      Converter<Object, Object> converter = (Converter<Object, Object>) sConverters.get(clazz);
      if (converter == null) {
        sConverters.put(clazz, converter = new WrappedConverters(clazz));
      }
      return converter;
    }

    @SuppressWarnings("unchecked")
    private WrappedConverters(Class<?> clazz) {
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
