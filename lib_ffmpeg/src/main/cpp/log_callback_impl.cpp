//
// Created by binlee on 2022/8/2.
//

#include <android/log.h>
#include "log_callback_impl.h"
extern "C" {
#include "includes/libavutil/log.h"
}

#define LOG_TAG "FFMPEG_LOG"

void log_callback_android(void *unused, int level, const char *fmt, va_list args) {
  switch (level) {
    case AV_LOG_TRACE:
    case AV_LOG_VERBOSE:
      __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, fmt, args);
      break;
    case AV_LOG_DEBUG:
      __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, args);
      break;
    case AV_LOG_INFO:
      __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, args);
      break;
    case AV_LOG_WARNING:
      __android_log_print(ANDROID_LOG_WARN, LOG_TAG, fmt, args);
      break;
    case AV_LOG_ERROR:
      __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, args);
      break;
    case AV_LOG_PANIC:
      __android_log_print(ANDROID_LOG_FATAL, LOG_TAG, fmt, args);
      break;
    default:
      __android_log_print(ANDROID_LOG_SILENT, LOG_TAG, fmt, args);
      break;
  }
}

namespace ffmpeg {
namespace log {

void init() {
  av_log_set_level(AV_LOG_TRACE);
  av_log_set_callback(&log_callback_android);
}
} // namespace log
} // namespace ffmpeg

