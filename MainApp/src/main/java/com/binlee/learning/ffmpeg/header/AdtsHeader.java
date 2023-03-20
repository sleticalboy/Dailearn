package com.binlee.learning.ffmpeg.header;

import android.media.AudioFormat;
import androidx.annotation.NonNull;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created on 3/19/23
 *
 * @author binlee
 */
public class AdtsHeader {

  public static final int SIZE = 7;

  private final ByteBuffer header;

  public static byte[] wrap(int channel, int sampleRate, int len) {
    return new AdtsHeader(channel, sampleRate, len).header.array();
  }

  public static int parseBodyLength(@NonNull byte[] header) {
    return header[4] << 3;
  }

  private AdtsHeader(int channel, int sampleRate, int len) {
    header = ByteBuffer.allocate(SIZE);

    final int profile = 2; // AAC LC
    final int freqIdx = mapFreqIdx(sampleRate); // 44100
    final int chanCfg = mapChanCfg(channel); // CPE

    header.put((byte) 0xFF);
    header.put((byte) 0xF9);
    header.put((byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2)));
    header.put((byte) ((chanCfg & 3) << 6 + (len >> 11)));
    header.put((byte) ((len & 0x7FF) >> 3));
    header.put((byte) (((len & SIZE) << 5) + 0x1F));
    header.put((byte) 0xFC);
  }

  // private void addADTStoPacket(byte[] packet, int packetLen) {
  //   int profile = 2; // AAC LC
  //   int freqIdx = 3; // 48000Hz
  //   int chanCfg = 2; // 2 Channel
  //   packet[0] = (byte) 0xFF;
  //   packet[1] = (byte) 0xF9;
  //   packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
  //   packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
  //   packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
  //   packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
  //   packet[6] = (byte) 0xFC;
  // }

  private static int mapChanCfg(int channel) {
    if (channel == AudioFormat.CHANNEL_IN_MONO) return 2;

    if (channel == 7) return channel + 1;
    if (channel > 7 || channel < 1) throw new IllegalArgumentException("Unsupported channel: " + channel);
    return channel;
  }

  private static int mapFreqIdx(int rate) {
    switch (rate) {
      case 96000: return 0x00;
      case 88000: return 0x01;
      case 64000: return 0x02;
      case 48000: return 0x03;
      case 44100: return 0x04;
      case 32000: return 0x05;
      case 24000: return 0x06;
      case 22000: return 0x07;
      case 16000:
      default:
        return 0x08;
    }
  }

  @NonNull @Override public String toString() {
    return Arrays.toString(header.array());
  }
}
