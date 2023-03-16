package com.binlee.learning.ffmpeg;

import android.media.AudioFormat;
import androidx.annotation.NonNull;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created on 2023/3/16
 *
 * @author binlee
 */
public final class WavHeader {

  public static final int SIZE = 44;
  private final ByteBuffer header;

  public WavHeader(int channel, int sampleRate, int fmt, int pcmLen) {
    header = ByteBuffer.allocate(SIZE);

    // 'RIFF'： 4 字节（0-3）
    header.put("RIFF".getBytes(StandardCharsets.UTF_8));
    // 文件总大小（pcm 长度 + 文件头）：4 字节（4-7）
    putIntBits(pcmLen + SIZE);

    // 'WAVE' 4 字节（8-11）
    header.put("WAVE".getBytes(StandardCharsets.UTF_8));
    // 'fmt ' chunk 4 字节（12-15）
    header.put("fmt ".getBytes(StandardCharsets.UTF_8));

    // 格式信息数据（20-35） 共 16 字节

    // size of 'fmt ' chunk：4 字节（16-19）
    header.put(new byte[] { 16, 0, 0, 0 });

    // format = 1 编码方式 & 声道数：4 字节（20-23）
    final byte cs = (byte) (channel == AudioFormat.CHANNEL_IN_MONO ? 1 : 2);
    header.put(new byte[] { 1/*fmt*/, 0, cs, 0 });

    // 采样频率：4 字节（24-27）
    putIntBits(sampleRate);

    // 每秒传输速率：4 字节（28-31）
    putIntBits(fmt * sampleRate * cs/*byteRate*/);

    // block align 数据库对齐单位，每个采样需要的字节数：2 字节
    // bits per sample 每个采样需要的 bit 数：2 字节
    // 共 4 字节（32-35）
    header.put(new byte[] { 2 * 16 / 8, 0, 16, 0 });

    // data chunk：4 字节（36-39）
    header.put("data".getBytes(StandardCharsets.UTF_8));

    // pcm 字节数： 4 字节（40-43）
    putIntBits(pcmLen);
  }

  public byte[] array() {
    return header.array();
  }

  @NonNull @Override public String toString() {
    return Arrays.toString(header.array());
  }

  private void putIntBits(int val) {
    header.put((byte) (val & 0xff));
    header.put((byte) ((val >> 8) & 0xff));
    header.put((byte) ((val >> 16) & 0xff));
    header.put((byte) ((val >> 24) & 0xff));
  }
}
