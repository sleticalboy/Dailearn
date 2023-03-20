package com.binlee.learning.ffmpeg;

/**
 * Created on 2023/3/16
 *
 * @author binlee
 */
public enum AVFormat {

  NONE(""), A_PCM(".pcm"), A_WAV(".wav"), A_AAC(".aac");

  public final String suffix;

  AVFormat(String suffix) {
    this.suffix = suffix;
  }

  public static AVFormat fromPath(String path) {
    return fromSuffix(path.substring(path.lastIndexOf('.')));
  }

  public static AVFormat fromSuffix(String suffix) {
    for (AVFormat format : values()) {
      if (format.suffix.equals(suffix)) return format;
    }
    return NONE;
  }
}
