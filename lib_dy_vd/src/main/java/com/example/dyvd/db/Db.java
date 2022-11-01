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
    String name();
  }

  /** 数据表中的列 */
  @Retention(RetentionPolicy.RUNTIME)
  @interface Column {

    String name();

    Class<?> type();

    boolean unique() default false;
  }
}
