package com.sleticalboy.learning.accounts.sync

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AuthenticatorException
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SyncResult
import android.os.Bundle
import android.os.SystemClock
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import com.sleticalboy.learning.accounts.Constants
import java.io.IOException

/**
 * Created by AndroidStudio on 20-2-23.
 *
 * @author binlee
 */
internal class ContactsSyncAdapter(context: Context?) : AbstractThreadedSyncAdapter(context, true),
  Constants {

  private val mAccountManager: AccountManager = AccountManager.get(context)

  override fun onPerformSync(
    account: Account, extras: Bundle, authority: String,
    provider: ContentProviderClient, syncResult: SyncResult
  ) {
    Log.d(
      TAG, "onPerformSync() called with: account = [" + account + "], extras = [" + extras
          + "], authority = [" + authority + "], provider = [" + provider
          + "], syncResult = [" + syncResult + "], " + Thread.currentThread()
    )
    printObj(extras)
    SystemClock.sleep(800L)
    try {
      val lastSyncMarker = getServerSyncMarker(account)
      if (lastSyncMarker == 0L) {
        setAccountContactsVisibility(context, account, true)
      }
      val authToken = mAccountManager.blockingGetAuthToken(
        account,
        Constants.ACCOUNT_TYPE, NOTIFY_AUTH_FAILURE
      )
      Log.d(TAG, "onPerformSync: authToken = $authToken")
    } catch (e: Throwable) {
      if (e is AuthenticatorException) {
        syncResult.stats.numAuthExceptions++
      } else if (e is IOException) {
        syncResult.stats.numIoExceptions++
      } else if (e is IllegalAccessException) {
        syncResult.databaseError = true
      } else {
        syncResult.stats.numParseExceptions++
      }
      e.printStackTrace()
    }
  }

  private fun getServerSyncMarker(account: Account): Long {
    val markerString = mAccountManager.getUserData(account, SYNC_MARKER_KEY)
    return if (!TextUtils.isEmpty(markerString)) {
      markerString.toLong()
    } else 0
  }

  /**
   * Save off the high-water-mark we receive back from the server.
   *
   * @param account The account we're syncing
   * @param marker  The high-water-mark we want to save.
   */
  private fun setServerSyncMarker(account: Account, marker: Long) {
    mAccountManager.setUserData(account, SYNC_MARKER_KEY, marker.toString())
  }

  companion object {
    private const val TAG = "ContactsSyncAdapter"
    private const val SYNC_MARKER_KEY = "com.sleticalboy.sync.marker"
    private const val NOTIFY_AUTH_FAILURE = true
    fun printObj(obj: Any?) {
      if (obj == null) {
        Log.d(TAG, "printObj: obj is null")
      }
      when (obj) {
        is Map<*, *> -> {
          Log.d(TAG, "printObj: obj is $obj")
        }
        is List<*> -> {
          Log.d(TAG, "printObj: obj is $obj")
        }
        is Bundle -> {
          printBundle(obj as Bundle?, "Bundle")
        }
        is Intent -> {
          printBundle(obj.extras, "Intent bundle")
        }
        is Throwable -> {
          Log.d(TAG, "printObj: obj is Throwable", obj as Throwable?)
        }
        else -> {
          Log.d(TAG, "printObj: obj is $obj")
        }
      }
    }

    private fun printBundle(bundle: Bundle?, prefix: String) {
      if (bundle == null || bundle.isEmpty) {
        Log.d(TAG, "$prefix empty bundle.")
        return
      }
      for (key in bundle.keySet()) {
        when (val value = bundle[key]) {
          is Bundle -> {
            printBundle(value as Bundle?, "Bundle")
          }
          is Intent -> {
            printBundle(value.extras, "Intent bundle")
          }
          else -> {
            Log.d(TAG, "$prefix key = $value")
          }
        }
      }
    }

    fun setAccountContactsVisibility(
      context: Context, account: Account,
      visible: Boolean
    ) {
      val values = ContentValues()
      values.put(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
      values.put(ContactsContract.RawContacts.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE)
      values.put(ContactsContract.Settings.UNGROUPED_VISIBLE, if (visible) 1 else 0)
      context.contentResolver.insert(Constants.DB_URI, values)
    }
  }

}