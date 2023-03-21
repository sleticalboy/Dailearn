package com.binlee.learning.ffmpeg.recorder

import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import com.binlee.learning.ffmpeg.AVFormat
import com.binlee.learning.ffmpeg.AVFormat.A_AAC
import com.binlee.learning.ffmpeg.Packet
import com.binlee.learning.ffmpeg.header.AdtsHeader
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.util.LinkedList

/**
 * Created on 3/19/23
 *
 * @author binlee
 */
abstract class AacRecorder : BaseRecorder(format = A_AAC) {

  protected var encoder: MediaCodec? = null

  private val queueLock = Object()
  private val queue = LinkedList<Packet>()

  protected var eos = false

  final override fun beforeStart(output: RandomAccessFile) {
    super.beforeStart(output)
    startEncoder()
  }

  override fun beforeFinish(output: RandomAccessFile) {
    if (eos) super.beforeFinish(output)
  }

  override fun processRawData(buffer: ByteArray, size: Int, format: AVFormat) {
    enqueuePacket(Packet(buffer, size))
  }

  private fun enqueuePacket(packet: Packet) {
    synchronized(queueLock) {
      queue.addFirst(packet)
      Log.d(TAG, "enqueuePacket() queue(${queue.size})")
      queueLock.notifyAll()
    }
  }

  protected fun dequeuePacket(): Packet? {
    // 正在录制中但队列为空，等待从 AudioRecord 读取数据放入队列
    return synchronized(queueLock) {
      var timer = 0
      while (isRecording() && queue.size == 0) {
        Log.w(TAG, "dequeuePacket() wait queue packet ${++timer} times")
        queueLock.wait(QUEUE_TIMEOUT)
      }
      if (queue.size == 0) null else queue.removeLast()
    }
  }

  private fun startEncoder() {
    if (encoder != null) return

    // 创建 aac 编码器
    encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
    beforeStartEncoder()
    // 配置编码器
    val format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 1)
    format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
    format.setInteger(MediaFormat.KEY_BIT_RATE, 44100 * 1 * AudioFormat.ENCODING_PCM_16BIT)
    format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16 * 1024)
    encoder!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    encoder!!.start()

    afterStartEncoder()
  }

  protected open fun beforeStartEncoder() {}

  protected open fun afterStartEncoder() {}

  protected fun writeAacFrame(buffer: ByteBuffer?, size: Int) {
    if (size == 0) return

    // 组合 aac frame
    val frame = AacFrame(buffer, size)
    // 写入文件
    writeBuffer(frame.data, frame.size)
    Log.i(TAG, "writeAacFrame() size: ${frame.size} , raw size: $size")
  }

  inner class AacFrame(buffer: ByteBuffer?, len: Int) {
    // 一个 aac 帧由 adts 头（7 字节）和 aac 数据包组成
    val size = AdtsHeader.SIZE + len
    private val header = AdtsHeader.wrap(AudioFormat.CHANNEL_IN_MONO, 44100, size)
    val data = ByteArray(size)

    init {
      System.arraycopy(header, 0, data, 0, AdtsHeader.SIZE)
      // 从输出 buffer 中取出 aac 数据填充到 data 中
      buffer?.get(data, AdtsHeader.SIZE, len)
    }
  }

  protected fun releaseEncoder() {
    encoder!!.stop()
    encoder!!.release()
    encoder = null
  }

  companion object {
    const val QUEUE_TIMEOUT = 150L
    const val ENCODER_TIMEOUT = 250L
  }
}