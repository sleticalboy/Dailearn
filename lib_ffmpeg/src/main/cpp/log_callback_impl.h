//
// Created by binlee on 2022/8/2.
//

#ifndef DAILY_WORK_LOG_CALLBACK_IMPL_H
#define DAILY_WORK_LOG_CALLBACK_IMPL_H

extern "C" {
#include "includes/libavutil/log.h"
}

#ifndef FlogV
#define FlogV(...) av_log(nullptr, AV_LOG_VERBOSE, __VA_ARGS__);
#endif

#ifndef FlogD
#define FlogD(...) av_log(nullptr, AV_LOG_DEBUG, __VA_ARGS__);
#endif

#ifndef FlogI
#define FlogI(...) av_log(nullptr, AV_LOG_INFO, __VA_ARGS__);
#endif

#ifndef FlogW
#define FlogW(...) av_log(nullptr, AV_LOG_WARNING, __VA_ARGS__);
#endif

#ifndef FlogE
#define FlogE(...) av_log(nullptr, AV_LOG_ERROR, __VA_ARGS__);
#endif

#ifndef FlogF
#define FlogF(...) av_log(nullptr, AV_LOG_FATAL, __VA_ARGS__);
#endif

namespace ffmpeg {
namespace log {

/**
 * ffmpeg log 初始化：设置 level 和 log callback
 */
void init(void);

} // namespace log
} // namespace ffmpeg

#endif //DAILY_WORK_LOG_CALLBACK_IMPL_H
