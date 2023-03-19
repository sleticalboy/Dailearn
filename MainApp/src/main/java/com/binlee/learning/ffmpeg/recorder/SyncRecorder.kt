package com.binlee.learning.ffmpeg.recorder

import android.media.MediaCodec
import android.media.MediaCodec.BufferInfo
import android.util.Log
import kotlin.concurrent.thread

/**
 * Created on 3/19/23
 *
 * @author binlee
 */
internal class SyncRecorder : AacRecorder() {

  override fun afterStartEncoder() {
    // 启动编码线程，开始给编码器送数据
    thread(start = true, name = "AAC-Encoder") {
      // 循环处理
      while (true) {
        // 从队列中取数据并填充到 buffer 中后送入编码器
        enqueuePcmBuffer()
        // 从编码器取出 aac 数据，封装成 aac 帧写入文件
        eos = dequeueAacBuffer()
        if (eos) break
      }
      Log.e(TAG, "encoder thread exit!!")

      // 释放解码器
      releaseEncoder()

      beforeFinish(output!!)
    }
  }

  private fun enqueuePcmBuffer() {
    // 从队列中取出数据并填充到 buffer 中

    // 找到可用的输入 buffer 索引
    val index = encoder!!.dequeueInputBuffer(ENCODER_TIMEOUT)
    if (index < 0) {
      Log.w(TAG, "queueInputBuffer() no available buffer($index)")
      return
    }
    val buffer = encoder!!.getInputBuffer(index)

    // 等待往队列中放入数据
    Log.d(TAG, "queueInputBuffer() take pcm packet >>>>> buffer($index), recording(${isRecording()})")
    val packet = dequeuePacket()
    Log.d(TAG, "queueInputBuffer() take pcm packet <<<<< $packet")
    if (packet == null) {
      // 停止录制，告诉解码器没有数据了
      Log.e(TAG, "queueInputBuffer() pcm packet queue is empty!")
      encoder!!.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
      return
    }

    // 把数据送给编码器
    // TODO: pcm 数据长度会不会超出 buffer 限制？
    Log.d(TAG, "queueInputBuffer() buffer size: ${buffer?.capacity()}, pcm size: ${packet.size}")
    buffer?.put(packet.buffer, 0, packet.size)
    encoder!!.queueInputBuffer(index, 0, packet.size, 0, 0)
  }

  private fun dequeueAacBuffer(): Boolean {
    var res: Int
    while (true) {
      res = dequeueOutputBuffer()
      if (res == MediaCodec.BUFFER_FLAG_END_OF_STREAM) return true
      if (res == MediaCodec.INFO_TRY_AGAIN_LATER) break
    }
    return false
  }

  private fun dequeueOutputBuffer(): Int {
    // 从解码器取编码好的数据，添加 adts 头组成 aac 帧，写入文件
    val info = BufferInfo()
    // 找到可用的输出 buffer
    val index = encoder!!.dequeueOutputBuffer(info, ENCODER_TIMEOUT)

    // 检测 buffer 信息
    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
      return MediaCodec.BUFFER_FLAG_END_OF_STREAM
    }
    if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
      Log.i(TAG, "dequeueOutputBuffer() codec config")
      return MediaCodec.BUFFER_FLAG_CODEC_CONFIG
    }
    if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
      Log.i(TAG, "dequeueOutputBuffer() output format changed to ${encoder?.outputFormat}")
      return MediaCodec.INFO_OUTPUT_FORMAT_CHANGED
    }
    if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
      Log.e(TAG, "dequeueOutputBuffer() try again later")
      return MediaCodec.INFO_TRY_AGAIN_LATER
    }
    if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
      Log.i(TAG, "dequeueOutputBuffer() output buffers changed")
      return MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED
    }

    val buffer = encoder!!.getOutputBuffer(index)
    Log.i(TAG, "dequeueOutputBuffer() output buffer $index -> $buffer")

    writeAacFrame(buffer, info.size)

    // 释放输出 buffer
    encoder!!.releaseOutputBuffer(index, false)
    return index
  }
}