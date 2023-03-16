//
// Created by binlee on 2022/8/2.
//

#include <jni.h>

#include "ffmpeg_util.h"
#include "log_callback_impl.h"

// cpp 引入 c 语言实现的库，头文件外要包一层 extern "C"
extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavutil/error.h"
#include "libavutil/avutil.h"
#include "libavdevice/avdevice.h"
#include "libavfilter/avfilter.h"
#include "libswresample/swresample.h"
#include "libswscale/swscale.h"
}

const char *kFfmpegHelperClass = "com/example/ffmpeg/FfmpegHelper";

void Ffmpeg_Init(JNIEnv *env, jclass clazz) {
  ffmpeg::log::init();
}

jstring Ffmpeg_GetVersions(JNIEnv *env, jclass clazz) {
  FlogD("%s %s", __func__, avcodec_configuration())
  return env->NewStringUTF(ffmpeg::util::GetVersionString());
}

void Ffmpeg_DumpMetaInfo(JNIEnv *env, jclass clazz, jstring filepath) {
  const char *url = env->GetStringUTFChars(filepath, JNI_FALSE);

  FlogD("%s for %s", __func__, url)

  AVFormatContext *format_ctx = nullptr;
  int res = avformat_open_input(&format_ctx, url, nullptr, nullptr);
  if (res < 0) {
    FlogE("%s, open %s error %s", __func__, url, av_err2str(res))
    goto exit;
  }
  FlogD("%s, av format context: %s", __func__, ffmpeg::util::AVFormatContextToString(format_ctx))
  av_dump_format(format_ctx, 0, url, 0);
  avformat_close_input(&format_ctx);
  // 退出
  exit:
  env->ReleaseStringUTFChars(filepath, url);
}

jint Ffmpeg_ExtractAudio(JNIEnv *env, jclass clazz, jstring jInput, jstring jOutput) {
  // 输入的媒体源
  const char *input = env->GetStringUTFChars(jInput, JNI_FALSE);
  // 输出的音频文件
  const char *output = env->GetStringUTFChars(jOutput, JNI_FALSE);

  AVFormatContext *format_ctx = nullptr;
  AVPacket *pkt;
  FILE *out_fd;

  int stream_index;

  // 打开媒体文件
  int res = avformat_open_input(&format_ctx, input, nullptr, nullptr);
  if (res < 0) {
    FlogI("%s open input failed: %s", __func__, av_err2str(res))
    goto exit;
  }

  stream_index = av_find_best_stream(format_ctx, AVMEDIA_TYPE_AUDIO, -1, -1, nullptr, 0);
  if (stream_index < 0) {
    FlogI("%s stream index: %d", __func__, stream_index)
    res = stream_index;
    goto exit;
  }

  FlogD("%s out file: %s", __func__, output)
  out_fd = fopen(output, "wr");
  if (out_fd == nullptr) {
    FlogE("%s open %s error: %d", __func__, output, errno)
    res = errno;
    goto exit;
  }

  // 分配 packet
  pkt = av_packet_alloc();
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
  // 关闭文件
  fclose(out_fd);

  exit:
  {
    // 关闭媒体文件
    avformat_close_input(&format_ctx);
    // 释放字符串
    env->ReleaseStringUTFChars(jInput, input);
    env->ReleaseStringUTFChars(jOutput, output);
    return res;
  }
}

JNINativeMethod gMethods[] = {
  {"nativeInit", "()V", (void *) Ffmpeg_Init},
  {"nativeGetVersions", "()Ljava/lang/String;", (void *) Ffmpeg_GetVersions},
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
  jclass clazz = env->FindClass(kFfmpegHelperClass);
  env->RegisterNatives(clazz, gMethods, sizeof(gMethods) / sizeof(JNINativeMethod));
  env->DeleteLocalRef(clazz);
  return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
  JNIEnv *env = nullptr;
  if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK || env == nullptr) {
    return;
  }
  jclass clazz = env->FindClass(kFfmpegHelperClass);
  env->UnregisterNatives(clazz);
  env->DeleteLocalRef(clazz);
}

