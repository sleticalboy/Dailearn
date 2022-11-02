package com.example.dyvd.db;

/**
 * Created on 2022/11/2
 *
 * @author binlee
 */
public interface Converter<Java, Db> {

  Converter<Object, Object> DO_NOT_CONVERT = new Converter<Object, Object>() {
    @Override public Object encode(Object input) {
      return input;
    }

    @Override public Object decode(Object input) {
      return input;
    }
  };

  /**
   * 编码
   *
   * @param input 输入
   * @return {@link Db} 数据库层数据结构
   */
  Db encode(Java input);

  /**
   * 解码
   *
   * @param input 输入
   * @return {@link Java} 层的数据结构
   */
  Java decode(Db input);
}
