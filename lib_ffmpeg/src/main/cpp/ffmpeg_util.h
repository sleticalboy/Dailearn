//
// Created by binlee on 2022/9/5.
//

#ifndef DAILEARN_FFMPEG_UTIL_H
#define DAILEARN_FFMPEG_UTIL_H

extern "C" {
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavutil/error.h"
#include "libavutil/avutil.h"
#include "libavdevice/avdevice.h"
#include "libavfilter/avfilter.h"
#include "libswresample/swresample.h"
#include "libswscale/swscale.h"
}

namespace ffmpeg {
namespace util {

const char *GetVersionString();

const char *AVFormatContextToString(AVFormatContext *ctx);

const char *AVInputFormatToString(const struct AVInputFormat *format);

const char *AVOutputFormatToString(const struct AVOutputFormat *format);

} // namespace util
} // namespace ffmpeg

#endif //DAILEARN_FFMPEG_UTIL_H
