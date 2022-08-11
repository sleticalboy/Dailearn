package com.binlee.emoji.compat

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.StringDef
import com.binlee.emoji.BuildConfig
import com.binlee.emoji.helper.LogHelper
import java.io.IOException
import java.io.Serializable
import java.net.Proxy

abstract class HttpEngine<RealRequest, RealResponse> {

  @Volatile
  private var mHandler: Handler? = null
  protected abstract fun setupClient(client: Any?)

  /**
   * 发送请求：同步
   *
   * @param request 请求
   * @return 响应
   */
  @Throws(IOException::class)
  abstract fun request(request: BaseRequest): BaseResponse

  /**
   * 发送请求：异步
   *
   * @param request  请求
   * @param callback 回调
   */
  abstract fun request(request: BaseRequest, callback: Callback)

  /**
   * @param request 通用的 [BaseRequest]
   * @return 实际的 http 请求
   * @throws IOException io 异常
   */
  @Throws(IOException::class)
  protected abstract fun adaptRequest(request: BaseRequest): RealRequest

  /**
   * @param raw  实际的 http 响应
   * @param current 本次响应对应的 [BaseRequest]
   * @return 通用的 [BaseResponse]
   * @throws IOException io 异常
   */
  @Throws(IOException::class)
  protected abstract fun adaptResponse(raw: RealResponse, current: BaseRequest): BaseResponse

  protected fun mainHandler(): Handler {
    if (mHandler == null) {
      mHandler = Handler(Looper.getMainLooper())
    }
    return mHandler!!
  }

  protected fun trackable(): Boolean {
    return BuildConfig.DEBUG
  }

  protected fun createConfig(): Config {
    return Config()
  }

  protected fun trace(tag: String?, response: BaseResponse) {
    if (!trackable()) {
      return
    }
    // trace request
    val request = response.current
    val buffer = StringBuilder(">>>>>>>>>>>>>>>traceRequest:\n")
    // request line
    buffer.append(request!!.method).append(" ").append(request.url).append("\n\n")
    // request headers
    for (name in request.headers.keys) {
      buffer.append(name).append(": ").append(request.headers[name]).append('\n')
    }
    buffer.append("\n")
    // params
    if (request.params != null) {
      for (name in request.params!!.keys) {
        buffer.append(name).append('=').append(request.params!![name]).append('\n')
      }
    }
    // traceResponse
    buffer.append("traceResponse:<<<<<<<<<<<<<<\n")
    // status line
    buffer.append(response.protocol).append(" ").append(response.code)
      .append(" ").append(response.msg).append("\n\n")
    // response headers
    for (name in response.headers.keys) {
      buffer.append(name).append(": ").append(response.headers[name]).append('\n')
    }
    buffer.append("\n")
    // response body
    if (response.data != null && response.data!!.isNotEmpty()) {
      buffer.append(response.string())
    }
    LogHelper.debug(tag, buffer.toString())
  }

  abstract class Callback {
    fun onProgress(progress: Float) {
      Log.d(TAG, "onProgress() called with: progress = $progress")
    }

    abstract fun onResponse(response: BaseResponse?)

    abstract fun onFailure(e: Throwable?)
  }

  class BaseRequest : Serializable {

    val headers = mutableMapOf<String, String>()

    @NonNull
    var url: String? = null

    @Method
    @NonNull
    var method: String? = null
    var params: MutableMap<String, Any>? = null

    companion object {

      private const val serialVersionUID = -316276577868557178L

      fun base(url: String, @Method method: String): BaseRequest {
        val request = BaseRequest()
        request.url = url
        request.method = method
        return request
      }
    }
  }

  class BaseResponse : Serializable {

    val headers = mutableMapOf<String?, String?>()
    var current: BaseRequest? = null
    var protocol: String? = null
    var code = 0
    var msg: String? = null
    var data: ByteArray? = null

    fun success(): Boolean {
      return code / 100 == 2
    }

    fun redirect(): Boolean {
      return code / 100 == 3
    }

    fun clientError(): Boolean {
      return code / 100 == 4
    }

    fun serverError(): Boolean {
      return code / 100 == 5
    }

    private fun bytes(): ByteArray {
      return if (data == null || data!!.isEmpty()) msg!!.toByteArray() else data!!
    }

    fun string(): String {
      return String(bytes())
    }

    companion object {
      private const val serialVersionUID = -6734972010487266836L
    }
  }

  class Config {
    /**
     * 超时时间：单位 s
     */
    var connectTimeout = 60
    var writeTimeout = 60
    var readTimeout = 60
    var proxy: Proxy? = null
    var authenticate: BaseRequest? = null

    // "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36"
    var userAgent: String = ""
  }

  @StringDef(GET, POST, PUT, DELETE)
  @Retention(AnnotationRetention.SOURCE)
  annotation class Method

  companion object {

    private const val TAG = "HttpEngine"

    const val GET = "GET"
    const val POST = "POST"
    const val PUT = "PUT"
    const val DELETE = "DELETE"

    @JvmStatic
    fun buildUrl(request: BaseRequest): String {
      if (GET != request.method || request.params == null || request.params!!.isEmpty()) {
        return request.url!!
      }
      request.params!!.size
      val builder = StringBuilder()
      builder.append(request.url)
      if (!request.url!!.contains("?")) {
        builder.append("?")
      } else {
        builder.append("&")
      }
      for ((i, name) in request.params!!.keys.withIndex()) {
        if (i != 0) {
          builder.append("&")
        }
        builder.append(name).append("=").append(request.params!![name])
      }
      return builder.toString()
    }
  }
}