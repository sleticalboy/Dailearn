package com.sleticalboy.learning.accounts

import android.net.Uri

interface Constants {

  companion object {
    const val ACCOUNT_PREFIX = "user"
    const val ACCOUNT_PASSWORD = "test"
    const val ACCOUNT_NAME = "sleticalboy@gmail.com"
    const val ACCOUNT_TYPE = "com.sleticalboy.dailywork"
    const val ACCOUNT_AUTH_TOKEN = "com.sleticalboy.dailywork.auth.token"
    val DB_URI = Uri.parse("content://com.sleticalboy.dailywork.contacts")
  }
}