package com.binlee.learning.accounts.auth

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.accounts.NetworkErrorException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import com.binlee.learning.accounts.Constants

/**
 * Created by AndroidStudio on 20-2-23.
 *
 * @author binlee
 */
class Authenticator(private val mContext: Context) : AbstractAccountAuthenticator(mContext),
  Constants {

  init {
    // Log.d(TAG, "Authenticator() called with: context = [" + context + "]");
  }

  @Throws(NetworkErrorException::class)
  override fun addAccount(
    response: AccountAuthenticatorResponse, accountType: String,
    authTokenType: String, requiredFeatures: Array<String>, options: Bundle
  ): Bundle {
    Log.d(
      TAG, "addAccount() called with: response = [" + response + "], accountType = ["
          + accountType + "], authTokenType = [" + authTokenType + "], options = [" + options + "]"
    )
    if (options.keySet() != null) {
      for (key in options.keySet()) {
        Log.d(TAG, "addAccount: key = " + key + ", value = " + options[key])
      }
    }
    val intent = Intent(mContext, AuthenticatorActivity::class.java)
    intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
    val result = Bundle()
    result.putParcelable(AccountManager.KEY_INTENT, intent)
    return result
  }

  @Throws(NetworkErrorException::class)
  override fun getAuthToken(
    response: AccountAuthenticatorResponse, account: Account,
    authTokenType: String, options: Bundle
  ): Bundle {
    Log.d(
      TAG, "getAuthToken() called with: response = [" + response + "], account = ["
          + account + "], authTokenType = [" + authTokenType + "], options = ["
          + options + "]"
    )
    if (Constants.ACCOUNT_AUTH_TOKEN != authTokenType) {
      val result = Bundle()
      result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType")
      return result
    }
    val am = AccountManager.get(mContext)
    val password = am.getPassword(account)
    if (password != null) {
      // pretend requesting auth token.
      SystemClock.sleep(300L)
      val authToken: String = Constants.ACCOUNT_AUTH_TOKEN
      if (!TextUtils.isEmpty(authToken)) {
        val result = Bundle()
        result.putString(AccountManager.KEY_ACCOUNT_NAME, Constants.ACCOUNT_NAME)
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE)
        result.putString(AccountManager.KEY_AUTHTOKEN, authToken)
        return result
      }
    }
    val intent = Intent(mContext, AuthenticatorActivity::class.java)
    intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name)
    intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, authTokenType)
    intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
    val result = Bundle()
    result.putParcelable(AccountManager.KEY_INTENT, intent)
    return result
  }

  override fun editProperties(response: AccountAuthenticatorResponse, accountType: String): Bundle {
    Log.d(
      TAG, "editProperties() called with: response = [" + response + "], accountType = ["
          + accountType + "]"
    )
    throw NetworkErrorException()
  }

  @Throws(NetworkErrorException::class)
  override fun confirmCredentials(
    response: AccountAuthenticatorResponse, account: Account,
    options: Bundle
  ): Bundle {
    Log.d(
      TAG, "confirmCredentials() called with: response = [" + response + "], account = ["
          + account + "], options = [" + options + "]"
    )
    throw NetworkErrorException()
  }

  override fun getAuthTokenLabel(authTokenType: String): String {
    Log.d(TAG, "getAuthTokenLabel() called with: authTokenType = [$authTokenType]")
    return "auto.token.label"
  }

  @Throws(NetworkErrorException::class)
  override fun updateCredentials(
    response: AccountAuthenticatorResponse, account: Account,
    authTokenType: String, options: Bundle
  ): Bundle {
    Log.d(
      TAG, "updateCredentials() called with: response = [" + response + "], account = ["
          + account + "], authTokenType = [" + authTokenType + "], options = ["
          + options + "]"
    )
    throw NetworkErrorException()
  }

  @Throws(NetworkErrorException::class)
  override fun hasFeatures(
    response: AccountAuthenticatorResponse, account: Account,
    features: Array<String>
  ): Bundle {
    Log.d(
      TAG, "hasFeatures() called with: response = [" + response + "], account = ["
          + account + "], features = [" + features + "]"
    )
    throw NetworkErrorException()
  }

  companion object {
    private const val TAG = "Authenticator"
  }
}