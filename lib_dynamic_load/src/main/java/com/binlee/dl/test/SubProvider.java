package com.binlee.dl.test;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.binlee.dl.puppet.IContentProvider;

/**
 * Created on 2022/9/6
 *
 * @author binlee
 */
public class SubProvider extends ContentProvider implements IContentProvider {

  @Override public boolean onCreate() {
    return false;
  }

  @Nullable @Override public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
    @Nullable String[] selectionArgs, @Nullable String sortOrder) {
    return null;
  }

  @Nullable @Override public String getType(@NonNull Uri uri) {
    return null;
  }

  @Nullable @Override public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
    return null;
  }

  @Override public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
    return 0;
  }

  @Override public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
    @Nullable String[] selectionArgs) {
    return 0;
  }
}
