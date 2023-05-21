package com.binlee.learning

import android.Manifest
import android.app.ActivityManager
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
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
    if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      loadJvmti()
    } else {
      askPermission(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }
  }

  override fun whenPermissionResult(permissions: Array<out String>, grantResults: BooleanArray) {
    if (grantResults[0]) loadJvmti()
  }

  private fun enterPipMode() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val params = PictureInPictureParams.Builder()
          .setAspectRatio(Rational(16, 9))
          .build()
      enterPictureInPictureMode(params)
    }
    mBind!!.root.postDelayed({
      val mgr = getSystemService(ACTIVITY_SERVICE) as ActivityManager
      var m = mgr.javaClass.getDeclaredMethod("getService").apply { isAccessible = true }
      var service = m.invoke(mgr)
      Log.d(TAG, "enterPipMode() ams: $service")
      for (proc in mgr.runningAppProcesses) {
        Log.d(TAG, "enterPipMode() running proc name: ${proc.processName}")
      }
    }, 2000L)
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
    DataEngine.get().indexModel().getModules().observe(this) {
      when (it) {
        is Result.Loading -> {
          Log.d(TAG, "initView() loading data: $it")
        }
        is Result.Error -> {
          Log.d(TAG, "initView() load data error: $it")
        }
        else -> {
          // Log.d(TAG, "initView() load data success: $it")
          dataSet.clear()
          dataSet.addAll((it as Result.Success).getData())
          mBind!!.recyclerView.adapter?.notifyItemRangeChanged(0, dataSet.size - 1)
          Log.d(TAG, "show UI cost: ${System.currentTimeMillis() - start} ms")
        }
      }
    }
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
    // threadException()
  }

  private fun threadException() {
    thread(start = true, name = "Exception-Thread") {
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
    thread(start = true, name = "Github-Thread") {
      val service = RetrofitClient.get().create(IDemo::class.java)
      service.listApis().enqueue(object : Callback<Apis> {
        override fun onResponse(call: Call<Apis>, response: Response<Apis>) {
          Log.v(TAG, "response: ${response.body()}")
        }

        override fun onFailure(call: Call<Apis>, t: Throwable) {
          Log.e(TAG, "error: $t")
        }
      })
    }
  }

  private fun baidu() {
    thread(start = true, name = "Baidu-Thread") {
      val demo = RetrofitClient.get().create(IDemo::class.java)
      val result = demo.visit("text/html").execute().body()
      Log.v(TAG, "retrofit result: $result")

      demo.visit().subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .subscribe {
          Log.v(TAG, "rxjava result: $it")
        }

      Log.v(TAG, "list result: " + demo.list())
      val array = demo.byteArray()
      Log.v(TAG, "byte array result: " + array.size + " " + String(array))

      val webPage = demo.webPage()
      Log.v(TAG, "web page result: $webPage")
    }
  }

  private fun reflectHiddenApiWithoutWarning() {
    // Hidden.relieve(application)
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
        if (item.cls == "enter_pip_mode") {
          enterPipMode()
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