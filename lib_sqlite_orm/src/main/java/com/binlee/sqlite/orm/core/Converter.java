package com.binlee.sqlite.orm.core;

/**
 * Created on 2022/11/2
 *
 * @author binlee
 */
public abstract class Converter<Java, Db> {

  public static final Converter<Object, Object> DO_NOT_CONVERT = new Converter<Object, Object>() {
    @Override public Object encode(Object input) {
      return input;
    }

    @Override public Object decode(Object input) {
      return input;
    }
  };

  public Converter() {
  }

  /**
   * 编码
   *
   * @param input 输入
   * @return {@link Db} 数据库层数据结构
   */
  public abstract Db encode(Java input);

  /**
   * 解码
   *
   * @param input 输入
   * @return {@link Java} 层的数据结构
   */
  public abstract Java decode(Db input);
}
