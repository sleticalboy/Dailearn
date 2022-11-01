package com.example.dyvd.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

/**
 * Created on 2022/11/1
 *
 * @author binlee
 */
final class VideosDbHelper extends SQLiteOpenHelper {

  private static final String DB_NAME = "dy_videos";
  private static final int DB_VERSION = 1;

  public VideosDbHelper(@Nullable Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override public void onCreate(SQLiteDatabase db) {
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
