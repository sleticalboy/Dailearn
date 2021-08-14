package com.sleticalboy.learning.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.sleticalboy.learning.databinding.FragmentBaseListBinding

/**
 * Created on 20-8-23.
 *
 * @author binlee sleticalboy@gmail.com
 */
abstract class BaseListFragment<DATA> : BaseFragment() {

    private var mAdapter: BaseRVAdapter<DATA>? = null
    private var mBind: FragmentBaseListBinding? = null

    override fun logTag(): String = "BaseListFragment"

    final override fun initView(view: View) {
        initHeader(mBind!!.headerContainer)

        mBind!!.listContainer.layoutManager = LinearLayoutManager(context)
        mBind!!.listContainer.adapter = getAdapter()

        initFooter(mBind!!.footerContainer)
    }

    fun getAdapter(): BaseRVAdapter<DATA> {
        if (mAdapter == null) {
            mAdapter = createAdapter()
        }
        return mAdapter!!
    }

    protected abstract fun createAdapter(): BaseRVAdapter<DATA>

    protected open fun initHeader(headerContainer: FrameLayout) {
    }

    protected open fun initFooter(footerContainer: FrameLayout) {
    }

    final override fun layout(inflater: LayoutInflater, container: ViewGroup?): View {
        // R.layout.fragment_base_list
        mBind = FragmentBaseListBinding.inflate(inflater, container, false)
        return mBind!!.root
    }
}