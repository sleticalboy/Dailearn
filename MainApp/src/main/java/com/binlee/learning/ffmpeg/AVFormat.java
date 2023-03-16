package com.binlee.learning.ffmpeg;

/**
 * Created on 2023/3/16
 *
 * @author binlee
 */
public enum AVFormat {

  NONE(null), A_PCM(".pcm"), A_WAV(".wav"), A_AAC(".aac");

  public final String suffix;

  AVFormat(String suffix) {
    this.suffix = suffix;
  }
}
