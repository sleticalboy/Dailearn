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

void Ffmpeg_DumpMetaInfo(JNIEnv *env, jclass clazz, jstring filepath) {
  const char *url = env->GetStringUTFChars(filepath, JNI_FALSE);

  FlogD("%s for %s", __func__, url)

  AVFormatContext *format_ctx = nullptr;
  int res = avformat_open_input(&format_ctx, url, nullptr, nullptr);
  if (res < 0) {
    FlogE("%s, open %s error %s", __func__, url, av_err2str(res))
    env->ReleaseStringUTFChars(filepath, url);
    return;
  }
  av_dump_format(format_ctx, 0, url, 0);
  avformat_close_input(&format_ctx);
  env->ReleaseStringUTFChars(filepath, url);
}

jint Ffmpeg_ExtractAudio(JNIEnv *env, jclass clazz, jstring jInput, jstring jOutput) {
  const char *input = env->GetStringUTFChars(jInput, JNI_FALSE);

  AVFormatContext *format_ctx = nullptr;

  // 打开媒体文件
  int res = avformat_open_input(&format_ctx, input, nullptr, nullptr);
  if (res < 0) {
    FlogI("%s open input failed: %s", __func__, av_err2str(res))
    // 关闭媒体文件
    avformat_close_input(&format_ctx);
    env->ReleaseStringUTFChars(jInput, input);
    return res;
  }

  int stream_index = av_find_best_stream(format_ctx, AVMEDIA_TYPE_AUDIO, -1, -1, nullptr, 0);
  if (stream_index < 0) {
    FlogI("%s stream index: %d", __func__, stream_index)
    // 关闭媒体文件
    avformat_close_input(&format_ctx);
    env->ReleaseStringUTFChars(jInput, input);
    return stream_index;
  }

  // 音频输出到文件中
  const char *output = env->GetStringUTFChars(jOutput, JNI_FALSE);
  FlogD("%s out file: %s", __func__, output)

  FILE *out_fd = fopen(output, "wr");
  if (out_fd == nullptr) {
    FlogE("%s open %s error: %d", __func__, output, errno)
    avformat_close_input(&format_ctx);
    env->ReleaseStringUTFChars(jInput, input);
    env->ReleaseStringUTFChars(jOutput, output);
    return errno;
  }

  // 分配 packet
  AVPacket *pkt = av_packet_alloc();
  while (av_read_frame(format_ctx, pkt) >= 0) {
    FlogV("%s packet size: %d", __func__, pkt->size)
    if (pkt->stream_index == stream_index) {
      // 写数据
      int len = fwrite(pkt->data, 1, pkt->size, out_fd);
      if (len != pkt->size) {
        FlogW("%s invalid write len: %d", __func__, len)
      }
    }
    // 重置 packet，以便下次复用
    av_packet_unref(pkt);
  }
  // 释放 packet
  av_packet_free(&pkt);

  // 关闭媒体文件
  avformat_close_input(&format_ctx);
  fclose(out_fd);

  // 释放字符串
  env->ReleaseStringUTFChars(jInput, input);
  env->ReleaseStringUTFChars(jOutput, output);
  return 0;
}

JNINativeMethod gMethods[] = {
  {"nativeInit", "()V", (void *) Ffmpeg_Init},
  {"nativeGetConfiguration", "()Ljava/lang/String;", (void *) Ffmpeg_GetConfiguration},
  {"nativeDumpMetaInfo", "(Ljava/lang/String;)V", (void* ) Ffmpeg_DumpMetaInfo},
  {"nativeExtractAudio", "(Ljava/lang/String;Ljava/lang/String;)I", (void *) Ffmpeg_ExtractAudio},
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

