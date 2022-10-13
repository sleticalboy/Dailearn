//
// Created by binlee on 2022/8/18.
//

#include "art_runtime.h"
#include "jni_logger.h"
#include "relieve_hidden_api.h"
#include "jni.h"
#include "sys/system_properties.h"
#include "string"

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
int FindOffset(void *addr, int start, int limit, T target) {
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

int FindStringOffset(void *addr, int start, int limit, const char *target) {
  if (addr == nullptr || start < 0 || limit <=0) {
    return kInvalid;
  }
  char *vm_start = static_cast<char *>(addr);
  // 从起始地址 + offset_start 开始查找，步长为 4
  for (int i = start; i < limit; i += 4) {
    if (strcmp(vm_start + i, target) == 0) {
      return i;
    }
  }
  return kNotFound;
}

jboolean Hidden_relieve(JNIEnv *env, jclass clazz, jint target_sdk_version, jstring fingerprint) {
  JavaVM *vm = nullptr;
  if (env->GetJavaVM(&vm) != JNI_OK) {
    ALOGE("%s GetJavaVM failed", __func__)
    return false;
  }

  ALOGW("%s target_sdk_version_ offset: %lu", __func__, offsetof(PartialRuntime, target_sdk_version_))
  ALOGW("%s implicit_null_checks_ offset: %lu", __func__, offsetof(PartialRuntime, implicit_null_checks_))
  ALOGW("%s implicit_so_checks_ offset: %lu", __func__, offsetof(PartialRuntime, implicit_so_checks_))
  ALOGW("%s implicit_suspend_checks_ offset: %lu", __func__, offsetof(PartialRuntime, implicit_suspend_checks_))
  ALOGW("%s zygote_max_failed_boots_ offset: %lu", __func__, offsetof(PartialRuntime, zygote_max_failed_boots_))
  ALOGW("%s experimental_flags_ offset: %lu", __func__, offsetof(PartialRuntime, experimental_flags_))
  ALOGW("%s fingerprint_ offset: %lu", __func__, offsetof(PartialRuntime, fingerprint_))
  ALOGW("%s oat_file_manager_ offset: %lu", __func__, offsetof(PartialRuntime, oat_file_manager_))
  ALOGW("%s is_low_memory_mode_ offset: %lu", __func__, offsetof(PartialRuntime, is_low_memory_mode_))

  // vm 在内存中指向 JavaVMExt 类
  JavaVMExt *java_vm = reinterpret_cast<JavaVMExt *>(vm);
  // 这里不能直接对 runtime 赋值，需要从内存中找出某个定值的偏移量，然后 vm_start + offset 对 runtime 进行赋值
  void *vm_start = java_vm->runtime;

  int res = 0;
  PartialRuntimeR *runtime = nullptr;
  const char *fingerprint_ = env->GetStringUTFChars(fingerprint, JNI_FALSE);
  ALOGD("%s fingerprint from java: %s", __func__, fingerprint_)
  // 获取当前系统信息
  char api_level_str[5];
  char preview_api_level_str[5];
  char fingerprint_str[PROP_VALUE_MAX];
  __system_property_get("ro.build.version.sdk", api_level_str);
  __system_property_get("ro.build.version.preview_sdk", preview_api_level_str);
  __system_property_get("ro.build.fingerprint", fingerprint_str);
  ALOGD("%s api level: %s, preview api level: %s, fingerprint: %s", __func__, api_level_str, preview_api_level_str, fingerprint_str)

  int api_level = atoi(api_level_str);
  int preview_api_level = atoi(preview_api_level_str);

  // 1、定位到 java_vm_ 字段
  int offset = FindOffset(vm_start, 0, kMaxLength, (long) java_vm);
  if (offset == kInvalid || offset == kNotFound) {
    ALOGE("%s can not find java_vm offset, error: %d", __func__, offset)
    goto exit;
  }
  ALOGD("%s java_vm offset: %d", __func__, offset)

  // 2、定位到 target_sdk_version
  offset = FindOffset(vm_start, offset, kMaxLength, target_sdk_version);
  if (offset == kInvalid || offset == kNotFound) {
    ALOGE("%s can not find target_sdk_version offset, error: %d", __func__, offset)
    res = offset;
    goto exit;
  }
  ALOGD("%s target_sdk_version offset: %d", __func__, offset)

  // 3、定位到 fingerprint
  offset = FindStringOffset(vm_start, offset, kMaxLength, fingerprint_);
  if (offset == kInvalid || offset == kNotFound) {
    ALOGE("%s can not find fingerprint_ offset, error: %d", __func__, offset)
    res = offset;
    goto exit;
  }
  ALOGD("%s fingerprint_ offset: %d", __func__, offset)

  runtime = reinterpret_cast<PartialRuntimeR *>((char *) vm_start + offset);
  ALOGE("%s runtime: %p, fingerprint_: %s", __func__, runtime, runtime->fingerprint_.c_str())

  exit:
  env->ReleaseStringUTFChars(fingerprint, fingerprint_);

  return res == 0;
}

JNINativeMethod gMethods[] = {
  {"nativeRelieve", "(ILjava/lang/String;)Z", (void *) Hidden_relieve}
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
