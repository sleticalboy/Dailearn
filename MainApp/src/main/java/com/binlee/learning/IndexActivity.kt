package com.binlee.learning

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.binlee.hidden.Hidden
import com.binlee.learning.base.BaseActivity
import com.binlee.learning.bean.ModuleItem
import com.binlee.learning.data.DataEngine
import com.binlee.learning.data.Result
import com.binlee.learning.databinding.ActivityListItemBinding
import com.binlee.learning.http.IDemo
import com.binlee.learning.http.RetrofitClient
import com.binlee.learning.http.bean.Apis
import com.binlee.learning.others.KeyboardHeightProvider
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.concurrent.thread

class IndexActivity : BaseActivity() {

  private val dataSet = arrayListOf<ModuleItem>()
  private var mBind: ActivityListItemBinding? = null

  override fun prepareWork(savedInstanceState: Bundle?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
    } else {
      loadJvmti()
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      loadJvmti();
    }
  }

  private fun loadJvmti() {
    // JvmtiLoader.attachAgent(this)
  }

  override fun layout(): View {
    // R.layout.activity_index
    mBind = ActivityListItemBinding.inflate(layoutInflater)
    return mBind!!.root
  }

  override fun initView() {
    mBind!!.recyclerView.adapter = DataAdapter(dataSet)
    val start = System.currentTimeMillis()
    DataEngine.get().indexModel().getModules().observe(this, {
      when (it) {
        is Result.Loading -> {
          Log.d(logTag(), "initView() loading data: $it")
        }
        is Result.Error -> {
          Log.d(logTag(), "initView() load data error: $it")
        }
        else -> {
          Log.d(logTag(), "initView() load data success: $it")
          dataSet.clear()
          dataSet.addAll((it as Result.Success).getData())
          mBind!!.recyclerView.adapter?.notifyItemRangeChanged(0, dataSet.size - 1)
          Log.d(logTag(), "show UI cost: ${System.currentTimeMillis() - start} ms")
        }
      }
    })
    KeyboardHeightProvider.inject(this, object : KeyboardHeightProvider.HeightObserver {
      override fun onHeightChanged(height: Int, orientation: Int) {
        // 检测到软键盘弹出
      }
    })
  }

  override fun initData() {
    // baidu()
    // github()
    // coroutines()
    threadException()
  }

  private fun threadException() {
    thread {
      Thread.sleep(5000L)
      try {
        throw NullPointerException("thread throw exception.")
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  private fun coroutines() {
    runBlocking {
      repeat(5) {
        Log.d(TAG, "coroutines() called")
        delay(500L)
      }
    }
    val job = GlobalScope.launch {
      delay(500L)
      Log.d(TAG, "coroutines() called")
    }
    job.cancel()
    // job.join()
  }

  private fun github() {
    thread {
      val service = RetrofitClient.get().create(IDemo::class.java)
      service.listApis().enqueue(object : Callback<Apis> {
        override fun onResponse(call: Call<Apis>, response: Response<Apis>) {
          Log.v(logTag(), "response: ${response.body()}")
        }

        override fun onFailure(call: Call<Apis>, t: Throwable) {
          Log.e(logTag(), "error: $t")
        }
      })
    }
  }

  private fun baidu() {
    thread {
      val demo = RetrofitClient.get().create(IDemo::class.java)
      val result = demo.visit("text/html").execute().body()
      Log.v(logTag(), "retrofit result: $result")

      demo.visit().subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .subscribe {
          Log.v(logTag(), "rxjava result: $it")
        }

      Log.v(logTag(), "list result: " + demo.list())
      val array = demo.byteArray()
      Log.v(logTag(), "byte array result: " + array.size + " " + String(array))

      val webPage = demo.webPage()
      Log.v(logTag(), "web page result: $webPage")
    }

  }

  override fun onDestroy() {
    super.onDestroy()
    mBind = null
  }

  private fun reflectHiddenApiWithoutWarning() {
    Hidden.relieve(application)
    try {
      val atClass = Class.forName("android.app.ActivityThread")
      var method = atClass.getDeclaredMethod("currentActivityThread")
      val currentThread = method.invoke(null)
      method = atClass.getDeclaredMethod("getApplication")
      val app = method.invoke(currentThread)
      Log.d(TAG, "reflectHiddenApiWithoutWarning() activity thread: $currentThread, app: $app")

      method = AssetManager::class.java.getDeclaredMethod("addAssetPath", String::class.java)
      Log.d(TAG, "reflectHiddenApiWithoutWarning() AssetManager#addAssetPath: $method")
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private inner class DataAdapter(private val dataSet: ArrayList<ModuleItem>) :
    RecyclerView.Adapter<ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
      val itemView = TextView(parent.context)
      itemView.setBackgroundResource(R.drawable.module_item_bg)
      return ItemHolder(itemView)
    }

    override fun getItemCount(): Int {
      return dataSet.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
      val item = dataSet[position]
      holder.textView.text = item.title
      holder.textView.setOnClickListener {
        Log.d(TAG, "item click with: ${item.clazz}")
        if (item.cls == "crack_hidden_api") {
          reflectHiddenApiWithoutWarning()
          return@setOnClickListener
        }
        if (item.clazz == Any::class || item.cls == "java.lang.Object") {
          Toast.makeText(holder.itemView.context, "未知 activity: ${item.clazz}", Toast.LENGTH_SHORT).show()
          return@setOnClickListener
        }
        holder.itemView.context.startActivity(Intent(holder.itemView.context, item.clazz))
      }
    }
  }

  private class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val textView = itemView as TextView

    init {
      textView.gravity = Gravity.CENTER
      textView.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
      )
      textView.setPadding(32, 16, 32, 16)
      textView.textSize = 24F
      textView.setTextColor(Color.BLUE)
    }
  }

  companion object {
    private const val TAG = "IndexActivity"
  }
}