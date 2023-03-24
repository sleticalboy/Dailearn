//
// Created by binlee on 2022/8/2.
//

#include <android/log.h>
#include "log_callback_impl.h"

#define LOG_TAG "FF_LOG"

const int kBufferSize = 2048;

static int kPrintPrefix = 1;

void log_callback_android(void *ptr, int level, const char *fmt, va_list args) {
  char buf[kBufferSize];
  av_log_format_line(ptr, level, fmt, args, buf, sizeof(buf), &kPrintPrefix);
  switch (level) {
    case AV_LOG_TRACE:
    case AV_LOG_VERBOSE:
      __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, "%s", buf);
      break;
    case AV_LOG_DEBUG:
      __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "%s", buf);
      break;
    case AV_LOG_INFO:
      __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "%s", buf);
      break;
    case AV_LOG_WARNING:
      __android_log_print(ANDROID_LOG_WARN, LOG_TAG, "%s", buf);
      break;
    case AV_LOG_ERROR:
      __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, "%s", buf);
      break;
    case AV_LOG_PANIC:
      __android_log_print(ANDROID_LOG_FATAL, LOG_TAG, "%s", buf);
      break;
    default:
      __android_log_print(ANDROID_LOG_SILENT, LOG_TAG, "%s", buf);
      break;
  }
}

namespace ffmpeg {
namespace log {

void init() {
  av_log_set_level(AV_LOG_TRACE);
  // ffmpeg log 重定向到 logcat 中
  av_log_set_callback(&log_callback_android);
  // 以下两种方式都可以使用，推荐使用 FlogX() 的方式
  av_log(nullptr, AV_LOG_WARNING, "%s() set log level debug, callback: %p", __func__, &log_callback_android);
  FlogE("%s() set log level debug, callback: %p", __func__, &log_callback_android)
}
} // namespace log
} // namespace ffmpeg

