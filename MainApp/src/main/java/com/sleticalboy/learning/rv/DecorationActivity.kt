package com.sleticalboy.learning.rv

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.sleticalboy.learning.R
import com.sleticalboy.learning.base.BaseActivity
import com.sleticalboy.learning.databinding.ActivityDecorationBinding
import com.sleticalboy.learning.rv.adapter.ItemTouchAdapter
import com.sleticalboy.weight.xrecycler.decoration.DividerGridItemDecoration
import com.sleticalboy.weight.xrecycler.helper.SelectedItemDragItemTouchCallback


/**
 * Created on 18-2-7.
 *
 * @author leebin
 * @version 1.0
 * @description
 */
class DecorationActivity : BaseActivity() {

    private val mImagesIds = arrayOf(
            R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher,
            R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher)

    override fun initData() {}
    override fun initView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        val adapter = ItemTouchAdapter(this, mImagesIds)
        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(this, 3)
        // layoutManager = new StaggeredGridLayoutManager(4, LinearLayout.HORIZONTAL);
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        //        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayout.HORIZONTAL));
        val decor = DividerGridItemDecoration(this, 8)
        decor.setDivider(R.drawable.divider)
        recyclerView.addItemDecoration(decor)
        //        recyclerView.addItemDecoration(new ItemTouchHelper(new SelectedItemDragItemTouchCallback(adapter)));
        ItemTouchHelper(SelectedItemDragItemTouchCallback(adapter)).attachToRecyclerView(recyclerView)
    }

    override fun layout(): View {
        // R.layout.activity_decoration
        val bind = ActivityDecorationBinding.inflate(layoutInflater)
        return bind.root
    }
}