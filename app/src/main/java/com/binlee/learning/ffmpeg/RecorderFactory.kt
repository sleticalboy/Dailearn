package com.binlee.learning.ffmpeg

import com.binlee.learning.ffmpeg.AVFormat.A_AAC
import com.binlee.learning.ffmpeg.AVFormat.A_WAV
import com.binlee.learning.ffmpeg.recorder.AsyncRecorder
import com.binlee.learning.ffmpeg.recorder.BaseRecorder
import com.binlee.learning.ffmpeg.recorder.SyncRecorder
import com.binlee.learning.ffmpeg.recorder.WavRecorder

/**
 * Created on 3/19/23
 *
 * @author binlee
 */
object RecorderFactory {

  @JvmStatic fun create(format: AVFormat, async: Boolean = false): IRecorder {
    return when (format) {
      A_WAV -> WavRecorder()
      A_AAC -> if (async) AsyncRecorder() else SyncRecorder()
      else -> BaseRecorder()
    }
  }
}