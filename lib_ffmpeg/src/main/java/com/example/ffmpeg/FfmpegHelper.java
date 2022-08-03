package com.example.ffmpeg;

/**
 * Created on 2022/8/2
 *
 * @author binlee
 */
public class FfmpegHelper {

  static {
    // 加载 ffmpeg 所需的动态库
    final String[] requiredLibs = {
      // ffmpeg 编译出的动态库
      "avcodec",
      "avdevice",
      "avfilter",
      "avformat",
      "avutil",
      "swresample",
      "swscale",
      // 基于 ffmpeg 封装的库
      "ffmpeg_demo",
    };
    for (String lib : requiredLibs) {
      try {
        System.loadLibrary(lib);
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
    nativeInit();
  }

  private FfmpegHelper() {
    // no instance
  }

  public static String getConfiguration() {
    return nativeGetConfiguration();
  }

  private static native void nativeInit();

  private static native String nativeGetConfiguration();
}
