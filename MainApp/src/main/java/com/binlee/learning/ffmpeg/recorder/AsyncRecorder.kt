package com.binlee.learning.ffmpeg.recorder

import android.media.MediaCodec
import android.media.MediaCodec.BufferInfo
import android.media.MediaCodec.CodecException
import android.media.MediaFormat
import android.util.Log

/**
 * Created on 3/19/23
 *
 * @author binlee
 */
class AsyncRecorder : AacRecorder() {

  override fun beforeStartEncoder() {
  encoder!!.setCallback(object : MediaCodec.Callback() {
    override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
      // 拿到可用的 buffer
      val buffer = codec.getInputBuffer(index)

      // 等待往队列中放入数据
      val packet = dequeuePacket()
      if (packet == null) {
        // 停止录制，告诉解码器没有数据了
        Log.e(TAG, "onInputBufferAvailable() pcm packet queue empty!")
        codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
        return
      }
      // 把数据送给编码器
      Log.d(TAG, "onInputBufferAvailable() buffer size: ${buffer?.capacity()}, pcm size: ${packet.size}")
      buffer?.put(packet.buffer, 0, packet.size)
      codec.queueInputBuffer(index, 0, packet.size, 0, 0)
    }

    override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: BufferInfo) {
      if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
        Log.i(TAG, "onOutputBufferAvailable() codec config")
        codec.releaseOutputBuffer(index, false)
        return
      }
      if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
        Log.w(TAG, "onOutputBufferAvailable() encoder finish!")
        releaseEncoder()
        beforeFinish(output!!)
        return
      }
      val buffer = codec.getOutputBuffer(index)
      Log.d(TAG, "onOutputBufferAvailable() buffer($index) -> $buffer")
      writeAacFrame(buffer, info.size)
      codec.releaseOutputBuffer(index, false)
    }

    override fun onError(codec: MediaCodec, e: CodecException) {
      Log.d(TAG, "onError() e = $e")
      releaseEncoder()
    }

    override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
    }
  })
  }
}