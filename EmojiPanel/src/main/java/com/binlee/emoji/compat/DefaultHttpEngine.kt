package com.binlee.emoji.compat

import com.binlee.emoji.helper.LogHelper
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class DefaultHttpEngine : HttpEngine<HttpURLConnection, HttpURLConnection>() {

  private val mLock = Object()
  private val mConfig: Config?

  @Volatile
  private var mService: ExecutorService? = null

  @Volatile
  private var mReauthenticating = false

  @Volatile
  private var redirectCount = 0
  private fun service(): ExecutorService {
    if (mService == null) {
      mService = ThreadPoolExecutor(
        0, Int.MAX_VALUE, 60, TimeUnit.SECONDS,
        SynchronousQueue()
      ) { r: Runnable? -> Thread(r, "DefaultHttpEngine") }
    }
    return mService!!
  }

  override fun setupClient(client: Any?) {
    if (client !is HttpURLConnection) {
      return
    }
    client.connectTimeout = mConfig!!.connectTimeout * 1000
    client.readTimeout = mConfig.readTimeout * 1000
    client.instanceFollowRedirects = true
    client.doInput = true
    client.useCaches = false
  }

  @Throws(IOException::class)
  override fun request(request: BaseRequest): BaseResponse {
    while (mReauthenticating) {
      synchronized(mLock) {
        try {
          mLock.wait()
        } catch (ignored: InterruptedException) {
        }
      }
    }
    val raw = adaptRequest(request)
    writeOutputStream(raw, request)
    return adaptResponse(raw, request)
  }

  override fun request(request: BaseRequest, callback: Callback) {
    // 异步请求要在子线程中执行，然后回调
    service().execute {
      try {
        val raw = request(request)
        mainHandler().post { callback.onResponse(raw) }
      } catch (e: IOException) {
        mainHandler().post { callback.onFailure(e) }
      }
    }
  }

  @Throws(IOException::class)
  override fun adaptRequest(request: BaseRequest): HttpURLConnection {
    val conn = openConnection(buildUrl(request), mConfig!!.proxy)
    setupClient(conn)
    conn.requestMethod = request.method
    request.headers["User-Agent"] = mConfig.userAgent
    for (name in request.headers.keys) {
      conn.addRequestProperty(name, request.headers[name])
    }
    return conn
  }

  @Throws(IOException::class)
  override fun adaptResponse(raw: HttpURLConnection, current: BaseRequest): BaseResponse {
    return resolveConnection(raw, current)
  }

  @Throws(IOException::class)
  private fun openConnection(url: String?, proxy: Proxy?): HttpURLConnection {
    if (url!!.startsWith("https")) {
      // config https
    }
    return if (proxy == null) {
      URL(url).openConnection() as HttpURLConnection
    } else URL(url).openConnection() as HttpURLConnection
  }

  @Throws(IOException::class)
  private fun writeOutputStream(conn: HttpURLConnection, request: BaseRequest) {
    conn.connect()
    if (POST != request.method && PUT != request.method) {
      return
    }
    if (request.params == null || request.params!!.isEmpty()) {
      return
    }
    // 发送表单数据
    // final BufferedOutputStream out = new BufferedOutputStream(conn.getOutputStream());
  }

  @Throws(IOException::class)
  private fun resolveConnection(conn: HttpURLConnection, current: BaseRequest): BaseResponse {
    val response = BaseResponse()
    // status line
    val statusLine = conn.getHeaderField(null)
    if (statusLine != null) {
      val elements = statusLine.split(" ".toRegex()).toTypedArray()
      if (elements.size > 2) {
        response.protocol = elements[0]
        response.code = elements[1].toInt()
        response.msg = statusLine.substring(statusLine.indexOf(elements[1]) + 4)
      }
    } else {
      response.protocol = conn.url.protocol
      response.code = conn.responseCode
      response.msg = conn.responseMessage
    }
    val headerFields = conn.headerFields
    for (name in headerFields.keys) {
      // skip status line
      if (name != null) {
        response.headers[name] = toString(headerFields[name])
      }
    }
    response.current = current
    if (response.redirect()) {
      val redirectUrl = response.headers["Location"]
      if (redirectUrl != null) {
        val old = redirectCount
        redirectCount = old + 1
        LogHelper.debug("DefHttpEngine", "redirect $redirectUrl $redirectCount")
        val redirect = BaseRequest()
        redirect.method = current.method
        redirect.url = redirectUrl
        redirect.headers.putAll(current.headers)
        redirect.params = current.params
        return request(redirect)
      }
    }
    if (response.clientError()) {
      if (response.code == 401 && mConfig!!.authenticate != null) {
        // 重新认证
        mReauthenticating = true
        val raw = request(mConfig.authenticate!!)
        onReauthenticateResponse(raw)
        synchronized(mLock) {
          mReauthenticating = false
          mLock.notifyAll()
        }
        // 继续发送`发生重新认证`时的请求
        return request(response.current!!)
      }
    }
    response.data = toBytes(if (response.success()) conn.inputStream else conn.errorStream)
    if (response.data == null || response.data!!.isEmpty()) {
      response.data = response.msg!!.toByteArray()
    }
    trace("DefHttpEngine", response)
    return response
  }

  private fun onReauthenticateResponse(response: BaseResponse?) {
    LogHelper.debug("DefHttpEngine", "onReauthenticateResponse() $response")
  }

  @Throws(IOException::class)
  private fun toBytes(`in`: InputStream?): ByteArray {
    // do not close the `in`
    if (`in` == null) {
      return ByteArray(0)
    }
    val buffer = ByteArrayOutputStream()
    var len: Int
    var data = ByteArray(8192)
    while (`in`.read(data).also { len = it } != -1) {
      buffer.write(data, 0, len)
    }
    buffer.flush()
    data = buffer.toByteArray()
    buffer.close()
    return data
  }

  companion object {
    private fun toString(list: List<String>?): String {
      if (list == null) {
        return ""
      }
      val builder = StringBuilder()
      for (i in list.indices) {
        if (i != 0) {
          builder.append("; ")
        }
        builder.append(list[i])
      }
      return builder.toString()
    }
  }

  init {
    mConfig = createConfig()
  }
}