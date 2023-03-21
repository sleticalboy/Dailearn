package com.binlee.learning.ffmpeg.player

import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import com.binlee.learning.ffmpeg.AVFormat.A_AAC
import com.binlee.learning.ffmpeg.IPlayer.Callback
import com.binlee.learning.ffmpeg.Packet
import java.nio.ByteBuffer
import java.util.LinkedList

/**
 * Created on 2023/3/20
 *
 * @author binlee
 */
abstract class AacPlayer : BasePlayer(format = A_AAC) {

  private val queueLock = Object()
  private val queue = LinkedList<Packet>()
  protected var extractor: MediaExtractor? = null
  protected var decoder: MediaCodec? = null
  protected var temp: ByteBuffer = ByteBuffer.allocate(1024)

  // 文件是否结束
  protected var eof = false

  override fun start(callback: Callback) {
    super.start(callback)
    // 开启解码器，把 aac 数据解码 pcm 裸数据
    // 从 MediaExtractor 中拿到媒体信息
    extractor = MediaExtractor()
    extractor!!.setDataSource(input!!.fd)
    extractor!!.selectTrack(0)
    val format = extractor!!.getTrackFormat(0)
    Log.w(TAG, "start() extractor track-0 format: $format")
    // 创建 decoder
    decoder = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
    // val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 2)
    // format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
    format.setInteger(MediaFormat.KEY_BIT_RATE, 44100 * 1 * AudioFormat.ENCODING_PCM_16BIT)
    // format.setInteger(MediaFormat.KEY_IS_ADTS, 1)
    format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16 * 1024)
    // 是否设置 callback，开启异步模式
    onDecoderCreated()
    decoder!!.configure(format, null, null, 0)
    Log.w(TAG, "start() start decoder with format: $format")
    decoder!!.start()
    onDecoderStarted()
  }

  protected open fun onDecoderStarted() {
  }

  protected open fun onDecoderCreated() {
  }

  override fun enqueuePacket(packet: Packet) {
    synchronized(queueLock) {
      while (queue.size >= 4) {
        queueLock.wait(250L)
      }
      queue.addFirst(packet)
      queueLock.notifyAll()
    }
  }

  override fun dequeuePacket(): Packet? {
    synchronized(queueLock) {
      while (queue.size == 0 && !eof) {
        queueLock.wait(250L)
      }
      val packet = if (queue.size == 0) null else queue.removeLast()
      queueLock.notifyAll()
      return packet
    }
  }

  protected fun stopDecoder() {
    extractor?.release()
    extractor = null
    // 释放解码器
    decoder?.stop()
    decoder?.release()
    decoder = null
  }
}