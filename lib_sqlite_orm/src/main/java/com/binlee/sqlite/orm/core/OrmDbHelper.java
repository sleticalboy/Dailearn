package com.binlee.sqlite.orm.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.binlee.sqlite.orm.OrmCore;
import com.binlee.sqlite.orm.core.DbUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
final class OrmDbHelper extends SQLiteOpenHelper {

  private static final String DB_NAME = "dy_videos.db";
  private static final int DB_VERSION = 1;

  private final Set<Class<?>> mTables;

  public OrmDbHelper(@Nullable Context context) {
    super(context, DB_NAME, null, DB_VERSION);
    mTables = new HashSet<>();
    Collections.addAll(mTables, OrmCore.getConfig().tables);
  }

  @Override public void onCreate(SQLiteDatabase db) {
    DbUtil.createTables(db, mTables.toArray(new Class[0]));
  }

  @Override public void onOpen(SQLiteDatabase db) {
  }

  @Override public void onConfigure(SQLiteDatabase db) {
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    DbUtil.upgradeTables(db, mTables.toArray(new Class[0]));
  }

  @Override public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    super.onDowngrade(db, oldVersion, newVersion);
  }
}
