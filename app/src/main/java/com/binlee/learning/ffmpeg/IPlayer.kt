package com.binlee.learning.ffmpeg

/**
 * Created on 2023/3/20
 *
 * @author binlee
 */
interface IPlayer {

  enum class State {
    PLAYING, PAUSED, STOPPED
  }

  interface Callback {
    fun onState(state: State)
  }

  fun setInputFile(path: String)
  fun start(callback: Callback)
  fun pause()
  fun resume()
  fun isPlaying(): Boolean
  fun isPaused(): Boolean
}