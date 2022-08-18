//
// Created by binlee on 2022/8/18.
//

#include "art_runtime.h"
#include "jni_logger.h"
#include "unseal_hidden_api.h"
#include "jni.h"
#include "sys/system_properties.h"

#define LOG_TAG "UnsealHiddenApi"

const char *kUnsealClass = "com/binlee/hidden/Hidden";
const int kMaxLength = 2000;

enum OffsetFindResult {
  // 没找到
  kNotFound = -2,
  // 非法入参
  kInvalid = -1,
};

template<typename T>
int findOffset(void *addr, int start, int limit, T target) {
  if (addr == nullptr || start < 0 || limit <=0) {
    return kInvalid;
  }
  char *vm_start = static_cast<char *>(addr);
  // 从起始地址 + offset_start 开始查找，步长为 4
  for (int i = start; i < limit; i += 4) {
    T *value = (T *) (vm_start + i);
    if (*value == target) {
      return i;
    }
  }
  return kNotFound;
}

void Unseal_unlock(JNIEnv *env, jclass clazz, jint target_sdk_version) {
  // 获取当前系统信息
  char api_level_str[5];
  char preview_api_level_str[5];
  __system_property_get("ro.build.version.sdk", api_level_str);
  __system_property_get("ro.build.version.preview_sdk", preview_api_level_str);
  ALOGD("%s api level: %s, preview api level: %s", __func__, api_level_str, preview_api_level_str)

  int api_level = atoi(api_level_str);
  int preview_api_level = atoi(preview_api_level_str);

  JavaVM *vm = nullptr;
  if (env->GetJavaVM(&vm) != JNI_OK) {
    ALOGE("%s GetJavaVM failed", __func__)
    return;
  }
  // vm 在 内存中指向 JavaVMExt 类
  JavaVMExt *java_vm = reinterpret_cast<JavaVMExt *>(vm);

  // 这里不能直接对 runtime 赋值，需要从内存中找出某个定值的偏移量，然后 vm_start + offset 对 runtime 进行赋值
  void *vm_start = java_vm->runtime;

  // 1、定位到 java_vm_ 字段
  int offset = findOffset(vm_start, 0, kMaxLength, (long) java_vm);
  if (offset == kInvalid || offset == kNotFound) {
    ALOGE("%s can not find java_vm offset, error: %d", __func__, offset)
    return;
  }
  ALOGD("%s java_vm offset: %d", __func__, offset)

  // 2、定位到 target_sdk_version
  offset = findOffset(vm_start, offset, kMaxLength, target_sdk_version);
  if (offset == kInvalid || offset == kNotFound) {
    ALOGE("%s can not find target_sdk_version offset, error: %d", __func__, offset)
    return;
  }
  ALOGD("%s target_sdk_version offset: %d", __func__, offset)
  // PartialRuntimeR *runtime = static_cast<PartialRuntimeR *>(vmExt->runtime);
  // ALOGE("%s hidden offset: %lu, value: %d", __func__, offsetof(PartialRuntimeR, hidden_api_policy_), runtime->hidden_api_policy_)
}

JNINativeMethod gMethods[] = {
  {"nativeRelieve", "(I)V", (void *) Unseal_unlock}
};

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  JNIEnv *env = nullptr;
  if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK || env == nullptr) {
    return JNI_ERR;
  }
  ALOGD("%s reserved: %p", __func__, reserved)

  jclass unseal_class = env->FindClass(kUnsealClass);
  if (env->RegisterNatives(unseal_class, gMethods, sizeof(gMethods) / sizeof(JNINativeMethod)) < 0) {
    ALOGE("%s RegisterNatives failed", __func__)
  }
  env->DeleteLocalRef(unseal_class);
  return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
  JNIEnv *env = nullptr;
  if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK || env == nullptr) {
    return;
  }
  ALOGD("%s reserved: %p", __func__, reserved)

  jclass unseal_class = env->FindClass(kUnsealClass);
  if (env->UnregisterNatives(unseal_class) < 0) {
    ALOGE("%s UnregisterNatives failed", __func__)
  }
  env->DeleteLocalRef(unseal_class);
}
