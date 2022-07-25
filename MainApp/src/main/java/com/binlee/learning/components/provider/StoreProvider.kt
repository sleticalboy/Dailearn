package com.binlee.learning.components.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log

class StoreProvider : ContentProvider() {

  companion object {
    private const val TAG = "StoreProvider"
    private const val AUTHORITY = "com.binlee.dailywork.store"
    private val URI_MATCHER = UriMatcher(UriMatcher.NO_MATCH)

    init {
      URI_MATCHER.addURI(AUTHORITY, "/cache", 0)
      URI_MATCHER.addURI(AUTHORITY, "/pair", 1)
    }
  }

  override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
    throw UnsupportedOperationException("Not yet implemented")
  }

  override fun getType(uri: Uri): String? {
    return null
  }

  override fun insert(uri: Uri, values: ContentValues?): Uri? {
    throw UnsupportedOperationException("Not yet implemented")
  }

  override fun onCreate(): Boolean {
    // 此方法会比 Application 的 onCreate() 方法先执行
    Log.d(TAG, "onCreate() start --->")
    StoreManager.init(context)
    return true
  }

  override fun query(
    uri: Uri, projection: Array<String>?, selection: String?,
    selectionArgs: Array<String>?, sortOrder: String?
  ): Cursor {
    val cursor = MatrixCursor(projection)
    var value = 0
    val table = uri.lastPathSegment
    var sql = "select mac from device_list where mac=? and status"
    sql += when (table) {
      "cache" -> {
        "=0"
      }
      "pair" -> {
        ">=3"
      }
      else -> {
        cursor.addRow(arrayOf<Any>(value))
        return cursor
      }
    }
    val c: Cursor = StoreManager.get().readableDb().rawQuery(sql, projection)
    value = if (c.count != 0) 1 else 0
    c.close()
    cursor.addRow(arrayOf<Any>(value))
    return cursor
  }

  override fun update(
    uri: Uri, values: ContentValues?, selection: String?,
    selectionArgs: Array<String>?
  ): Int {
    throw UnsupportedOperationException("Not yet implemented")
  }
}