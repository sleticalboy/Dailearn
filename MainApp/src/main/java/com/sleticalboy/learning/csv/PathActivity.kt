package com.sleticalboy.learning.csv

import android.view.View
import com.sleticalboy.learning.R
import com.sleticalboy.learning.base.BaseActivity
import com.sleticalboy.learning.databinding.ActivityPathBinding
import com.sleticalboy.weight.view.TestView

/**
 * Created on 18-3-1.
 *
 * @author leebin
 * @version 1.0
 */
class PathActivity : BaseActivity() {

    override fun initData() {}

    override fun initView() {
        val testView = findViewById<TestView>(R.id.test)
        val singleLine = findViewById<View>(R.id.singleLine)
        // singleLine.setOnClickListener(v -> testView.postDelayed(() -> {
        //     testView.update();
        //     singleLine.performClick();
        // }, 250L));
        singleLine.setOnClickListener { testView.invalidate() }
    }

    override fun layout(): View {
        // R.layout.activity_path
        return ActivityPathBinding.inflate(layoutInflater).root
    }
}