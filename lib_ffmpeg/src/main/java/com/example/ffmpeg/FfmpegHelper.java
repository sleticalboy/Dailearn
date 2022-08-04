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

  /**
   * 获取配置
   *
   * @return {@link String}
   */
  public static String getConfiguration() {
    return nativeGetConfiguration();
  }

  /**
   * 转储文件元信息
   *
   * @param filepath filepath
   */
  public static void dumpMetaInfo(String filepath) {
    nativeDumpMetaInfo(filepath);
  }

  /**
   * 提取音频
   *
   * @param input 输入媒体路径
   * @param output 输出媒体路径
   * @return 错误码
   */
  public static int extractAudio(String input, String output) {
    return nativeExtractAudio(input, output);
  }

  private static native void nativeDumpMetaInfo(String filepath);

  private static native int nativeExtractAudio(String input, String output);

  private static native void nativeInit();

  private static native String nativeGetConfiguration();
}
