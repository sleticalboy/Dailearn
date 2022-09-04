package com.binlee.dl.puppet;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created on 2022-09-04.
 *
 * @author binlee
 */
public interface IContentProvider {

  void attachInfo(Context context, ProviderInfo info);

  boolean onCreate();

  Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder);

  String getType(Uri uri);

  Uri insert(Uri uri, ContentValues values);

  int delete(Uri uri, String selection, String[] selectionArgs);

  int update(Uri uri, ContentValues values, String selection, String[] selectionArgs);
}
