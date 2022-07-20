//
// Created by binlee on 2022/7/15.
//

#include "jvmti_util.h"
#include "jni_logger.h"

#define LOG_TAG "JVMTI_UTIL"

// 线程信息：名字、组、id
std::string jvmti::getThreadInfo(jvmtiEnv *jvmti, jthread thread) {
  std::string buffer;
  // 先判断一下线程状态再获取信息，可以规避一些错误
  jint thread_state = 0;
  jvmtiError error = jvmti->GetThreadState(thread, &thread_state);
  if (error == JVMTI_ERROR_NONE) {
    // JVMTI_THREAD_STATE_ALIVE = 0x0001,
    // JVMTI_THREAD_STATE_TERMINATED = 0x0002,
    // JVMTI_THREAD_STATE_RUNNABLE = 0x0004,
    // JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER = 0x0400,
    // JVMTI_THREAD_STATE_WAITING = 0x0080,
    // JVMTI_THREAD_STATE_WAITING_INDEFINITELY = 0x0010,
    // JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT = 0x0020,
    // JVMTI_THREAD_STATE_SLEEPING = 0x0040,
    // JVMTI_THREAD_STATE_IN_OBJECT_WAIT = 0x0100,
    // JVMTI_THREAD_STATE_PARKED = 0x0200,
    // JVMTI_THREAD_STATE_SUSPENDED = 0x100000,
    // JVMTI_THREAD_STATE_INTERRUPTED = 0x200000,
    // JVMTI_THREAD_STATE_IN_NATIVE = 0x400000,
    // JVMTI_THREAD_STATE_VENDOR_1 = 0x10000000,
    // JVMTI_THREAD_STATE_VENDOR_2 = 0x20000000,
    // JVMTI_THREAD_STATE_VENDOR_3 = 0x40000000
    if (thread_state == JVMTI_THREAD_STATE_ALIVE) {
      //
    }
  }
  jvmtiThreadInfo info = {};
  error = jvmti->GetThreadInfo(thread, &info);
  if (error == JVMTI_ERROR_NONE) {
    buffer.append("thread(").append(info.name) += "), priority: "
        + std::to_string(info.priority) + ", daemon: " + (info.is_daemon ? "true" : "false");
    jvmtiThreadGroupInfo gInfo = {};
    error = jvmti->GetThreadGroupInfo(info.thread_group, &gInfo);
    if (error == JVMTI_ERROR_NONE) {
      buffer.append(", group(").append(gInfo.name) += ") priority: "
          + std::string(gInfo.name) + ", daemon: " + (gInfo.is_daemon ? "true" : "false");
    } else {
      ALOGE("%s GetThreadGroupInfo error: %s", __func__, getErrorName(jvmti, error))
    }
  } else {
    ALOGE("%s GetThreadInfo error: %s", __func__, getErrorName(jvmti, error))
  }
  return buffer;
}

std::string jvmti::getMethodInfo(jvmtiEnv *jvmti, jmethodID method) {
  std::string buffer;
  char *name = {};
  char *sig = {};
  char *gsig = {};
  jvmtiError error = jvmti->GetMethodName(method, &name, &sig, &gsig);
  if (error == JVMTI_ERROR_NONE) {
    buffer.append(name).append(sig).append(gsig == nullptr ? "" : gsig);
  } else {
    ALOGE("%s GetMethodName error: %s", __func__, getErrorName(jvmti, error))
  }
  return buffer;
}

std::string jvmti::getClassInfo(jvmtiEnv *jvmti, jclass klass, jlong size) {
  std::string buffer;

  char *className = {};
  jvmtiError error = jvmti->GetClassSignature(klass, &className, nullptr);
  if (error == JVMTI_ERROR_NONE) {
    buffer.append("class: ").append(className);
  } else {
    ALOGE("%s GetClassSignature error: %s", __func__, getErrorName(jvmti, error))
  }
  if (size > 0) {
    buffer.append(", size: ") += std::to_string(size);
  }
  delete className;
  return buffer;
}

const char *jvmti::getErrorName(jvmtiEnv *jvmti, jvmtiError &error) {
  char *error_name = nullptr;
  jvmtiError err = jvmti->GetErrorName(error, &error_name);
  if (err != JVMTI_ERROR_NONE) {
    ALOGE("%s GetErrorName error: %d", __func__, err)
  }
  return error_name;
}

void jvmti::fromJavaConfig(JNIEnv *env, jobject jConfig, Config *config) {
  jclass cls_config = env->FindClass("com/binlee/sample/jni/JvmtiConfig");
  
  jfieldID field = env->GetFieldID(cls_config, "rootDir", "Ljava/lang/String;");
  auto root_dir = (jstring) env->GetObjectField(jConfig, field);
  config->root_dir = env->GetStringUTFChars(root_dir, JNI_FALSE);

  field = env->GetFieldID(cls_config, "objectAlloc", "Z");
  config->object_alloc = env->GetBooleanField(jConfig, field);
  field = env->GetFieldID(cls_config, "objectFree", "Z");
  config->object_free = env->GetBooleanField(jConfig, field);

  field = env->GetFieldID(cls_config, "exceptionCreate", "Z");
  config->exception_create = env->GetBooleanField(jConfig, field);
  field = env->GetFieldID(cls_config, "exceptionCatch", "Z");
  config->exception_catch = env->GetBooleanField(jConfig, field);

  field = env->GetFieldID(cls_config, "methodEnter", "Z");
  config->method_enter = env->GetBooleanField(jConfig, field);
  field = env->GetFieldID(cls_config, "methodExit", "Z");
  config->method_exit = env->GetBooleanField(jConfig, field);
}
