//
// Created by binlee on 2022/7/15.
//

#include <string>
#include <jni.h>
#include "jvmti_config.h"

#ifndef DAILY_WORK_JVMTI_UTIL_H
#define DAILY_WORK_JVMTI_UTIL_H

using namespace std;

namespace jvmti {
namespace util {

class ThreadInfo {
private:
  jint *state_;
  char *data_ = nullptr;
  jvmtiThreadInfo *thread_info_ = nullptr;
  jvmtiThreadGroupInfo *group_info_ = nullptr;
public:
  ThreadInfo(jvmtiEnv *jvmti, jthread thread) {
    // 线程状态
    jint state = 0;
    jvmtiError error = jvmti->GetThreadState(thread, &state);
    // ALOGW("ThreadInfo#%s GetThreadState %s", __func__, getErrorName(jvmti, error))
    if (error == JVMTI_ERROR_NONE) {
      state_ = &state;
    }
    // 线程信息
    jvmtiThreadInfo thread_info = {};
    error = jvmti->GetThreadInfo(thread, &thread_info);
    // ALOGW("ThreadInfo#%s GetThreadInfo %s", __func__, getErrorName(jvmti, error))
    if (error == JVMTI_ERROR_NONE) {
      thread_info_ = &thread_info;
    }
    // 线程组信息
    if (thread_info_ != nullptr) {
      jvmtiThreadGroupInfo group_info = {};
      error = jvmti->GetThreadGroupInfo(thread_info_->thread_group, &group_info);
      // ALOGW("ThreadInfo#%s GetThreadGroupInfo %s", __func__, getErrorName(jvmti, error))
      if (error == JVMTI_ERROR_NONE) {
        group_info_ = &group_info;
      }
    }
  }

  const char *ToString();

  ~ThreadInfo() {
    if (data_ != nullptr) delete data_;
  }
};

class ClassInfo {
private:
  char *class_name_;
  char *generic_;
  int status;
  char *data_ = nullptr;
public:
  ClassInfo(jvmtiEnv *jvmti, jclass clazz) {
    jvmti->GetClassSignature(clazz, &class_name_, &generic_);
    jvmti->GetClassStatus(clazz, &status);
  }

  const char *ToString();

  ~ClassInfo() {
    if (class_name_ != nullptr) delete class_name_;
    if (generic_ != nullptr) delete generic_;
    if (data_ != nullptr) delete data_;
  }
};

class MethodInfo {
private:
  char *method_name;
  char *method_signature;
  char *method_generic;
  char *owner_class;
  char *data_ = nullptr;
public:
  MethodInfo(jvmtiEnv *jvmti, jmethodID method) {
    jvmtiError error = jvmti->GetMethodName(method, &method_name, &method_signature, &method_generic);
    if (error == JVMTI_ERROR_NONE) {
      jclass owned_class = nullptr;
      error = jvmti->GetMethodDeclaringClass(method, &owned_class);
      if (error == JVMTI_ERROR_NONE && owned_class != nullptr) {
        jvmti->GetClassSignature(owned_class, &owner_class, nullptr);
      }
    }
  }

  bool Printable();

  const char *ToString();

  ~MethodInfo() {
    if (method_name != nullptr) delete method_name;
    if (method_signature != nullptr) delete method_signature;
    if (method_generic != nullptr) delete method_generic;
    if (owner_class != nullptr) delete owner_class;
    if (data_ != nullptr) delete data_;
  }
};

class AllocInfo {
private:
  jlong size_;
  char *class_name_ = nullptr;
  char *data_ = nullptr;
public:
  AllocInfo(jvmtiEnv *jvmti, jclass klass, jlong size) {
    jvmti->GetClassSignature(klass, &class_name_, nullptr);
    size_ = size;
  }

  const char *ToString();

  ~AllocInfo() {
    // if (class_name_ != nullptr) delete class_name_;
    // if (data_ != nullptr) delete data_;
  }
};

// 获取线程信息：名字、组、id
string getThreadInfo(jvmtiEnv *jvmti, jthread thread);

// 获取方法信息
string getMethodInfo(jvmtiEnv *jvmti, jmethodID method);

// 获取类信息
string getClassInfo(jvmtiEnv *jvmti, jclass klass, jlong size = 0);

// 获取错误名
const char *getErrorName(jvmtiEnv *jvmti, jvmtiError &error);

// java 结构体转 cpp 结构体
void fromJavaConfig(JNIEnv *env, jobject jConfig, Config *config);
} // namespace util
} // namespace jvmti

#endif //DAILY_WORK_JVMTI_UTIL_H
