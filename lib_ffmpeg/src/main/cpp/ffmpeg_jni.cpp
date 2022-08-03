//
// Created by binlee on 2022/8/2.
//

#include <jni.h>
#include "log_callback_impl.h"
extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavutil/error.h"
}

void Ffmpeg_Init(JNIEnv *env, jclass clazz) {
  ffmpeg::log::init();
}

jstring Ffmpeg_GetConfiguration(JNIEnv *env, jclass clazz) {
  const char *configuration = avcodec_configuration();
  FlogD("%s %s", __func__, configuration)
  FlogD("%s ffmpeg license: %s", __func__, avcodec_license())
  FlogD("%s avcodec version: %d", __func__, avcodec_version())
  const AVCodec *codec = avcodec_find_decoder(AV_CODEC_ID_AAC);
  FlogE("%s find decoder AAC: %p", __func__, codec)
  if (codec != nullptr) {
    FlogD("AAC name: %s", codec->name)
    FlogD("AAC long name: %s", codec->long_name)
    FlogD("AAC wrapper name: %s", codec->wrapper_name)
  }
  return env->NewStringUTF(configuration);
}

void Ffmpeg_DumpMetaInf(JNIEnv *env, jclass clazz, jstring filepath) {
  AVFormatContext *format_ctx = nullptr;
  const char *url = env->GetStringUTFChars(filepath, JNI_FALSE);
  int res = avformat_open_input(&format_ctx, url, nullptr, nullptr);
  if (res < 0) {
    FlogE("%s, open %s error %s", __func__, url, av_err2str(res))
    return;
  }
  av_dump_format(format_ctx, 0, url, 0);
  avformat_close_input(&format_ctx);
}

jstring Ffmpeg_ExtractAudio(JNIEnv *env, jclass clazz, jstring filepath) {
  return filepath;
}

JNINativeMethod gMethods[] = {
  {"nativeInit", "()V", (void *) Ffmpeg_Init},
  {"nativeGetConfiguration", "()Ljava/lang/String;", (void *) Ffmpeg_GetConfiguration},
  {"nativeDumpMetaInfo", "(Ljava/lang/String;)V", (void* ) Ffmpeg_DumpMetaInf},
  {"nativeExtractAudio", "(Ljava/lang/String;)Ljava/lang/String;", (void *) Ffmpeg_ExtractAudio},
};

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  JNIEnv *env = nullptr;
  int res = vm->GetEnv((void **) &env, JNI_VERSION_1_6);
  if (res != JNI_OK || env == nullptr) {
    FlogD("%s GetEnv error: %d", __func__, res)
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

