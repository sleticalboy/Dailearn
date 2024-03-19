package com.binlee.dl.host;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created on 2022-08-28.
 *
 * @author binlee
 */
public class ProxyProvider extends ContentProvider {

  @Override public void attachInfo(Context context, ProviderInfo info) {
    super.attachInfo(context, info);
  }

  @Override public boolean onCreate() {
    return false;
  }

  @Nullable @Override
  public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs,
    @Nullable String sortOrder) {
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

  @Override
  public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
    return 0;
  }
}
