//
// Created by binlee on 2022/9/5.
//

#ifndef DAILEARN_FFMPEG_UTIL_H
#define DAILEARN_FFMPEG_UTIL_H

extern "C" {
#include "libavformat/avformat.h"
}

namespace ffmpeg {
namespace util {

char *AVFormatContextToString(AVFormatContext *context);

} // namespace util
} // namespace ffmpeg

#endif //DAILEARN_FFMPEG_UTIL_H
