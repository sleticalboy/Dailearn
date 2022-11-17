package com.binlee.sqlite.orm;

import com.binlee.sqlite.orm.core.DbUtil;

/**
 * Created on 2022/11/17
 *
 * @author binlee
 */
public final class OrmCore {

  private static OrmConfig sConfig;

  public static void init(OrmConfig config) {
    sConfig = config;
    if (config.preloadTables) {
      DbUtil.preloadTables(config.tables);
    }
  }

  public static OrmConfig getConfig() {
    return sConfig;
  }
}
