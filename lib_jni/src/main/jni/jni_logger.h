//
// Created by binlee on 2022/7/11.
//

#include <android/log.h>

#ifndef DAILEARN_JNI_LOGGER_H
#define DAILEARN_JNI_LOGGER_H

#ifndef ALOGD
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__);
#endif

#ifndef ALOGI
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);
#endif

#ifndef ALOGE
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);
#endif

#endif //DAILEARN_JNI_LOGGER_H
