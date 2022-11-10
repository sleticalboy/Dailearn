package com.binlee.sqlite.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
final class VideosDbHelper extends SQLiteOpenHelper {

  private static final String DB_NAME = "dy_videos.db";
  private static final int DB_VERSION = 1;

  private static final Set<Class<?>> TABLES = new HashSet<>();

  public static void registerTables(Class<?>... tables) {
    if (tables == null || tables.length == 0) return;

    final List<Class<?>> preloads = new ArrayList<>();
    for (Class<?> table : tables) {
      if (TABLES.add(table)) {
        preloads.add(table);
      }
    }
    DbUtil.preload(preloads.toArray(new Class[0]));
  }

  public VideosDbHelper(@Nullable Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override public void onCreate(SQLiteDatabase db) {
    DbUtil.createTables(db, TABLES.toArray(new Class[0]));
  }

  @Override public void onOpen(SQLiteDatabase db) {
  }

  @Override public void onConfigure(SQLiteDatabase db) {
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    DbUtil.upgradeTables(db, TABLES.toArray(new Class[0]));
  }

  @Override public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    super.onDowngrade(db, oldVersion, newVersion);
  }
}
