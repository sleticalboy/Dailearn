package com.binlee.learning.ffmpeg.player

import com.binlee.learning.ffmpeg.AVFormat
import com.binlee.learning.ffmpeg.AVFormat.A_WAV
import com.binlee.learning.ffmpeg.header.WavHeader
import java.io.RandomAccessFile

/**
 * Created on 2023/3/20
 *
 * @author binlee
 */
class WavPlayer() : BasePlayer(format = A_WAV) {

  override fun onReaderStarted(input: RandomAccessFile) {
    // 跳过文件头，直接读 pcm 数据
    input.seek(WavHeader.SIZE.toLong())
  }
}