package com.binlee.learning.ffmpeg.player

import android.media.MediaCodec
import android.media.MediaCodec.BufferInfo
import android.media.MediaCodec.CodecException
import android.media.MediaFormat
import android.util.Log
import com.binlee.learning.ffmpeg.Packet

/**
 * Created on 2023/3/21
 *
 * @author binlee
 */
class AsyncPlayer: AacPlayer() {

  override fun onDecoderCreated() {
    // 异步处理
    decoder!!.setCallback(object : MediaCodec.Callback() {
      override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        // 读取数据填充到 buffer 中
        val readBytes = extractor!!.readSampleData(temp, temp.arrayOffset())
        // Log.d(TAG, "onInputBufferAvailable() buffer($index), readBytes($readBytes)")
        if (readBytes > 0) {
          val buffer = codec.getInputBuffer(index)
          buffer?.put(temp)
          codec.queueInputBuffer(index, 0, readBytes, 0, 0)
          extractor!!.advance()
        } else {
          codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
        }
        temp.clear()
      }

      override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: BufferInfo) {
        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
          // 解码结束
          eof = true
          stopDecoder()
        } else if (index >= 0) {
          // 消费解码后的
          val packet = Packet(ByteArray(info.size), info.size)
          val buffer = codec.getOutputBuffer(index)
          buffer?.get(packet.buffer, 0, packet.size)
          enqueuePacket(packet)
          codec.releaseOutputBuffer(index, false)
        }
      }

      override fun onError(codec: MediaCodec, e: CodecException) {
      }

      override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
      }
    })
    Log.d(TAG, "onDecoderCreated() start decoder as async mode")
  }
}