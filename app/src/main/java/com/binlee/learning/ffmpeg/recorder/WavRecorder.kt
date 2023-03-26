package com.binlee.learning.ffmpeg.recorder

import android.media.AudioFormat
import com.binlee.learning.ffmpeg.AVFormat.A_WAV
import com.binlee.learning.ffmpeg.header.WavHeader
import java.io.RandomAccessFile

/**
 * Created on 3/19/23
 *
 * @author binlee
 */
class WavRecorder : BaseRecorder(format = A_WAV) {

  override fun beforeStart(output: RandomAccessFile) {
    // 先跳过 44 字节，后面再把真正的文件头写入
    output.seek(WavHeader.SIZE.toLong())
    super.beforeStart(output)
  }

  override fun beforeFinish(output: RandomAccessFile) {
    // 保留原始指针位置
    val pointer = output.filePointer
    val pcmSize = output.length() - WavHeader.SIZE
    output.seek(0)
    output.write(WavHeader.of(AudioFormat.CHANNEL_IN_MONO, 44100, AudioFormat.ENCODING_PCM_16BIT, pcmSize))
    // 恢复到原始指针位置
    output.seek(pointer)
    super.beforeFinish(output)
  }
}