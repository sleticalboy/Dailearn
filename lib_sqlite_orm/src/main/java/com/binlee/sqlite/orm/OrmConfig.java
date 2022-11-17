package com.binlee.sqlite.orm;

/**
 * Created on 2022/11/14
 *
 * @author binlee
 */
public class OrmConfig {

  /** 是否使用缓存 */
  public boolean useCache = true;
  /** 是否预加载表 */
  public boolean preloadTables = false;

  public Class<?>[] tables;

  public void tables(Class<?>... tables) {
    this.tables = tables;
  }
}
