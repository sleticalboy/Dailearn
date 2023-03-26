package com.binlee.learning.ffmpeg.recorder

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder.AudioSource
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.binlee.learning.ffmpeg.AVFormat
import com.binlee.learning.ffmpeg.AVFormat.A_PCM
import com.binlee.learning.ffmpeg.IRecorder
import com.binlee.learning.ffmpeg.IRecorder.Callback
import java.io.RandomAccessFile
import kotlin.concurrent.thread

/**
 * Created on 3/19/23
 *
 * @author binlee
 */
open class BaseRecorder(private val format: AVFormat = A_PCM) : IRecorder {

  protected val TAG: String = javaClass.simpleName

  protected lateinit var path: String
  protected var output: RandomAccessFile? = null
  private var record: AudioRecord? = null
  private val mTimer = object : CountDownTimer(60 * 60_000L/*1h*/, 1000L) {

    private var timer = 0

    override fun onTick(millisUntilFinished: Long) {
      timer = (60 * 60 - millisUntilFinished / 1000).toInt()
      innerCallback.onTimer(timer)
    }

    override fun onFinish() {
      Log.d(TAG, "onFinish() called")
    }
  }
  private val innerCallback = object : Callback {
    private val handler = Handler(Looper.getMainLooper())
    override fun onStarted() {
      handler.post { userCallback?.onStarted() }
    }

    override fun onTimer(timer: Int) {
      handler.post { userCallback?.onTimer(timer) }
    }

    override fun onFinished(path: String) {
      handler.post { userCallback?.onFinished(path) }
    }
  }

  private var userCallback: Callback? = null

  override fun setOutputFile(path: String) {
    this.path = path
  }

  @RequiresPermission(Manifest.permission.RECORD_AUDIO)
  override fun start(callback: Callback?) {
    if (isRecording()) return

    userCallback = callback

    Log.d(TAG, "start() $callback -> $path")

    // 创建输出文件
    output = RandomAccessFile(path, "rw")

    // 构造 AudioRecord
    val size = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2
    record = AudioRecord(AudioSource.DEFAULT, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, size)
    // 开始录音
    record!!.startRecording()
    // 计时器开始计时
    mTimer.start()

    // 开启子线程从 audio buffer 中读取数据
    thread(start = true, name = "PCM-Recorder") {
      val buffer = ByteArray(size)
      while (true) {
        // 读取数据
        val readBytes = record!!.read(buffer, 0, buffer.size)
        if (readBytes <= 0) {
          Log.w(TAG, "recorder thread: no more data!")
          break
        }
        // 处理数据
        processRawData(buffer, readBytes, format)
      }
      // 释放 AudioTrack
      record!!.release()
      Log.e(TAG, "recorder thread exit!!")

      beforeFinish(output!!)
    }

    beforeStart(output!!)
  }

  override fun stop() {
    try {
      record?.stop()
    } catch (_: IllegalStateException) {
    }
    mTimer.cancel()
  }

  override fun resume() {
    if (record?.state == AudioRecord.STATE_INITIALIZED && isRecording()) {
      record?.startRecording()
    }
  }

  override fun isRecording(): Boolean {
    return record?.recordingState == AudioRecord.RECORDSTATE_RECORDING
  }

  protected open fun beforeStart(output: RandomAccessFile) {
    innerCallback.onStarted()
  }

  protected open fun beforeFinish(output: RandomAccessFile) {
    output.close()
    innerCallback.onFinished(path)
  }

  protected open fun processRawData(buffer: ByteArray, size: Int, format: AVFormat) {
    writeBuffer(buffer, size)
  }

  protected fun writeBuffer(buffer: ByteArray, size: Int) {
    if (size > 0) output?.write(buffer, 0, size)
  }
}