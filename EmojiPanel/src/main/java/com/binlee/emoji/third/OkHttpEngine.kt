package com.binlee.emoji.third

import com.binlee.emoji.compat.HttpEngine
import okhttp3.Authenticator
import okhttp3.Call
import okhttp3.Headers
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import java.io.File
import java.io.IOException
import java.util.HashMap
import java.util.concurrent.TimeUnit

class OkHttpEngine : HttpEngine<Request, Response>() {

  @Volatile
  private var mClient: OkHttpClient? = null
  private lateinit var mConfig: Config

  private fun client(): OkHttpClient {
    if (mClient == null) {
      synchronized(this) {
        if (mClient == null) {
          setupClient(null)
        }
      }
    }
    return mClient!!
  }

  override fun setupClient(client: Any?) {
    mConfig = createConfig()
    val builder = OkHttpClient.Builder()
    builder.connectTimeout(mConfig.connectTimeout.toLong(), TimeUnit.SECONDS)
    builder.writeTimeout(mConfig.writeTimeout.toLong(), TimeUnit.SECONDS)
    builder.readTimeout(mConfig.readTimeout.toLong(), TimeUnit.SECONDS)
    builder.proxy(mConfig.proxy)
    val authenticator = if (mConfig.authenticate == null) {
      Authenticator.NONE
    } else {
      object : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
          return adaptRequest(mConfig.authenticate!!)
        }
      }
    }
    builder.authenticator(authenticator)
    mClient = builder.build()
  }

  @Throws(IOException::class)
  override fun request(request: BaseRequest): BaseResponse {
    val raw = client().newCall(adaptRequest(request)).execute()
    val response = adaptResponse(raw, request)
    trace("OkHttpEngine", response)
    return response
  }

  override fun request(request: BaseRequest, callback: Callback) {
    client().newCall(adaptRequest(request)).enqueue(object : okhttp3.Callback {
      override fun onFailure(call: Call, e: IOException) {
        mainHandler().post { callback.onFailure(e) }
      }

      @Throws(IOException::class)
      override fun onResponse(call: Call, response: Response) {
        val cooked = adaptResponse(response, request)
        mainHandler().post { callback.onResponse(cooked) }
        trace("OkHttpEngine", cooked)
      }
    })
  }

  override fun adaptRequest(request: BaseRequest): Request {
    val builder = Request.Builder()
    builder.url(buildUrl(request))
    builder.headers(toHeaders(request.headers))
    when (request.method) {
      GET -> {
        builder.get()
      }
      POST -> {
        builder.post(toRequestBody(request))
      }
      PUT -> {
        builder.put(toRequestBody(request))
      }
      DELETE -> {
        builder.delete()
      }
    }
    return builder.build()
  }

  private fun toRequestBody(request: BaseRequest): RequestBody {
    val builder = MultipartBody.Builder()
    for (name in request.params!!.keys) {
      val value = request.params!![name]
      if (value is File) {
        // type File
        builder.addPart(
          MultipartBody.Part.Companion.createFormData(
            "files", value.name, value.asRequestBody()
          )
        )
      } else {
        // type String
        builder.addPart(value.toString().toRequestBody())
      }
    }
    return builder.build()
  }

  private fun toHeaders(headers: MutableMap<String, String>): Headers {
    headers["User-Agent"] = mConfig.userAgent
    return headers.toHeaders()
  }

  @Throws(IOException::class)
  override fun adaptResponse(raw: Response, current: BaseRequest): BaseResponse {
    val response = BaseResponse()
    response.protocol = raw.protocol.toString()
    response.code = raw.code
    response.msg = raw.message
    response.headers.putAll(toHeaders(raw.headers))
    if (raw.body != null) {
      response.data = raw.body.bytes()
    }
    response.current = current
    return response
  }

  private fun toHeaders(headers: Headers): MutableMap<String?, String?> {
    val ret: MutableMap<String?, String?> = HashMap()
    for (i in 0 until headers.size) {
      ret[headers.name(i)] = headers.value(i)
    }
    return ret
  }
}