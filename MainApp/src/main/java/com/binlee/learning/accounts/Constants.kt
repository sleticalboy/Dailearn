package com.binlee.learning.accounts

import android.net.Uri

interface Constants {

  companion object {
    const val ACCOUNT_PREFIX = "user"
    const val ACCOUNT_PASSWORD = "test"
    const val ACCOUNT_NAME = "sleticalboy@gmail.com"
    const val ACCOUNT_TYPE = "com.binlee.dailywork"
    const val ACCOUNT_AUTH_TOKEN = "com.binlee.dailywork.auth.token"
    val DB_URI = Uri.parse("content://com.binlee.dailywork.contacts")
  }
}