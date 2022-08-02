package com.example.ffmpeg;

/**
 * Created on 2022/8/2
 *
 * @author binlee
 */
public class FfmpegHelper {

  static {
    final String[] requiredLibs = {
      "avcodec",
      "avdevice",
      "avfilter",
      "avformat",
      "avutil",
      "swresample",
      "swscale",
      "ffmpeg_demo",
    };
    for (String lib : requiredLibs) {
      try {
        System.loadLibrary(lib);
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
  }

  private FfmpegHelper() {
    // no instance
  }

  public static String getConfiguration() {
    return nativeGetConfiguration();
  }

  private static native String nativeGetConfiguration();
}
