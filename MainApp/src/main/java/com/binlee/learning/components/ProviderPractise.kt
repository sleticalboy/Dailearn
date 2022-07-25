package com.binlee.learning.components

import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.binlee.learning.R
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.databinding.ActivityProviderBinding

class ProviderPractise : BaseActivity() {

  private var mResult: TextView? = null

  override fun layout(): View {
    // R.layout.activity_provider
    return ActivityProviderBinding.inflate(layoutInflater).root
  }

  override fun initView() {
    mResult = findViewById(R.id.queryResult)
    val table = findViewById<EditText>(R.id.etTable)
    findViewById<View>(R.id.btnQuery).setOnClickListener {
      doQuery(table.text.toString().trim { it <= ' ' })
    }
  }

  private fun doQuery(table: String) {
    require(!TextUtils.isEmpty(table)) { "table is null." }
    val projection = arrayOf("mac_address")
    val cursor = contentResolver.query(
      Uri.parse("$BASE_URI/$table"),
      projection, null, null, null
    )
    if (cursor != null) {
      if (cursor.moveToFirst()) {
        val mac = cursor.getString(cursor.getColumnIndex(projection[0]))
        mResult!!.text = mac
      }
      try {
        cursor.close()
      } catch (t: Throwable) {
        Log.d(TAG, "doQuery() error with: $table", t)
      }
    }
  }

  companion object {
    private const val TAG = "MainActivity"
    private const val BASE_URI = "com.binlee.dailywork.store"
  }
}