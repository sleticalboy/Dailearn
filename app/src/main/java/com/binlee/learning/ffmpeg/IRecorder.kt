package com.binlee.learning.ffmpeg

interface IRecorder {

  interface Callback {
    fun onStarted()
    fun onTimer(timer: Int)
    fun onFinished(path: String)
  }

  fun setOutputFile(path: String)
  fun start(callback: Callback?)
  fun stop()
  fun resume()
  fun isRecording(): Boolean
}
