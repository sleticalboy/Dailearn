package com.binlee.learning.ffmpeg.player

import android.media.MediaCodec
import android.util.Log
import com.binlee.learning.ffmpeg.Packet
import kotlin.concurrent.thread

/**
 * Created on 2023/3/21
 *
 * @author binlee
 */
class SyncPlayer: AacPlayer() {

  override fun onDecoderStarted() {
    // start reader(encoder) thread
    thread(start = true, name = "${TAG}-Reader-thread") {
      Log.e(TAG, "reader thread enter!")
      while (true) {
        readFileOnce()
        if (eof) break
      }
      Log.e(TAG, "reader thread exit!")
      stopDecoder()
    }
  }

  private fun readFileOnce() {
    if (!eof) {
      val readBytes = extractor!!.readSampleData(temp, temp.arrayOffset())
      if (readBytes > 0) {
        // Log.d(TAG, "decoder thread: readBytes($readBytes), ts(${extractor.sampleTime / 1000f})")
        temp.get(buffer, 0, readBytes)
        temp.clear()
      }
      // 发送 aac 数据到解码器(第一次 eof 是为了发送一次 EOS 给解码器)
      queueInputBuffer(decoder!!, Packet(buffer, readBytes))
      // 判断文件是否读完
      eof = readBytes < 0 || !extractor!!.advance()
      if (eof) Log.w(TAG, "readFileOnce() eof -> readBytes($readBytes)")
    }
    // 从解码器接收 pcm 数据
    while (true) {
      val pair: Pair<Packet?/*pcm packet*/, Boolean/*eos*/> = dequeueOutputBuffer(decoder!!)

      // 判断是否解码结束(eos)
      if (pair.first == null || pair.second) break

      // 解码后的数据送入播放队列等待写入 AudioTrack 进行播放
      pair.first?.let { enqueuePacket(it) }
    }
  }

  private fun queueInputBuffer(decoder: MediaCodec, packet: Packet) {
    val index = decoder.dequeueInputBuffer(100L)
    if (index < 0) {
      Log.w(TAG, "queueInputBuffer() no available buffer($index)")
      return
    }
    val buffer = decoder.getInputBuffer(index)
    if (packet.size < 0) {
      // EOS
      Log.w(TAG, "queueInputBuffer() hit EOS!")
      decoder.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
    } else {
      buffer?.put(packet.buffer, 0, packet.size)
      decoder.queueInputBuffer(index, 0, packet.size, 0, 0)
      // Log.d(TAG, "queueInputBuffer() buffer($index) packet.size(${packet.size})")
    }
  }

  private fun dequeueOutputBuffer(decoder: MediaCodec): Pair<Packet?, Boolean> {
    val info = MediaCodec.BufferInfo()
    val index = decoder.dequeueOutputBuffer(info, 500L)
    // 检测是否结束
    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) return Pair(null, true)
    // 正常 buffer
    if (index >= 0) {
      val buffer = decoder.getOutputBuffer(index)
      // Log.d(TAG, "dequeueOutputBuffer() buffer($index) size(${info.size})")
      val packet = Packet(ByteArray(info.size), info.size)
      buffer?.get(packet.buffer, 0, info.size)
      decoder.releaseOutputBuffer(index, false)
      return Pair(packet, false)
    }
    // 其他信息
    // if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
    //   Log.w(TAG, "dequeueOutputBuffer() codec config: ${decoder.outputFormat}")
    // } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
    //   Log.w(TAG, "dequeueOutputBuffer() output format changed to ${decoder.outputFormat}")
    // } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
    //   Log.w(TAG, "dequeueOutputBuffer() try again later")
    // } else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
    //   Log.w(TAG, "dequeueOutputBuffer() output buffers changed")
    // }
    return Pair(null, false)
  }

}