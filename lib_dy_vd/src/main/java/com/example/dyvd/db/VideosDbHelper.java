package com.example.dyvd.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import com.example.dyvd.VideoItem;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
final class VideosDbHelper extends SQLiteOpenHelper {

  private static final String DB_NAME = "dy_videos.db";
  private static final int DB_VERSION = 1;

  private static final Class<?>[] TABLES = {
    VideoItem.class
  };

  static {
    DbUtil.preload(TABLES);
  }

  public VideosDbHelper(@Nullable Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override public void onCreate(SQLiteDatabase db) {
    DbUtil.createTables(db, TABLES);
  }

  @Override public void onConfigure(SQLiteDatabase db) {
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  }

  @Override public void onOpen(SQLiteDatabase db) {
  }

  @Override public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    super.onDowngrade(db, oldVersion, newVersion);
  }
}
