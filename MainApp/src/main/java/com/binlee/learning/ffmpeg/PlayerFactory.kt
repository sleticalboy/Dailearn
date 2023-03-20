package com.binlee.learning.ffmpeg

import com.binlee.learning.ffmpeg.AVFormat.A_AAC
import com.binlee.learning.ffmpeg.AVFormat.A_WAV
import com.binlee.learning.ffmpeg.player.AacPlayer
import com.binlee.learning.ffmpeg.player.BasePlayer
import com.binlee.learning.ffmpeg.player.WavPlayer

/**
 * Created on 2023/3/20
 *
 * @author binlee
 */
object PlayerFactory {

  @JvmStatic
  fun create(format: AVFormat): IPlayer {
    return when (format) {
      A_WAV -> WavPlayer()
      A_AAC -> AacPlayer()
      else -> BasePlayer()
    }
  }
}