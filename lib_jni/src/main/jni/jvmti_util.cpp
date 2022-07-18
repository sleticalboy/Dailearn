//
// Created by binlee on 2022/7/15.
//

#include "jvmti_util.h"
#include "jni_logger.h"

#define LOG_TAG "JVMTI_UTIL"

// 线程信息：名字、组、id
std::string jvmti::getThreadInfo(jvmtiEnv *jvmti, jthread thread) {
  std::string buffer;
  jvmtiThreadInfo info = {};
  jvmtiError error = jvmti->GetThreadInfo(thread, &info);
  if (error == JVMTI_ERROR_NONE) {
    buffer.append("thread(").append(info.name) += "), priority: "
                                                  + std::to_string(info.priority) + ", daemon: " + (info.is_daemon ? "true" : "false");
    jvmtiThreadGroupInfo gInfo = {};
    error = jvmti->GetThreadGroupInfo(info.thread_group, &gInfo);
    if (error == JVMTI_ERROR_NONE) {
      buffer.append(", group(").append(gInfo.name) += ") priority: "
                                                      + std::string(gInfo.name) + ", daemon: " + (gInfo.is_daemon ? "true" : "false");
    } else {
      ALOGE("%s GetThreadGroupInfo error: %d", __func__, error)
    }
  } else {
    ALOGE("%s GetThreadInfo error: %d", __func__, error)
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
    ALOGE("%s GetMethodName error: %d", __func__, error)
  }
  return buffer;
}

std::string jvmti::getClassInfo(jvmtiEnv *jvmti, jclass klass, jlong size) {
  std::string buffer;

  char *className = {};
  jvmtiError error = jvmti->GetClassSignature(klass, &className, nullptr);
  if (error == JVMTI_ERROR_NONE) {
    buffer.append("class: ").append(className);
  }
  if (size > 0) {
    buffer.append(", size: ") += std::to_string(size);
  }
  delete className;
  return buffer;
}
