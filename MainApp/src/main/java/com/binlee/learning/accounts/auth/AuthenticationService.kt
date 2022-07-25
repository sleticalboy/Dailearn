package com.binlee.learning.accounts.auth

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Created by AndroidStudio on 20-2-23.
 *
 * @author binlee
 */
class AuthenticationService : Service() {

  private var mAuthenticator: Authenticator? = null

  override fun onCreate() {
    mAuthenticator = Authenticator(this)
    // Log.d(TAG, "onCreate() called");
  }

  override fun onBind(intent: Intent): IBinder? {
    // Log.d(TAG, "onBind() called with: intent = [" + intent + "]");
    return mAuthenticator!!.iBinder
  }

  companion object {
    private const val TAG = "AuthenticationService"
  }
}