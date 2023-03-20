package com.binlee.learning.ffmpeg.player

import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import com.binlee.learning.ffmpeg.AVFormat.A_AAC
import com.binlee.learning.ffmpeg.Packet
import java.io.RandomAccessFile
import java.nio.ByteBuffer

/**
 * Created on 2023/3/20
 *
 * @author binlee
 */
class AacPlayer(): BasePlayer(format = A_AAC) {
  
  private var extractor: MediaExtractor? = null
  private var decoder: MediaCodec? = null
  // 文件是否结束
  private var eof = false
  private var temp: ByteBuffer = ByteBuffer.allocate(1024)
  
  override fun onReaderStarted(input: RandomAccessFile) {
    // 开启解码器，把 aac 数据解码 pcm 裸数据
    // 从 MediaExtractor 中拿到媒体信息
    extractor = MediaExtractor()
    extractor!!.setDataSource(input.fd)
    extractor!!.selectTrack(0)
    val format = extractor!!.getTrackFormat(0)
    Log.w(TAG, "startDecoder() extractor track-0 format: $format")
    // 创建 decoder
    decoder = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
    // val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 2)
    // format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
    format.setInteger(MediaFormat.KEY_BIT_RATE, 44100 * 1 * AudioFormat.ENCODING_PCM_16BIT)
    // format.setInteger(MediaFormat.KEY_IS_ADTS, 1)
    format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16 * 1024)
    decoder!!.configure(format, null, null, 0)
    Log.w(TAG, "beforeStartReader() start decoder with format: $format")
    decoder!!.start()
  }

  override fun onReaderStopped() {
    extractor?.release()
    extractor = null
    // 释放解码器
    decoder?.stop()
    decoder?.release()
    decoder = null
  }

  override fun readFile(input: RandomAccessFile, buffer: ByteArray): Boolean {
    if (!eof) {
      val readBytes = extractor!!.readSampleData(temp, temp.arrayOffset())
      if (readBytes > 0) {
        // Log.d(TAG, "decoder thread: readBytes($readBytes), ts(${extractor.sampleTime / 1000f})")
        temp.get(buffer, 0, readBytes)
      }
      // 发送 aac 数据到解码器(第一次 eof 是为了发送一次 EOS 给解码器)
      queueInputBuffer(decoder!!, Packet(buffer, readBytes))
      // 判断文件是否读完
      eof = readBytes < 0 || !extractor!!.advance()
      if (eof) {
        Log.w(TAG, "readFile() eof -> readBytes($readBytes)")
        queueInputBuffer(decoder!!, Packet(buffer, -1/*eos*/))
      }
    }
    // 从解码器接收 pcm 数据
    val pair: Pair<Packet?/*pcm packet*/, Boolean/*eos*/> = dequeueOutputBuffer(decoder!!)

    // 判断是否解码结束(eos)
    if (pair.second) return true

    // 解码后的数据送入播放队列等待写入 AudioTrack 进行播放
    pair.first?.let { enqueuePacket(it) }
    return false
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
    if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
      Log.w(TAG, "dequeueOutputBuffer() codec config: ${decoder.outputFormat}")
    } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
      Log.w(TAG, "dequeueOutputBuffer() output format changed to ${decoder.outputFormat}")
    } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
      Log.w(TAG, "dequeueOutputBuffer() try again later")
    } else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
      Log.w(TAG, "dequeueOutputBuffer() output buffers changed")
    }
    return Pair(null, false)
  }
}