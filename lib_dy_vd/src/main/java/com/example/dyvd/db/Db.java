package com.example.dyvd.db;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
public @interface Db {

  @interface Table {
    String name();
  }

  @interface Column {

    String name();

    Class<?> type();
  }
}
