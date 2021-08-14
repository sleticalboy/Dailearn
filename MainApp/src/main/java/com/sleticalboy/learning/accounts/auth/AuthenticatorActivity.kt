package com.sleticalboy.learning.accounts.auth

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.sleticalboy.learning.R
import com.sleticalboy.learning.accounts.Constants
import com.sleticalboy.learning.base.BaseActivity
import com.sleticalboy.learning.databinding.ActivityLoginBinding

class AuthenticatorActivity : BaseActivity(), Constants {

    private var mAccountAuthenticatorResponse: AccountAuthenticatorResponse? = null
    private var mResultBundle: Bundle? = null
    private var mUsername: String? = null
    private var mPassword: String? = null
    private var mAccountManager: AccountManager? = null
    private var mRequestNewAccount = false
    private var mLoading: ProgressBar? = null

    override fun prepareWork(savedInstanceState: Bundle?) {
        mAccountManager = AccountManager.get(application)
        mUsername = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        mRequestNewAccount = mUsername == null
        mAccountAuthenticatorResponse = intent.getParcelableExtra(
                AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
        if (mAccountAuthenticatorResponse != null) {
            mAccountAuthenticatorResponse!!.onRequestContinued()
        }
    }

    public override fun initView() {
        mLoading = findViewById(R.id.loading)
        val loginBtn = findViewById<Button>(R.id.login)
        val usernameEt = findViewById<EditText>(R.id.username)
        val passwordEt = findViewById<EditText>(R.id.password)
        val watcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                loginBtn.isEnabled = (!TextUtils.isEmpty(usernameEt.text)
                        && !TextUtils.isEmpty(passwordEt.text))
            }
        }
        usernameEt.addTextChangedListener(watcher)
        passwordEt.addTextChangedListener(watcher)
        loginBtn.setOnClickListener { v: View ->
            mLoading?.visibility = View.VISIBLE
            mUsername = usernameEt.text.toString().trim { it <= ' ' }
            mPassword = passwordEt.text.toString().trim { it <= ' ' }
            handleLogin(v)
        }
    }

    private fun handleLogin(v: View) {
        if (mUsername!!.startsWith(Constants.ACCOUNT_PREFIX) && mPassword!!.startsWith(Constants.ACCOUNT_PASSWORD)) {
            v.postDelayed({ finishLogin(Constants.ACCOUNT_AUTH_TOKEN) }, 300L)
        } else {
            finishLogin(null)
        }
    }

    private fun finishLogin(authToken: String?) {
        Log.d(TAG, "finishLogin() called with: authToken = [$authToken]")
        if (authToken == null || authToken.isEmpty()) {
            mLoading!!.visibility = View.GONE
            Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show()
            return
        }
        val account = Account(mUsername, Constants.ACCOUNT_TYPE)
        if (mRequestNewAccount) {
            mAccountManager!!.addAccountExplicitly(account, mPassword, null)
            // Set contacts sync for this account.
            ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true)
        } else {
            mAccountManager!!.setPassword(account, mPassword)
        }
        val intent = Intent()
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername)
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE)
        mResultBundle = intent.extras
        setResult(RESULT_OK, intent)
        mLoading!!.visibility = View.GONE
        finish()
    }

    override fun layout(): View {
        // return R.layout.activity_login
        val bind = ActivityLoginBinding.inflate(layoutInflater)
        return bind.root
    }

    override fun finish() {
        if (mAccountAuthenticatorResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (mResultBundle != null) {
                mAccountAuthenticatorResponse!!.onResult(mResultBundle)
            } else {
                mAccountAuthenticatorResponse!!.onError(AccountManager.ERROR_CODE_CANCELED,
                        "canceled")
            }
            mAccountAuthenticatorResponse = null
        }
        super.finish()
    }

    companion object {
        private const val TAG = "AuthenticatorActivity"
    }
}