package com.binlee.emoji.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.binlee.emoji.HttpAdapter
import com.binlee.emoji.compat.HttpEngine
import com.binlee.emoji.databinding.FragmentMainBinding
import com.binlee.emoji.helper.LogHelper
import okhttp3.*
import java.io.IOException
import kotlin.concurrent.thread

/**
 * A placeholder fragment containing a simple view.
 */

private const val TAG = "MainActivityFragment"

class MainActivityFragment : Fragment() {

  private var request: HttpEngine.BaseRequest? = null

  // private val url = "https://www.baidu.com/"
  private val url = "http://www.minxing365.com/"

  private lateinit var mBinding: FragmentMainBinding

  override fun onAttach(context: Context) {
    super.onAttach(context)
    LogHelper.debug(TAG, "onAttach() -> $context")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    LogHelper.debug(TAG, "onCreate() ->")
    request = HttpEngine.BaseRequest.base(url, HttpEngine.GET)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    LogHelper.debug(TAG, "onCreateView() ->")
    mBinding = FragmentMainBinding.inflate(inflater, container, false)
    return mBinding.root
  }

  @Deprecated("Deprecated in Java")
  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    LogHelper.debug(TAG, "onActivityCreate() ->")
  }

  override fun onStart() {
    super.onStart()
    LogHelper.debug(TAG, "onStart() ->")
  }

  override fun onResume() {
    super.onResume()
    LogHelper.debug(TAG, "onResume() ->")
  }

  override fun onPause() {
    super.onPause()
    LogHelper.debug(TAG, "onPause() ->")
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    LogHelper.debug(TAG, "onSaveInstanceState() ->")
  }

  override fun onStop() {
    super.onStop()
    LogHelper.debug(TAG, "onStop() ->")
  }

  override fun onDestroyView() {
    super.onDestroyView()
    LogHelper.debug(TAG, "onDestroyView() ->")
  }

  override fun onDestroy() {
    super.onDestroy()
    LogHelper.debug(TAG, "onDestroy() ->")
  }

  override fun onDetach() {
    super.onDetach()
    LogHelper.debug(TAG, "onDetach() ->")
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    mBinding.syncRequest.setOnClickListener {
      sendRequest(request!!, false)
    }
    mBinding.asyncRequest.setOnClickListener {
      sendRequest(request!!)
    }
  }

  private fun sendRequest(request: HttpEngine.BaseRequest, async: Boolean = true) {
    if (async) {
      HttpAdapter.engine().request(request, object : HttpEngine.Callback() {
        override fun onFailure(e: Throwable?) {
          mBinding.helloWorld.append(e?.message)
        }

        override fun onResponse(response: HttpEngine.BaseResponse?) {
          response.let { mBinding.helloWorld.text = it?.string() }
        }
      })
    } else {
      thread(start = true, name = "HttpEngine-Thread") {
        try {
          val result = HttpAdapter.engine().request(request).string()
          mBinding.helloWorld.post { mBinding.helloWorld.text = result }
        } catch (e: IOException) {
          val result = e.toString()
          mBinding.helloWorld.post { mBinding.helloWorld.text = result }
          return@thread
        }
      }
    }
    // okhttpSend(async)
  }

  private fun okhttpSend(async: Boolean) {
    val client = OkHttpClient.Builder().build()
    val r = Request.Builder()
      .get()
      .url("https://www.baidu.com/")
      .build()
    if (async) {
      client.newCall(r).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
          val result = response.body.string()
          Log.d("Http", "onResponse()\n$result")
          mBinding.helloWorld.post { mBinding.helloWorld.append(result) }
        }

        override fun onFailure(call: Call, e: IOException) {
          Log.d("Http", "onFailure()\n$e")
        }
      })
    } else {
      Thread {
        val response = client.newCall(r).execute()
        val result = response.body.string()
        Log.d("Http", "sync result\n$result")
        mBinding.helloWorld.post { mBinding.helloWorld.append(result) }
      }.start()
    }
  }
}
