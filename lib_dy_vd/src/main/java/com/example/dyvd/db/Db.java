package com.example.dyvd.db;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Db {

  /** 数据表 */
  @Retention(RetentionPolicy.RUNTIME)
  @interface Table {
    /**
     * 名字
     *
     * @return {@link String}
     */
    String name();
  }

  /** 数据表中的列 */
  @Retention(RetentionPolicy.RUNTIME)
  @interface Column {

    /**
     * 名字，为 "" 时表示使用 bean 中的字段
     *
     * @return {@link String}
     */
    String name() default "";

    /**
     * 类型
     *
     * @return {@link Class}<{@link ?}>
     */
    Class<?> type();

    /**
     * 独特
     *
     * @return boolean
     */
    boolean unique() default false;

    /**
     * 转换器
     *
     * @return {@link Class}<{@link ?} {@link extends} {@link Converter}>
     */
    Class<? extends Converter> converter() default Converter.class;
  }
}
