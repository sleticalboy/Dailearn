//
// Created by binlee on 2022/9/5.
//

#include "ffmpeg_util.h"

namespace ffmpeg {
namespace util {

const int kBufferSize = 4096;

char *AVFormatContextToString(AVFormatContext *ctx) {
  char *buffer = new char[kBufferSize];
  sprintf(buffer, "%s", "dddd");
  return buffer;
}

} // namespace util
} // namespace ffmpeg