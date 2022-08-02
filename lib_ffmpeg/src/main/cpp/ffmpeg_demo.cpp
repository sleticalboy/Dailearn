//
// Created by binlee on 2022/8/2.
//

#include <jni.h>
#include <android/log.h>
#include "log_callback_impl.h"
extern "C" {
#include "libavcodec/avcodec.h"
}

#define LOGTAG "LIB_FFMPEG"

#ifndef ALOGD
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOGTAG, __VA_ARGS__);
#endif

#ifndef ALOGE
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOGTAG, __VA_ARGS__);
#endif

void Ffmpeg_Init(JNIEnv *env, jclass clazz) {
  ffmpeg::log::init();
}

jstring Ffmpeg_GetConfiguration(JNIEnv *env, jclass clazz) {
  const char *configuration = avcodec_configuration();
  ALOGD("%s %s", __func__, configuration)
  ALOGD("%s ffmpeg license: %s", __func__, avcodec_license())
  ALOGD("%s avcodec version: %d", __func__, avcodec_version())
  const AVCodec *codec = avcodec_find_decoder(AV_CODEC_ID_AAC);
  ALOGE("%s find decoder AAC: %p", __func__, codec)
  if (codec != nullptr) {
    ALOGD("AAC name: %s", codec->name)
    ALOGD("AAC long name: %s", codec->long_name)
    ALOGD("AAC wrapper name: %s", codec->wrapper_name)
  }
  return env->NewStringUTF(configuration);
}

JNINativeMethod gMethods[] = {
  {"nativeGetConfiguration", "()Ljava/lang/String;", (void *) Ffmpeg_GetConfiguration},
  {"nativeInit", "()V", (void *) Ffmpeg_Init}
};

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  JNIEnv *env = nullptr;
  int res = vm->GetEnv((void **) &env, JNI_VERSION_1_6);
  if (res != JNI_OK || env == nullptr) {
    ALOGE("%s GetEnv error: %d", __func__, res)
    return JNI_ERR;
  }
  jclass cls_ffmpeg_helper = env->FindClass("com/example/ffmpeg/FfmpegHelper");
  env->RegisterNatives(cls_ffmpeg_helper, gMethods, sizeof(gMethods) / sizeof(JNINativeMethod));
  env->DeleteLocalRef(cls_ffmpeg_helper);
  return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
  JNIEnv *env = nullptr;
  if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK || env == nullptr) {
    return;
  }
  jclass cls_ffmpeg_helper = env->FindClass("com/example/ffmpeg/FfmpegHelper");
  env->UnregisterNatives(cls_ffmpeg_helper);
  env->DeleteLocalRef(cls_ffmpeg_helper);
}

