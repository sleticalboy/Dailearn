package com.binlee.emoji.third

import com.binlee.emoji.compat.HttpEngine
import com.binlee.emoji.helper.LogHelper
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.net.URI
import java.nio.charset.Charset
import java.util.Locale

class SocketHttpEngine : HttpEngine<Socket, Socket>() {

  private val mConfig: Config?

  override fun setupClient(client: Any?) {}

  @Throws(IOException::class)
  override fun request(request: BaseRequest): BaseResponse {
    val socket = adaptRequest(request)
    writeRequest(socket, request)
    return adaptResponse(socket, request)
  }

  override fun request(request: BaseRequest, callback: Callback) {}

  @Throws(IOException::class)
  override fun adaptRequest(request: BaseRequest): Socket {
    return createSocket(request)
  }

  @Throws(IOException::class)
  override fun adaptResponse(raw: Socket, current: BaseRequest): BaseResponse {
    if (raw.isClosed) {
      throw IOException("sock is closed: $raw")
    }
    val response = BaseResponse()
    response.current = current
    val `in` = BufferedInputStream(raw.getInputStream())

    // read status line
    var ch: Int
    var buf = StringBuilder()
    while (`in`.read().also { ch = it } != '\n'.code) {
      if (ch == '\r'.code) {
        continue
      }
      buf.append(ch.toChar())
    }
    val statusLine = buf.toString()
    LogHelper.debug("SocketHttpEngine", "status line: $buf")
    val elements = statusLine.split(" ".toRegex()).toTypedArray()
    if (elements.size > 2) {
      response.protocol = elements[0]
      response.code = elements[1].toInt()
      response.msg = statusLine.substring(statusLine.indexOf(elements[1]) + 4)
    }

    // read response header
    buf = StringBuilder()
    while (`in`.read().also { ch = it } != -1) {
      if (ch == '\r'.code) {
        continue
      } else if (ch == '\n'.code) {
        break
      }
      buf.append(ch.toChar())
    }
    val headers = buf.toString()
    LogHelper.debug("SocketHttpEngine", "response headers:\n$headers")

    // read response body
    raw.close()
    trace("SocketHttpEngine", response)
    return response
  }

  @Throws(IOException::class)
  private fun createSocket(request: BaseRequest?): Socket {
    val uri = URI.create(request!!.url!!)
    var port = uri.port
    if (port < 0) {
      val scheme = uri.scheme
      if ("http" == scheme.lowercase(Locale.getDefault())) {
        port = 80
      } else if ("https" == scheme.lowercase(Locale.getDefault())) {
        port = 443
      }
    }
    // final Socket socket = SocketFactory.getDefault().createSocket();
    val socket = Socket()
    val endpoint: SocketAddress = InetSocketAddress(uri.host, port)
    socket.connect(endpoint, mConfig!!.connectTimeout * 1000)
    return socket
  }

  @Throws(IOException::class)
  private fun writeRequest(socket: Socket, request: BaseRequest) {
    val out = BufferedOutputStream(socket.getOutputStream())
    // write request line
    out.write(request.method!!.toByteArray(UTF_8))
    out.write(COLONSPACE[1].toInt())
    out.write(buildUrl(request).toByteArray(UTF_8))
    out.write(COLONSPACE[1].toInt())
    out.write("HTTP/1.1".toByteArray(UTF_8))
    out.write(CRLF) /*request line end*/
    out.flush()

    // write headers
    if (request.headers.isNotEmpty()) {
      for (name in request.headers.keys) {
        out.write(name.toByteArray(UTF_8))
        out.write(COLONSPACE)
        out.write(request.headers[name]!!.toByteArray(UTF_8))
        out.write(CRLF)
      }
      out.write(CRLF) /*header end*/
      out.flush()
    }

    // write request body
    if (request.params != null && request.params!!.isNotEmpty()) {
      // have not implemented yet
      out.write(CRLF) /*request end*/
      out.flush()
    }
  }

  companion object {
    private val UTF_8 = Charset.forName("UTF-8")
    private val CRLF = byteArrayOf('\r'.code.toByte(), '\n'.code.toByte())
    private val COLONSPACE = byteArrayOf(':'.code.toByte(), ' '.code.toByte())
  }

  init {
    mConfig = createConfig()
  }
}