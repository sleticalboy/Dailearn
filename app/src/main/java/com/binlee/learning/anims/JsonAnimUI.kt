package com.binlee.learning.anims

import android.view.View
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.databinding.ActivityJsonAnimBinding

/**
 * Created on 20-9-9.
 *
 * @author Ben binli@grandstream.cn
 */
class JsonAnimUI : BaseActivity() {

  override fun layout(): View {
    // R.layout.activity_json_anim
    return ActivityJsonAnimBinding.inflate(layoutInflater).root
  }

  override fun initView() {
  }
}