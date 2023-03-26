package com.binlee.learning.components.provider

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class StoreManager private constructor(context: Context? = null) {

  private val mHelper: DbHelper? = context?.let { DbHelper(it) }

  private object SingletonHolder {
    val MANAGER = StoreManager()
  }

  private fun helper(): DbHelper? {
    return mHelper
  }

  fun readableDb(): SQLiteDatabase {
    return get().helper()!!.readableDatabase
  }

  fun writableDb(): SQLiteDatabase {
    return get().helper()!!.writableDatabase
  }

  private class DbHelper  // public static final int STATUS_CONNECTED = 0;
  // public static final int STATUS_NOT_CONNECTED = 1;
  // public static final int STATUS_DISCONNECTING = 2;
  // public static final int STATUS_CONNECTING = 3;
  // public static final int STATUS_WRITE_CONFIG = 4;
  // public static final int STATUS_SWITCH_MODE = 5;
  // public static final int STATUS_SWITCH_DONE = 6;
  // 用户主动断开时：
  // 1、需要同步断开 profile；2、重连时需要同步连上 profile
  // public static final int DISCONNECT_BY_USER = 1;
  // 同 DISCONNECT_BY_USER
  // public static final int DISCONNECT_BY_AUDIO_ROUTE = 2;
  // 同 DISCONNECT_BY_USER
  // public static final int DISCONNECT_BY_DEVICE_SLEEP = 3;
  // 非用户主动断开：
  // 实现反连
  // public static final int DISCONNECT_BY_LOST_HOST = 4;
    (context: Context?) : SQLiteOpenHelper(context, "connected_devices.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
      db.execSQL(
        "CREATE TABLE device_list(" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name VARCHAR(15), " +
            "mac VARCHAR(15), " +
            "pipe INTEGER, " +
            "channels VARCHAR(15), " +
            "disconnect_type INTEGER DEFAULT 1, " +
            "status INTEGER DEFAULT 1" +
            ")"
      )
      migrantFromCache(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
      //
    }

    private fun migrantFromCache(db: SQLiteDatabase) {}
  }

  companion object {
    @JvmStatic
    fun init(context: Context?) {
      StoreManager(context)
    }

    @JvmStatic
    fun get(): StoreManager {
      return SingletonHolder.MANAGER
    }
  }

}