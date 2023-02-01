//
// Created by leebin on 19-3-28.
//

#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <malloc.h>
extern "C" {
#include "include/jpeglib.h"
}

#define LOG_TAG "Luban"
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);

/**
 * 鲁班压缩核心代码
 * @param data 图片数组
 * @param width 图片宽
 * @param height  图片高
 * @param quality 压缩质量
 * @param outfile 输出文件
 * @return 0 表示出错，其他正常
 */
int WriteImageToFile(uint8_t *data, int width, int height, jint quality, const char *outfile) {
  struct jpeg_compress_struct compress_struct; // 声明结构体
  struct jpeg_error_mgr error_mgr;
  compress_struct.err = jpeg_std_error(&error_mgr); // 设置错误函数
  jpeg_create_compress(&compress_struct); // 创建结构体
  FILE *image = fopen(outfile, "wb"); // 打开输出文件
  if (image == NULL) {
    ALOGE("open file error %s", outfile)
    return JNI_FALSE;
  }
  jpeg_stdio_dest(&compress_struct, image); // 设置输出文件句柄

  // 配置信息
  compress_struct.image_width = (JDIMENSION) width; // 宽
  compress_struct.image_height = (JDIMENSION) height; // 高
  // TRUE=arithmetic coding, FALSE=Huffman false 表示启用 Huffman 算法
  compress_struct.arith_code = FALSE; // 启用哈夫曼编码
  compress_struct.input_components = 3; // 像素值只取 rgb
  compress_struct.in_color_space = JCS_RGB;
  compress_struct.optimize_coding = TRUE;
  jpeg_set_defaults(&compress_struct); // 设置默认参数
  jpeg_set_quality(&compress_struct, quality, TRUE); // 设置压缩质量

  // 开始压缩
  jpeg_start_compress(&compress_struct, TRUE);

  unsigned int row_stride = compress_struct.image_width * compress_struct.input_components;
  JSAMPROW row_ptr[1];
  // 循环写入文件
  while (compress_struct.next_scanline < compress_struct.image_height) {
    row_ptr[0] = &data[compress_struct.next_scanline * row_stride];
    jpeg_write_scanlines(&compress_struct, row_ptr, 1);
  }

  // 释放资源
  jpeg_finish_compress(&compress_struct);
  jpeg_destroy_compress(&compress_struct);
  fclose(image);
  return JNI_TRUE;
}

jboolean Compress(JNIEnv *env, jclass clazz, jobject bitmap, jint quality, jstring outPath_) {
  ALOGI("native start compress quality is %d", quality);
  const char *outfile = env->GetStringUTFChars(outPath_, JNI_FALSE);
  ALOGI("out file path is %s", outfile);

  // 1、获取 bitmap info
  AndroidBitmapInfo bitmapInfo;
  int result = AndroidBitmap_getInfo(env, bitmap, &bitmapInfo);
  if (result < 0) {
    ALOGE("get bitmap info error");
    // 释放资源
    env->ReleaseStringUTFChars(outPath_, outfile);
    return FALSE;
  }
  // 2、从 bitmap 中将像素点提取到数组中
  uint8_t *pixelColor;
  result = AndroidBitmap_lockPixels(env, bitmap, (void **) &pixelColor);
  if (result < 0) {
    ALOGE("lock bitmap pixel error");
    env->ReleaseStringUTFChars(outPath_, outfile);
    return FALSE;
  }

  // 分配数组：宽*高*3 (rgb)
  uint8_t *data = (uint8_t *) malloc((size_t) (bitmapInfo.width * bitmapInfo.height * 3));
  uint8_t *tempData = data;
  // 修改色值
  int color;
  uint8_t r, g, b;
  for (int x = 0; x < bitmapInfo.height; ++x) {
    for (int y = 0; y < bitmapInfo.width; ++y) {
      // 1 1 0 0 0 0 0 0 -> a
      // 0 0 1 1 0 0 0 0 -> r
      // 0 0 0 0 1 1 0 0 -> g
      // 0 0 0 0 0 0 1 1 -> b
      color = *((int *) pixelColor); // pixelColor[i][j]
      r = (uint8_t) ((color & 0x00ff0000) >> 16); // r
      g = (uint8_t) ((color & 0x0000ff00) >> 8); // g
      b = (uint8_t) ((color & 0x000000ff)); // b
      // 存放时要按照 b g r 顺序存储
      *(data + 0) = b;
      *(data + 1) = g;
      *(data + 2) = r;
      data += 3;
      pixelColor += 4; // 4 是因为有 alpha 通道
    }
  }

  // 释放像素点
  AndroidBitmap_unlockPixels(env, bitmap);

  // 压缩图片：调用 jpeg 引擎，哈夫曼压缩
  result = WriteImageToFile(tempData, bitmapInfo.width, bitmapInfo.height, quality, outfile);
  ALOGI("write image to file result is %d", result);

  // 释放资源
  free(tempData);
  env->ReleaseStringUTFChars(outPath_, outfile);

  // 返回结果
  return result == 0 ? FALSE : TRUE;
}

/*
 * java 数据类型、方法 -> 签名
 *
 * boolean      -> Z
 * byte         -> B
 * char         -> C
 * short        -> S
 * int          -> I
 * long         -> J
 * float        -> F
 * double       -> D
 * int[]        -> [I                   // 基本数据类型数组
 * String[]     -> [Ljava/lang/Object   // 非基本数据类型数组
 * <init>       -> ()V                  // 构造函数
 * void(long)   -> (J)V                 // 方法
 */
// 声明需要注册的方法表
static JNINativeMethod gLubanMethods[] = {
  // {方法名， 方法签名[(参数)返回值]， c 函数指针}
  {"nativeCompress", "(Landroid/graphics/Bitmap;ILjava/lang/String;)Z", (void *) Compress},
};

// 重载 jni.h JNI_OnLoad() 方法
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  ALOGI("%s enter", __func__)
  JNIEnv *env = NULL;
  if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
    return JNI_ERR;
  }
  jclass clazz = env->FindClass("com/binlee/luban/Luban");
  if (env->RegisterNatives(clazz, gLubanMethods, sizeof(gLubanMethods) / sizeof(JNINativeMethod)) != JNI_OK) {
    ALOGE("%s RegisterNatives failed ", __func__)
    return JNI_ERR;
  }
  env->DeleteLocalRef(clazz);
  return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *vm, void* reserved) {
  ALOGI("%s enter", __func__)
  JNIEnv *env = NULL;
  if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
    return;
  }
  jclass clazz = env->FindClass("com/binlee/luban/Luban");
  env->UnregisterNatives(clazz);
  env->DeleteLocalRef(clazz);
}