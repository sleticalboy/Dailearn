package com.sleticalboy.learning.rv

import android.os.Handler
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.sleticalboy.learning.R
import com.sleticalboy.learning.base.BaseActivity
import com.sleticalboy.learning.databinding.ActivityWheelRvBinding
import com.sleticalboy.learning.rv.adapter.ItemAdapter

/**
 * Created on 18-2-7.
 *
 * @author leebin
 * @version 1.0
 * @description
 */
class WheelRVActivity : BaseActivity() {

    private val mImagesId = arrayOf(
        R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher,
        R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher
    )

    private var mRecyclerView: RecyclerView? = null
    private var mCurrentPos = Integer.MAX_VALUE shr 1

    private var mIsPause = false

    private val mHandler = Handler()
    private val mWheelTask = object : Runnable {
        override fun run() {
            if (!mIsPause) {
                mRecyclerView!!.smoothScrollToPosition(++mCurrentPos)
                mHandler.postDelayed(this, INTERVAL_TIME.toLong())
            }
        }
    }

    override fun initData() {}

    override fun onAttachedToWindow() {
        mIsPause = false
        startWheel()
    }

    override fun onDetachedFromWindow() {
        mIsPause = true
        stopWheel()
    }

    // 开启定时轮播
    private fun startWheel() {
        mHandler.postDelayed(mWheelTask, (INTERVAL_TIME / 10).toLong())
    }

    // 停止定时轮播
    private fun stopWheel() {
        mHandler.removeCallbacksAndMessages(null)
    }

    override fun onPause() {
        super.onPause()
        mIsPause = true
        stopWheel()
    }

    override fun initView() {
        mRecyclerView = findViewById<View>(R.id.recycler_view) as RecyclerView
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        mRecyclerView!!.layoutManager = layoutManager
        val adapter = ItemAdapter(this, mImagesId)
        mRecyclerView!!.adapter = adapter
        mRecyclerView!!.scrollToPosition(mCurrentPos)
        //        new LinearSnapHelper().attachToRecyclerView(mRecyclerView);
        PagerSnapHelper().attachToRecyclerView(mRecyclerView)
        //        new StartSnapHelper().attachToRecyclerView(mRecyclerView);

        mRecyclerView!!.setOnTouchListener(MyOnTouchListener())
        mRecyclerView!!.addOnScrollListener(MyOnScrollListener())
    }

    override fun layout(): View {
        // R.layout.activity_wheel_rv
        return ActivityWheelRvBinding.inflate(layoutInflater).root
    }

    private inner class MyOnTouchListener : View.OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            mIsPause = when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> true
                else -> false
            }
            return false
        }
    }

    private inner class MyOnScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (mIsPause && dy < 0) {
                mCurrentPos--
            } else if (mIsPause && dy > 0) {
                mCurrentPos++
            }
        }
    }

    companion object {
        private val INTERVAL_TIME = 3000
    }

}
