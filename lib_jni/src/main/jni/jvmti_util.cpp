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
