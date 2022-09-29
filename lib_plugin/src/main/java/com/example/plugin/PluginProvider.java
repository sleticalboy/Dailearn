package com.example.plugin;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created on 2022/9/29
 *
 * @author binlee
 */
public final class PluginProvider extends ContentProvider {

  private static final String TAG = "PluginProvider";

  @Override public boolean onCreate() {
    return true;
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

  @Nullable @Override public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
    if ("sampleMethod".equals(method)) {
      sampleMethod(arg, extras);
    }
    return null;
  }

  private void sampleMethod(String arg, Bundle extras) {
    Log.d(TAG, "sampleMethod() called with: arg = [" + arg + "], extras = [" + extras + "]");
  }
}
