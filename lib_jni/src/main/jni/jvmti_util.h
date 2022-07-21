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
    jvmti->GetThreadState(thread, state_);
    // 线程信息
    jvmti->GetThreadInfo(thread, thread_info_);
    // 线程组信息
    if (thread_info_ != nullptr) {
      jvmti->GetThreadGroupInfo(thread_info_->thread_group, group_info_);
    }
  }

  const char *ToString();

  ~ThreadInfo();
};

class ClassInfo {
private:
  char *class_name;
  char *generic;
  int status;
  char *data_;

public:
  ClassInfo(jvmtiEnv *jvmti, jclass clazz) {
    jvmti->GetClassSignature(clazz, &class_name, &generic);
    jvmti->GetClassStatus(clazz, &status);
  }

  ~ClassInfo();

  char *ToString();
};

class MethodInfo {
private:
  char *method_name;
  char *method_signature;
  char *method_generic;
  char *owner_class;
  const char *data_;
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

  ~MethodInfo();
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
